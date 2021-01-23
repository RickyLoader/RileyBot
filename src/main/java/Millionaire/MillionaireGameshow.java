package Millionaire;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MillionaireGameshow {
    private final Member owner;
    private final String helpMessage;
    private final MessageChannel channel;
    private final Emote a, b, c, d, lifeline;
    private long gameID;
    private final Quiz quiz;
    private boolean running, victory, forfeit, paused, timeout;
    private final String correctEmote, incorrectEmote, blankEmote;
    public final static String thumb = "https://i.imgur.com/6kjTqXa.png";
    private final HashMap<Question, Timer> questionTimers;

    /**
     * Create a game of who wants to be a millionaire
     *
     * @param owner       Member who started game
     * @param channel     Channel where game takes place
     * @param emoteHelper Emote helper
     * @param helpMessage Help message
     */
    public MillionaireGameshow(Member owner, MessageChannel channel, EmoteHelper emoteHelper, String helpMessage) {
        this.owner = owner;
        this.channel = channel;
        this.a = emoteHelper.getMillionaireOptionA();
        this.b = emoteHelper.getMillionaireOptionB();
        this.c = emoteHelper.getMillionaireOptionC();
        this.d = emoteHelper.getMillionaireOptionD();
        this.lifeline = emoteHelper.getLifeline();
        this.correctEmote = EmoteHelper.formatEmote(emoteHelper.getComplete());
        this.incorrectEmote = EmoteHelper.formatEmote(emoteHelper.getFail());
        this.blankEmote = EmoteHelper.formatEmote(emoteHelper.getBlankGap());
        this.helpMessage = helpMessage;
        this.quiz = new Quiz(getQuestions());
        this.questionTimers = new HashMap<>();
    }

    /**
     * Get the questions to be used
     *
     * @return List of questions
     */
    private ArrayList<Question> getQuestions() {
        channel.sendMessage("Let me grab some questions for you").queue();
        ArrayList<Question> questions = new ArrayList<>();

        JSONArray questionData = new JSONObject(
                new NetworkRequest("https://opentdb.com/api.php?amount=15&type=multiple", false).get().body
        ).getJSONArray("results");

        Random rand = new Random();

        for(int i = 0; i < questionData.length(); i++) {
            HashMap<Emote, Answer> answers = new HashMap<>();
            ArrayList<Emote> emotes = new ArrayList<>(Arrays.asList(a, b, c, d));
            JSONObject question = questionData.getJSONObject(i);
            JSONArray answerData = question.getJSONArray("incorrect_answers");

            for(int j = 0; j < answerData.length(); j++) {
                Emote key = emotes.get(rand.nextInt(emotes.size()));
                emotes.remove(key);
                answers.put(
                        key,
                        new Answer(
                                StringEscapeUtils.unescapeHtml4(answerData.getString(j)),
                                getEmoteName(key),
                                key,
                                false
                        )
                );
            }
            Emote finalKey = emotes.get(0);
            Answer correctAnswer = new Answer(
                    StringEscapeUtils.unescapeHtml4(question.getString("correct_answer")),
                    getEmoteName(finalKey),
                    finalKey,
                    true
            );

            answers.put(finalKey, correctAnswer);
            questions.add(
                    new Question(
                            StringEscapeUtils.unescapeHtml4(question.getString("question")),
                            answers,
                            correctAnswer
                    )
            );
        }
        return questions;
    }

    /**
     * Get the option name to use for the given emote
     *
     * @param emote Emote to get option name for
     * @return Option name
     */
    private String getEmoteName(Emote emote) {
        if(emote == a) {
            return "A:";
        }
        else if(emote == b) {
            return "B:";
        }
        else if(emote == c) {
            return "C:";
        }
        else {
            return "D:";
        }
    }

    /**
     * Start the game
     */
    public void start() {
        running = true;
        sendGameMessage(buildGameMessage());
    }

    /**
     * Send or edit the game message.
     * Add reward to bank if game has completed
     */
    private void updateGame() {
        if(!running) {
            addToBank(forfeit ? quiz.getForfeitReward() : quiz.getReward());
        }
        MessageEmbed gameMessage = buildGameMessage();
        int lifeline = quiz.hasLifeline() ? 1 : 0;
        paused = true;
        channel.retrieveMessageById(gameID).queue(message -> {
            int options = lifeline + quiz.getCurrentQuestion().getAnswers().size();
            if(running && channel.getLatestMessageIdLong() == gameID && options == message.getReactions().size()) {
                channel.editMessageById(gameID, gameMessage).queue(m -> {
                    paused = false;
                    startTimer();
                });
            }
            else {
                sendGameMessage(gameMessage);
            }
        });
    }

    /**
     * Send the game message
     *
     * @param gameMessage Game message
     */
    private void sendGameMessage(MessageEmbed gameMessage) {
        channel.deleteMessageById(gameID).queue();
        channel.sendMessage(gameMessage).queue(m -> {
            gameID = m.getIdLong();
            if(running) {
                for(Answer a : quiz.getCurrentQuestion().getAnswers()) {
                    m.addReaction(a.getEmote()).queue();
                }
                if(quiz.hasLifeline()) {
                    m.addReaction(lifeline).queue();
                }
                startTimer();
            }
            paused = false;
        });
    }

    /**
     * Start a timer to end the game if an answer is not submitted within 90 seconds.
     * Ignore questions that are already being timed.
     */
    private void startTimer() {
        Question currentQuestion = quiz.getCurrentQuestion();
        if(questionTimers.containsKey(currentQuestion)) {
            return;
        }
        Timer timer = new Timer();
        questionTimers.put(currentQuestion, timer);
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if(!running || currentQuestion.hasSelectedAnswer()) {
                            timer.cancel();
                            return;
                        }
                        running = false;
                        timeout = true;
                        updateGame();
                    }
                },
                90000);
    }

    /**
     * Forfeit the game and retain the current reward
     */
    public void forfeit() {
        running = false;
        forfeit = true;
        updateGame();
    }

    /**
     * Build the game message
     *
     * @return Game message
     */
    private MessageEmbed buildGameMessage() {
        ArrayList<Answer> answers = quiz.getCurrentQuestion().getAnswers();
        EmbedBuilder builder = new EmbedBuilder();

        for(int i = 0; i < answers.size(); i++) {
            Answer a = answers.get(i);
            String value = a.getTitle();
            if(!running) {
                if(a.isSelected() && !a.isCorrect()) {
                    value = "~~" + value + "~~ " + incorrectEmote;
                }
                else if(a.isCorrect()) {
                    value = "**" + value + "** " + correctEmote;
                }
            }
            builder.addField("**" + a.getOption() + "** ", value, true);
            if((i + 2) % 2 == 0) {
                builder.addBlankField(true);
            }
        }

        return builder
                .setTitle(owner.getEffectiveName() + " " + getTitle() + " a millionaire!")
                .setDescription(buildDescription())
                .setThumbnail(thumb)
                .setFooter("Try: " + helpMessage, running ? EmbedHelper.CLOCK_GIF : EmbedHelper.CLOCK_STOPPED)
                .setColor(getColour())
                .setImage(EmbedHelper.SPACER_IMAGE)
                .build();
    }

    /**
     * Build the description of the game embed
     *
     * @return Description for embed
     */
    private String buildDescription() {
        String description = "**Question**: " + quiz.getCurrentQuestion().getTitle();
        if(running) {
            description = "__**Reward Details**__"
                    + "\n\nCurrent/If forfeit: " + quiz.formatReward(quiz.getForfeitReward())
                    + "\nIf fail: " + quiz.formatReward(quiz.getReward())
                    + "\n**Playing for**: " + quiz.formatReward(quiz.getPlayingForReward())
                    + "\n\n"
                    + description;
        }
        else {
            int reward = forfeit ? quiz.getForfeitReward() : quiz.getReward();
            description = "**You won**: "
                    + (reward == 0 ? getEmpatheticMessage() : quiz.formatReward(reward))
                    + "\n\n" + description;
        }

        String progression = getGameProgression();
        if(!quiz.isFirstQuestion()) {
            Answer previous = quiz.getPreviousQuestion().getSelectedAnswer();
            progression += "\n\n"
                    + "__**Previous Answer**__: " + "**" + previous.getOption() + "** " + previous.getTitle();
        }
        return progression + "\n\n" + description;
    }

    /**
     * Get an empathetic message to comfort the user who won $0.00
     *
     * @return Comforting message
     */
    private String getEmpatheticMessage() {
        String[] messages = new String[]{
                "Absolutely fucking nothing idiot",
                "Fuck all lmao",
                "0 dollars and 0 cents, cash or cheque?",
                "NOTHING",
                "Nothing, that question was easy as fuck too",
        };
        return messages[new Random().nextInt(messages.length)];
    }

    /**
     * Build a String showing the current progression within the quiz
     *
     * @return String showing quiz progression
     */
    private String getGameProgression() {
        StringBuilder progression = new StringBuilder();
        StringBuilder safetyNetLocation = new StringBuilder();
        String green = "🟢", white = "⚪", red = "\uD83D\uDD34", blue = "\uD83D\uDD35", yellow = "\uD83D\uDFE1";
        int currentIndex = quiz.getCurrentQuestionIndex();

        for(int i = 0; i < quiz.getTotalQuestions(); i++) {
            Question current = quiz.getCurrentQuestion();
            boolean safetyNet = quiz.isSafetyNetQuestion(i);

            if(i == currentIndex) {
                if(current.hasSelectedAnswer()) {
                    progression.append(current.getCorrectAnswer().isSelected() ? green : red);
                }
                else {
                    progression.append(blue);
                }
            }
            else if(i < currentIndex) {
                progression.append(green);
            }
            else {
                progression.append(safetyNet ? yellow : white);
            }
            safetyNetLocation.append(safetyNet ? "\uD83D\uDCB0" : blankEmote);
        }
        return safetyNetLocation.append("\n").append(progression.toString()).toString();
    }

    /**
     * Get the colour to use based on the game status
     *
     * @return Colour to use
     */
    private int getColour() {
        return running ? EmbedHelper.YELLOW : (victory ? EmbedHelper.GREEN : EmbedHelper.RED);
    }

    /**
     * Get the title to use based on the game status
     *
     * @return Title
     */
    private String getTitle() {
        if(running) {
            return "wants to be";
        }
        if(victory) {
            return "became a";
        }
        if(forfeit) {
            return "gave up on becoming";
        }
        if(timeout) {
            return "was too slow to become";
        }
        return "failed becoming";
    }

    /**
     * Reaction has been added to the game message
     *
     * @param reaction Reaction added
     */
    public void reactionAdded(MessageReaction reaction) {
        Emote emote = reaction.getReactionEmote().getEmote();
        if(paused || emote != a && emote != b && emote != c && emote != d && emote != lifeline) {
            return;
        }
        if(emote == lifeline && quiz.hasLifeline()) {
            quiz.useLifeline();
            updateGame();
            return;
        }
        answerQuestion(emote);
    }

    /**
     * Answer the question
     *
     * @param emote Emote clicked
     */
    private void answerQuestion(Emote emote) {
        Question question = quiz.getCurrentQuestion();
        Answer selected = question.selectAnswer(emote);

        if(selected.isCorrect()) {
            if(quiz.isFinalQuestion()) {
                running = false;
                victory = true;
            }
            else {
                quiz.nextQuestion();
            }
        }
        else {
            running = false;
        }
        updateGame();
    }

    /**
     * Add the winnings to the player's bank
     *
     * @param reward Reward from quiz
     */
    private void addToBank(int reward) {
        new Thread(() -> new NetworkRequest("millionaire/bank/update", true)
                .post(
                        new JSONObject()
                                .put("discord_id", owner.getIdLong())
                                .put("name", owner.getEffectiveName())
                                .put("reward", reward)
                                .toString()
                )).start();
    }

    /**
     * Get the id of the game message
     *
     * @return ID of game message
     */
    public long getGameId() {
        return gameID;
    }

    /**
     * Get whether the game is running
     *
     * @return Game is running
     */
    public boolean isActive() {
        return running;
    }

    /**
     * Millionaire quiz
     */
    private static class Quiz {
        private final ArrayList<Question> questions;
        private final HashMap<Integer, Integer> rewardMap;
        private int index;
        private final int firstCheckpoint = 4, secondCheckpoint = 9;
        private boolean lifeline;

        /**
         * Create the quiz
         *
         * @param questions Quiz questions
         */
        public Quiz(ArrayList<Question> questions) {
            this.questions = questions;
            this.rewardMap = getRewardMap();
            this.index = 0;
            this.lifeline = true;
        }

        /**
         * Check if lifeline is available
         *
         * @return Lifeline is available
         */
        public boolean hasLifeline() {
            return lifeline;
        }

        /**
         * Use the 50/50 lifeline to half the possible answers
         */
        public void useLifeline() {
            getCurrentQuestion().applyLifeline();
            this.lifeline = false;
        }

        /**
         * Get the reward for the current question
         *
         * @return Reward money for current question
         */
        public int getPlayingForReward() {
            return rewardMap.get(index);
        }

        /**
         * Get the current forfeit reward
         *
         * @return Forfeit reward
         */
        public int getForfeitReward() {
            return rewardMap.get(index - 1);
        }

        /**
         * Get the reward
         *
         * @return Reward for finishing/failing quiz
         */
        public int getReward() {
            if(questions.get(index).getCorrectAnswer().isSelected()) {
                return rewardMap.get(index);
            }
            else if(index >= secondCheckpoint + 1) {
                return rewardMap.get(secondCheckpoint);
            }
            else if(index >= firstCheckpoint + 1) {
                return rewardMap.get(firstCheckpoint);
            }
            return rewardMap.get(-1);
        }

        /**
         * Format the given reward to a String with a dollar sign and comma separated groups
         *
         * @param reward Reward value
         * @return Formatted reward String
         */
        public String formatReward(int reward) {
            return new DecimalFormat("$#,###").format(reward);
        }

        /**
         * Get the current question
         *
         * @return Current question
         */
        public Question getCurrentQuestion() {
            return questions.get(index);
        }

        /**
         * Get the previous question
         *
         * @return Previous question
         */
        public Question getPreviousQuestion() {
            return questions.get(index - 1);
        }

        /**
         * Move to the next question
         */
        public void nextQuestion() {
            this.index++;
        }

        /**
         * Check if it is the final question
         *
         * @return Final question
         */
        public boolean isFinalQuestion() {
            return index == questions.size() - 1;
        }

        /**
         * Check if the current question is a safety net/checkpoint
         *
         * @param index Index of question
         * @return Question is a safety net
         */
        public boolean isSafetyNetQuestion(int index) {
            return index == firstCheckpoint || index == secondCheckpoint;
        }

        /**
         * Build the reward map
         *
         * @return Map of question index to reward
         */
        private HashMap<Integer, Integer> getRewardMap() {
            HashMap<Integer, Integer> rewardMap = new HashMap<>();
            rewardMap.put(-1, 0);
            rewardMap.put(0, 100);
            rewardMap.put(1, 200);
            rewardMap.put(2, 300);
            rewardMap.put(3, 500);
            rewardMap.put(4, 1000);
            rewardMap.put(5, 2000);
            rewardMap.put(6, 4000);
            rewardMap.put(7, 8000);
            rewardMap.put(8, 16000);
            rewardMap.put(9, 32000);
            rewardMap.put(10, 64000);
            rewardMap.put(11, 125000);
            rewardMap.put(12, 250000);
            rewardMap.put(13, 500000);
            rewardMap.put(14, 1000000);
            return rewardMap;
        }

        /**
         * Get the number of questions
         *
         * @return Total question number
         */
        public int getTotalQuestions() {
            return questions.size();
        }

        /**
         * Get the current question index within the quiz
         *
         * @return Current question index
         */
        public int getCurrentQuestionIndex() {
            return index;
        }

        /**
         * Check if the quiz is currently on the first question
         *
         * @return Currently on first question
         */
        public boolean isFirstQuestion() {
            return index == 0;
        }
    }

    /**
     * Millionaire question
     */
    private static class Question {

        private final String title;
        private final HashMap<Emote, Answer> answerMap;
        private final ArrayList<Answer> answers;
        private final Answer correctAnswer;
        private Answer selectedAnswer;

        /**
         * Create a question
         *
         * @param title         Title of question
         * @param answerMap     Map of emote to answer
         * @param correctAnswer Correct answer
         */
        public Question(String title, HashMap<Emote, Answer> answerMap, Answer correctAnswer) {
            this.title = title;
            this.answerMap = answerMap;
            this.answers = answerMap
                    .entrySet()
                    .stream()
                    .sorted(
                            Comparator.comparing(e -> e.getValue().getOption())
                    )
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toCollection(ArrayList::new));
            this.correctAnswer = correctAnswer;
        }

        /**
         * Check if an answer has been selected
         *
         * @return Answer has been selected
         */
        public boolean hasSelectedAnswer() {
            return selectedAnswer != null;
        }

        /**
         * Get the selected answer for the question
         *
         * @return Selected answer
         */
        public Answer getSelectedAnswer() {
            return selectedAnswer;
        }

        /**
         * Get the correct answer
         *
         * @return Correct answer
         */
        public Answer getCorrectAnswer() {
            return correctAnswer;
        }

        /**
         * Get the question title
         *
         * @return Question title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Get the list of possible answers
         *
         * @return Possible answers
         */
        public ArrayList<Answer> getAnswers() {
            return answers;
        }

        /**
         * Select an answer
         *
         * @param e Emote mapped to answer
         * @return Selected answer
         */
        public Answer selectAnswer(Emote e) {
            Answer selectedAnswer = answerMap.get(e);
            selectedAnswer.selectAnswer();
            this.selectedAnswer = selectedAnswer;
            return selectedAnswer;
        }

        /**
         * Apply the 50/50 lifeline
         */
        public void applyLifeline() {
            Emote[] options = answerMap.keySet().toArray(new Emote[0]);
            Random rand = new Random();
            Arrays.sort(options, Comparator.comparingInt(o -> rand.nextInt()));

            for(Emote option : options) {
                Answer answer = answerMap.get(option);
                if(answers.size() > 2 && !answer.isCorrect()) {
                    answers.remove(answer);
                    answerMap.remove(option);
                }
            }
        }
    }

    /**
     * Question answer
     */
    private static class Answer {
        private boolean selected;
        private final boolean correct;
        private final String title, option;
        private final Emote emote;

        /**
         * Create an answer
         *
         * @param title   Answer title
         * @param option  Option name A/B/C/D
         * @param correct Answer is correct
         * @param emote   Answer emote
         */
        public Answer(String title, String option, Emote emote, boolean correct) {
            this.title = title;
            this.option = option;
            this.emote = emote;
            this.correct = correct;
            this.selected = false;
        }

        /**
         * Get the answer emote
         *
         * @return Answer emote
         */
        public Emote getEmote() {
            return emote;
        }

        /**
         * Get the option name
         *
         * @return Option name
         */
        public String getOption() {
            return option;
        }

        /**
         * Get the answer title
         *
         * @return Answer title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Get correctness of answer
         *
         * @return Answer is correct
         */
        public boolean isCorrect() {
            return correct;
        }

        /**
         * Mark the answer as selected
         */
        public void selectAnswer() {
            this.selected = true;
        }

        /**
         * Get whether the answer is locked in
         *
         * @return Answer selected
         */
        public boolean isSelected() {
            return selected;
        }
    }
}

package Millionaire;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MillionaireGameshow {
    private final Member owner;
    private final String helpMessage;
    private final MessageChannel channel;
    private final Button lifeline;
    private long gameID;
    private final Quiz quiz;
    private boolean running, victory, forfeit, paused, timeout;
    private final String correctEmote, incorrectEmote, blankEmote;
    public final static String THUMB = "https://i.imgur.com/6kjTqXa.png";
    private final HashMap<Question, Timer> questionTimers;
    private final HashMap<String, Button> answerButtons;

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
        this.answerButtons = getAnswerButtons(emoteHelper);
        this.lifeline = Button.primary("lifeline", "50:50");
        this.correctEmote = EmoteHelper.formatEmote(emoteHelper.getComplete());
        this.incorrectEmote = EmoteHelper.formatEmote(emoteHelper.getFail());
        this.blankEmote = EmoteHelper.formatEmote(emoteHelper.getBlankGap());
        this.helpMessage = helpMessage;
        this.quiz = new Quiz(getQuestions());
        this.questionTimers = new HashMap<>();
    }

    /**
     * Get a map of answer button ID -> answer button
     *
     * @param emoteHelper Emote helper for getting the emotes to use in the buttons
     * @return Map of answer button ID -> answer button
     */
    private HashMap<String, Button> getAnswerButtons(EmoteHelper emoteHelper) {
        HashMap<String, Button> answerButtons = new HashMap<>();
        Button a = Button.success("A:", Emoji.ofEmote(emoteHelper.getMillionaireOptionA()));
        Button b = Button.success("B:", Emoji.ofEmote(emoteHelper.getMillionaireOptionB()));
        Button c = Button.success("C:", Emoji.ofEmote(emoteHelper.getMillionaireOptionC()));
        Button d = Button.success("D:", Emoji.ofEmote(emoteHelper.getMillionaireOptionD()));
        answerButtons.put(a.getId(), a);
        answerButtons.put(b.getId(), b);
        answerButtons.put(c.getId(), c);
        answerButtons.put(d.getId(), d);
        return answerButtons;
    }

    /**
     * Get the questions to be used
     *
     * @return List of questions
     */
    private ArrayList<Question> getQuestions() {
        channel.sendTyping().queue();
        ArrayList<Question> questions = new ArrayList<>();

        JSONArray questionData = new JSONObject(
                new NetworkRequest("https://opentdb.com/api.php?amount=15&type=multiple", false).get().body
        ).getJSONArray("results");

        Random rand = new Random();

        for(int i = 0; i < questionData.length(); i++) {
            HashMap<String, Answer> answers = new HashMap<>();
            ArrayList<Button> buttons = new ArrayList<>(answerButtons.values());
            JSONObject question = questionData.getJSONObject(i);
            JSONArray answerData = question.getJSONArray("incorrect_answers");

            for(int j = 0; j < answerData.length(); j++) {
                Button button = buttons.get(rand.nextInt(buttons.size()));
                String key = button.getId();
                buttons.remove(button);
                answers.put(
                        key,
                        new Answer(
                                StringEscapeUtils.unescapeHtml4(answerData.getString(j)),
                                key,
                                false
                        )
                );
            }
            String finalKey = buttons.get(0).getId();
            Answer correctAnswer = new Answer(
                    StringEscapeUtils.unescapeHtml4(question.getString("correct_answer")),
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
     * Start the game
     */
    public void start() {
        running = true;
        sendGameMessage(buildGameMessage());
    }

    /**
     * Send or edit the game message.
     * Add reward to bank if game has completed
     *
     * @param event Optional Button click event to acknowledge
     */
    private void updateGame(ButtonClickEvent... event) {
        if(!running) {
            addToBank(forfeit ? quiz.getForfeitReward() : quiz.getReward());
        }

        MessageEmbed gameMessage = buildGameMessage();
        paused = true;
        ActionRow buttons = getButtons();

        if(channel.getLatestMessageIdLong() != gameID || buttons == null) {
            sendGameMessage(gameMessage, event);
            return;
        }

        if(event.length == 0) {
            channel.editMessageById(gameID, gameMessage).setActionRows(buttons).queue(message -> messageSent(event));
        }
        else {
            event[0].deferEdit().setEmbeds(gameMessage).setActionRows(buttons).queue(interactionHook -> messageSent(event));
        }
    }

    /**
     * Actions to perform after editing/sending the game message.
     * Unpause the game and start a question timer if the game is running.
     *
     * @param event Optional Button click event
     */
    private void messageSent(ButtonClickEvent... event) {
        paused = false;
        if(running) {
            startTimer(event);
        }
    }

    /**
     * Get the action row of buttons to use for the current question
     *
     * @return Buttons for current question, will be null if no buttons
     */
    @Nullable
    private ActionRow getButtons() {
        ArrayList<Button> buttons = getButtonList();
        return buttons.isEmpty() ? null : ActionRow.of(buttons);
    }

    /**
     * Get the list of buttons to use for the current question
     *
     * @return List of buttons to use for the current question
     */
    private ArrayList<Button> getButtonList() {
        ArrayList<Button> buttons = new ArrayList<>();
        if(running) {
            for(Answer a : quiz.getCurrentQuestion().getAnswers()) {
                buttons.add(answerButtons.get(a.getButtonId()));
            }
            if(quiz.hasLifeline()) {
                buttons.add(lifeline);
            }
        }
        return buttons;
    }

    /**
     * Send the game message and apply the appropriate buttons
     *
     * @param gameMessage Game message
     * @param event       Optional Button click event to acknowledge
     */
    private void sendGameMessage(MessageEmbed gameMessage, ButtonClickEvent... event) {
        if(event.length > 0) {
            event[0].deferEdit().queue();
        }
        channel.deleteMessageById(gameID).queue();

        ActionRow buttons = getButtons();
        MessageAction sendMessage = channel.sendMessage(gameMessage);
        if(buttons != null) {
            sendMessage = sendMessage.setActionRows(buttons);
        }

        sendMessage.queue(m -> {
            gameID = m.getIdLong();
            messageSent();
        });
    }

    /**
     * Start a timer to end the game if an answer is not submitted within 90 seconds.
     * Ignore questions that are already being timed.
     *
     * @param event Optional Button click event
     */
    private void startTimer(ButtonClickEvent... event) {
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
                        updateGame(event);
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
            builder.addField("**" + a.getButtonId() + "** ", value, true);
            if((i + 2) % 2 == 0) {
                builder.addBlankField(true);
            }
        }

        return builder
                .setTitle(owner.getEffectiveName() + " " + getTitle() + " a millionaire!")
                .setDescription(buildDescription())
                .setThumbnail(THUMB)
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
                    + "__**Previous Answer**__: " + "**" + previous.getButtonId() + "** " + previous.getTitle();
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
     * A button has been clicked on the game message
     *
     * @param event Button click event
     */
    public void buttonClicked(ButtonClickEvent event) {
        if(paused) {
            return;
        }
        String buttonId = event.getComponentId();
        if(buttonId.equals(lifeline.getId()) && quiz.hasLifeline()) {
            quiz.useLifeline();
            updateGame(event);
            return;
        }
        answerQuestion(buttonId, event);
    }

    /**
     * Answer the question with the given button ID.
     *
     * @param event Button click event
     */
    private void answerQuestion(String buttonId, ButtonClickEvent event) {
        Question question = quiz.getCurrentQuestion();
        Answer selected = question.selectAnswer(buttonId);

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
        updateGame(event);
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
        private final HashMap<String, Answer> answerMap;
        private final ArrayList<Answer> answers;
        private final Answer correctAnswer;
        private Answer selectedAnswer;

        /**
         * Create a question
         *
         * @param title         Title of question
         * @param answerMap     Map of button ID to answer
         * @param correctAnswer Correct answer
         */
        public Question(String title, HashMap<String, Answer> answerMap, Answer correctAnswer) {
            this.title = title;
            this.answerMap = answerMap;
            this.answers = answerMap
                    .entrySet()
                    .stream()
                    .sorted(
                            Comparator.comparing(e -> e.getValue().getButtonId())
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
         * @param buttonId ID of the button mapped to the answer
         * @return Selected answer
         */
        public Answer selectAnswer(String buttonId) {
            Answer selectedAnswer = answerMap.get(buttonId);
            selectedAnswer.selectAnswer();
            this.selectedAnswer = selectedAnswer;
            return selectedAnswer;
        }

        /**
         * Apply the 50/50 lifeline
         */
        public void applyLifeline() {
            String[] options = answerMap.keySet().toArray(new String[0]);
            Random rand = new Random();
            Arrays.sort(options, Comparator.comparingInt(o -> rand.nextInt()));

            for(String optionId : options) {
                Answer answer = answerMap.get(optionId);
                if(answers.size() > 2 && !answer.isCorrect()) {
                    answers.remove(answer);
                    answerMap.remove(optionId);
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
        private final String title;
        private final String buttonId;

        /**
         * Create an answer
         *
         * @param title    Answer title
         * @param correct  Answer is correct
         * @param buttonId ID of the button for the answer - e.g "A:" or "B:" etc
         */
        public Answer(String title, String buttonId, boolean correct) {
            this.title = title;
            this.buttonId = buttonId;
            this.correct = correct;
            this.selected = false;
        }

        /**
         * Get the ID of the button used to select the answer
         * This is also the answer option e.g - "A:", "B:" etc
         *
         * @return Answer button ID
         */
        public String getButtonId() {
            return buttonId;
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

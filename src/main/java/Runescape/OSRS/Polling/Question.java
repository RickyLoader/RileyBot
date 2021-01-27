package Runescape.OSRS.Polling;

/**
 * Wrap OSRS poll questions
 */
public class Question {
    private final int number;
    private final String text;
    private final Answer[] answers;
    private final boolean pass, opinionQuestion;
    private final Answer winner;

    /**
     * Initialise poll question
     *
     * @param number          Question number
     * @param text            Question text
     * @param answers         Array of answers
     * @param pass            Question passed 75% threshold
     * @param winner          Winning answer
     * @param opinionQuestion Question is an opinion question
     */
    public Question(int number, String text, Answer[] answers, boolean pass, Answer winner, boolean opinionQuestion) {
        this.number = number;
        this.text = number + ". " + text;
        this.answers = answers;
        this.pass = pass;
        this.opinionQuestion = opinionQuestion;
        this.winner = winner;
    }

    /**
     * Get the winning answer
     *
     * @return Winning answer
     */
    public Answer getWinner() {
        return winner;
    }

    /**
     * Return whether the question is an opinion question and therefore not subject to the 75% threshold
     *
     * @return Question is an opinion question
     */
    public boolean isOpinionQuestion() {
        return opinionQuestion;
    }

    /**
     * Return whether question received the 75% passing threshold
     *
     * @return Question passed
     */
    public boolean isPassed() {
        return pass;
    }

    /**
     * Get the question number
     *
     * @return Question number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Get the question text
     *
     * @return Question text
     */
    public String getText() {
        return text.length() > 200 ? text.substring(0, 200).trim() + "..." : text;
    }

    /**
     * Get the answers
     *
     * @return Answers
     */
    public Answer[] getAnswers() {
        return answers;
    }
}
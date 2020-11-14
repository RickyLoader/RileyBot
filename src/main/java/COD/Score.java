package COD;

/**
 * Hold score details
 */
public class Score {
    private final int scoreA, scoreB;
    private final Match.RESULT result;

    /**
     * Create a score
     *
     * @param scoreA Team a score
     * @param scoreB Team b score
     */
    public Score(int scoreA, int scoreB, Match.RESULT result) {
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.result = result;
    }

    /**
     * Get the score and format to display relevant team first
     * e.g WIN = 6/2 LOSS = 2/6
     *
     * @return Formatted score
     */
    public String getScore() {
        int min = Math.min(scoreA, scoreB);
        int max = Math.max(scoreA, scoreB);
        return result == Match.RESULT.WIN ? max + "/" + min : min + "/" + max;
    }
}
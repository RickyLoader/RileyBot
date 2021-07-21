package COD.Match;

/**
 * Hold score details
 */
public class Score {
    private final int scoreA, scoreB;
    private final MatchStats.RESULT result;

    /**
     * Create a score
     *
     * @param scoreA Team a score
     * @param scoreB Team b score
     */
    public Score(int scoreA, int scoreB, MatchStats.RESULT result) {
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
    public String getFormattedScore() {
        int min = Math.min(scoreA, scoreB);
        int max = Math.max(scoreA, scoreB);
        return result == MatchStats.RESULT.WIN ? max + "/" + min : min + "/" + max;
    }

    /**
     * Get the result of the match - WIN/LOSS etc
     *
     * @return Match result
     */
    public MatchStats.RESULT getResult() {
        return result;
    }
}
package LOL;

/**
 * TFT ranked queue info
 */
public class TFTRankedQueue extends RankedQueue {
    private final boolean veteran, inactive, freshBlood, hotStreak;

    private TFTRankedQueue(TFTRankedQueueBuilder builder) {
        super(
                builder.wins,
                builder.losses,
                builder.points,
                builder.tier,
                builder.rank,
                "TFT"
        );
        this.veteran = builder.veteran;
        this.inactive = builder.inactive;
        this.freshBlood = builder.freshBlood;
        this.hotStreak = builder.hotStreak;
    }

    public static class TFTRankedQueueBuilder {
        private int wins, losses, points;
        private String tier, rank;
        private boolean veteran, inactive, freshBlood, hotStreak;

        /**
         * Set the wins and losses
         *
         * @param wins   TFT wins
         * @param losses TFT losses
         * @return Builder
         */
        public TFTRankedQueueBuilder setWinLoss(int wins, int losses) {
            this.wins = wins;
            this.losses = losses;
            return this;
        }

        /**
         * Set the tier and rank
         *
         * @param tier Ranked tier - e.g "Silver"
         * @param rank Rank within tier - e.g "IV"
         * @return Builder
         */
        public TFTRankedQueueBuilder setTierRank(String tier, String rank) {
            this.tier = tier;
            this.rank = rank;
            return this;
        }

        /**
         * Set the league points the summoner holds within the ranked tier
         *
         * @param points League points
         * @return Builder
         */
        public TFTRankedQueueBuilder setLeaguePoints(int points) {
            this.points = points;
            return this;
        }

        /**
         * Set whether the summoner is a veteran in the queue
         *
         * @param veteran Summoner is a veteran
         * @return Builder
         */
        public TFTRankedQueueBuilder setVeteran(boolean veteran) {
            this.veteran = veteran;
            return this;
        }

        /**
         * Set whether the summoner is inactive in the queue
         *
         * @param inactive Summoner is inactive
         * @return Builder
         */
        public TFTRankedQueueBuilder setInactive(boolean inactive) {
            this.inactive = inactive;
            return this;
        }

        /**
         * Set whether the summoner is new to the queue
         *
         * @param freshBlood Summoner is new to the queue
         * @return Builder
         */
        public TFTRankedQueueBuilder setFreshBlood(boolean freshBlood) {
            this.freshBlood = freshBlood;
            return this;
        }

        /**
         * Set whether the summoner is on a hot streak in the queue
         *
         * @param hotStreak Summoner is on a hot streak
         * @return Builder
         */
        public TFTRankedQueueBuilder setHotStreak(boolean hotStreak) {
            this.hotStreak = hotStreak;
            return this;
        }

        /**
         * Build the TFTRankedQueue from the builder values
         *
         * @return TFTRankedQueue
         */
        public TFTRankedQueue build() {
            return new TFTRankedQueue(this);
        }
    }

    /**
     * Check whether the summoner is a veteran in the ranked queue
     *
     * @return Summoner is a veteran in the ranked queue
     */
    public boolean isVeteran() {
        return veteran;
    }

    /**
     * Check whether the summoner is inactive in the ranked queue
     *
     * @return Summoner is inactive in the ranked queue
     */
    public boolean isInactive() {
        return inactive;
    }

    /**
     * Check whether the summoner is new to the ranked queue
     *
     * @return Summoner is new to the ranked queue
     */
    public boolean isFreshBlood() {
        return freshBlood;
    }

    /**
     * Check whether the summoner is on a hot streak in the ranked queue
     *
     * @return Summoner is on a hot streak
     */
    public boolean onHotStreak() {
        return hotStreak;
    }
}

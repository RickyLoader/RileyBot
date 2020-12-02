package DOND;

import java.text.DecimalFormat;

/**
 * Deal or No Deal briefcase
 */
public class Briefcase {
    private final double reward;
    private boolean opened;
    private final int caseNumber;

    /**
     * Create a briefcase
     *
     * @param caseNumber Case number
     * @param reward     Reward the case holds inside
     */
    public Briefcase(int caseNumber, double reward) {
        this.caseNumber = caseNumber;
        this.reward = reward;
        this.opened = false;
    }

    /**
     * Get the case number of the briefcase
     *
     * @return Case number
     */
    public int getCaseNumber() {
        return caseNumber;
    }

    /**
     * Open the briefcase
     */
    public void openCase() {
        opened = true;
    }

    /**
     * Check if the briefcase has been opened
     *
     * @return Briefcase is open
     */
    public boolean isOpened() {
        return opened;
    }

    /**
     * Check if the reward contained within the briefcase is of
     * high value
     *
     * @return Reward is high value
     */
    public boolean isHighValue() {
        return reward > 750;
    }

    /**
     * Get the reward held by the briefcase
     *
     * @return Reward
     */
    public double getReward() {
        return reward;
    }

    /**
     * Get the reward in a truncated String format - 10000 = 10k
     * For display on the case once opened
     *
     * @return Truncated reward String
     */
    public String getCaseLabelReward() {
        if(reward < 1) {
            return "$" + reward;
        }
        if(reward < 1000) {
            return "$" + (int) reward;
        }
        int exp = (int) (Math.log(reward) / Math.log(1000));
        return new DecimalFormat("$#.#").format(
                reward / Math.pow(1000, exp)
        ) + "KMGTPE".charAt(exp - 1);
    }
}

package Runescape.Stats;

public class RuneMetrics {
    public final int questsStarted, questsCompleted, questsNotStarted, totalQuests;

    /**
     * Create player RuneMetrics
     *
     * @param questsStarted    Number of quests started
     * @param questsNotStarted Number of quests not started
     * @param questsCompleted  Number of completed quests
     */
    public RuneMetrics(int questsStarted, int questsNotStarted, int questsCompleted) {
        this.questsStarted = questsStarted;
        this.questsNotStarted = questsNotStarted;
        this.questsCompleted = questsCompleted;
        this.totalQuests = questsStarted + questsNotStarted + questsCompleted;
    }

    /**
     * Get the total number of quests there is data for
     *
     * @return Total number of quests
     */
    public int getTotalQuests() {
        return totalQuests;
    }

    /**
     * Get the total number of completed quests
     *
     * @return Number of completed quests
     */
    public int getQuestsCompleted() {
        return questsCompleted;
    }

    /**
     * Get the number of quests that have not been started
     *
     * @return Number of quests not started
     */
    public int getQuestsNotStarted() {
        return questsNotStarted;
    }

    /**
     * Get the number of quests the player has started
     *
     * @return Number of quests started
     */
    public int getQuestsStarted() {
        return questsStarted;
    }
}

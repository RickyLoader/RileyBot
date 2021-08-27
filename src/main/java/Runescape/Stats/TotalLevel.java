package Runescape.Stats;

/**
 * Total level skill
 */
public class TotalLevel extends Skill {

    /**
     * Create a total level skill using the CSV from the hiscores API.
     *
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @param csv       CSV from API
     * @param skills    Player skills (used to calculate virtual total)
     */
    public TotalLevel(int rankIndex, String[] csv, Skill[] skills) {
        super(
                SKILL_NAME.OVERALL,
                parseRank(csv, rankIndex),
                parseLevel(csv, rankIndex),
                parseXp(csv, rankIndex),
                calculateVirtualLevel(skills)
        );
    }

    /**
     * Calculate the virtual total level by summing all levels.
     *
     * @param skills Skills to add
     * @return Virtual total level
     */
    private static int calculateVirtualLevel(Skill[] skills) {
        int level = 0;
        for(Skill skill : skills) {
            if(skill.getName() == Skill.SKILL_NAME.OVERALL || !skill.isRanked()) {
                continue;
            }
            level += skill.getVirtualLevel();
        }
        return level;
    }
}

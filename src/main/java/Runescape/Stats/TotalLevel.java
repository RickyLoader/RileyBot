package Runescape.Stats;

/**
 * Total level skill
 */
public class TotalLevel extends Skill {

    /**
     * Create a total level skill
     *
     * @param rank          Total level rank
     * @param level         Total level (sum of skill levels)
     * @param xp            Total XP
     * @param virtualLevel  Virtual level (sum of skill virtual levels)
     * @param maxTotalLevel Total level when all skills are at their max level - e.g 2277
     * @param xpAtMaxLevel  XP at the max level (sum of XP at the max level for all skills)
     * @param maxXp         Maximum possible XP - e.g 4,600,000,000
     */
    private TotalLevel(int rank, int level, long xp, int virtualLevel, int maxTotalLevel, long xpAtMaxLevel, long maxXp) {
        super(SKILL_NAME.OVERALL, rank, level, xp, virtualLevel, maxTotalLevel, xpAtMaxLevel, maxXp);
    }

    @Override
    public boolean isMaxed(boolean virtual) {
        return virtual ? getXp() == getMaxXp() : getLevel() == getMaxLevel();
    }

    /**
     * Create a total level from the given player skills
     *
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @param csv       CSV from API
     * @param skills    Player skills to calculate total level values from
     * @return Total level
     */
    public static TotalLevel fromSkills(int rankIndex, String[] csv, Skill[] skills) {
        int virtualLevel = 0, maxTotalLevel = 0;
        long xpAtMaxLevel = 0, maxXp = 0;

        // Sum skill values
        for(Skill skill : skills) {
            virtualLevel += skill.getVirtualLevel();
            maxTotalLevel += skill.getMaxLevel();
            xpAtMaxLevel += skill.getXpAtMaxLevel();
            maxXp += skill.getMaxXp();
        }

        return new TotalLevel(
                parseRank(csv, rankIndex),
                parseLevel(csv, rankIndex),
                parseXp(csv, rankIndex),
                virtualLevel,
                maxTotalLevel,
                xpAtMaxLevel,
                maxXp
        );
    }
}

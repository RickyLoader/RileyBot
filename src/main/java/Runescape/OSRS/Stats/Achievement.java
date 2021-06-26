package Runescape.OSRS.Stats;

import Bot.ResourceHandler;
import Runescape.*;
import Runescape.OSRS.Boss.Boss;
import Runescape.OSRS.Boss.BossManager;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * Player achievement
 */
public class Achievement {
    private static final String
            CLUE_METRIC_REGEX = "clue_scrolls_",
            BASE_LEVEL_REGEX = "Base \\d+ Stats",
            LMS_REGEX = "last_man_standing";
    private final String name, measure, metric;
    private final long progress, threshold;
    private final boolean completed;
    private final Date date;
    private final TYPE type;

    public enum TYPE {
        BOSS,
        SKILL,
        CLUE,
        LMS,
        UNKNOWN;

        /**
         * Get an achievement type from the achievement metric
         *
         * @param metric Achievement metric e.g "attack", "commander_zilyana" etc
         * @return Achievement type
         */
        public static TYPE fromAchievementMetric(String metric) {
            Skill.SKILL_NAME skill = Skill.SKILL_NAME.fromName(metric);
            if(skill != Skill.SKILL_NAME.UNKNOWN) {
                return SKILL;
            }
            Boss boss = BossManager.getInstance().getBossByName(metric);
            if(boss.getId() != Boss.BOSS_ID.UNKNOWN) {
                return BOSS;
            }
            Clue.TYPE clue = Clue.TYPE.fromTypeName(getClueTypeName(metric));
            if(clue != Clue.TYPE.UNKNOWN) {
                return CLUE;
            }
            if(metric.matches(LMS_REGEX)) {
                return LMS;
            }
            return UNKNOWN;
        }
    }

    /**
     * Create a player achievement
     *
     * @param name      Name - e.g "500 Abyssal Sire kills"
     * @param measure   Measurement the achievement is measured in - e.g "kills"
     * @param metric    Metric the achievement is counting - e.g "abyssal_sire"
     * @param progress  Current progress/threshold
     * @param threshold Threshold - when achievement is completed
     * @param date      Date of completion (may be null)
     */
    public Achievement(String name, String measure, String metric, long progress, long threshold, @Nullable Date date) {
        this.name = name;
        this.measure = fixMeasure(name, measure);
        this.metric = metric;
        this.progress = progress;
        this.threshold = threshold;
        this.completed = progress >= threshold;
        this.date = date;
        this.type = TYPE.fromAchievementMetric(metric);
    }

    /**
     * Get the clue type name from a clue achievement metric. E.g "clue_scrolls_beginner" -> "beginner"
     *
     * @param clueMetric Clue achievement metric e.g "clue_scrolls_beginner"
     * @return Clue type name e.g "beginner"
     */
    public static String getClueTypeName(String clueMetric) {
        return clueMetric.replaceFirst(CLUE_METRIC_REGEX, "").trim();
    }

    /**
     * Fix the achievement measure - e.g The achievements for reaching a base total level
     * e.g "Base 70 Stats" have a measure of "levels" yet a metric which displays the XP of the player's lowest skill.
     * Replace "levels" with "lowest xp" in this case.
     *
     * @param name    Name - e.g "500 Abyssal Sire kills"
     * @param measure Measurement the achievement is measured in - e.g "kills"
     * @return Fixed achievement name
     */
    private String fixMeasure(String name, String measure) {
        if(!name.matches(BASE_LEVEL_REGEX)) {
            return measure;
        }
        return "lowest xp";
    }

    /**
     * Get the achievement type
     *
     * @return Achievement type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Check if the achievement type can have an associated image
     *
     * @return Achievement type can have associated image
     */
    public boolean canHaveImage() {
        return type != TYPE.UNKNOWN;
    }

    /**
     * Get the achievement image.
     * This will be null if none is available
     *
     * @return Achievement image (will be null if the achievement type is UNKNOWN or the skill/boss/etc doesn't have an image)
     */
    @Nullable
    public BufferedImage getImage() {
        ResourceHandler resourceHandler = new ResourceHandler();
        switch(type) {
            case BOSS:
                return BossManager.getInstance().getBossByName(metric).getIconImage();
            case SKILL:
                return resourceHandler.getImageResource(Skill.SKILL_NAME.fromName(metric).getImagePath());
            case CLUE:
                Clue.TYPE clueType = Clue.TYPE.fromTypeName(getClueTypeName(metric));
                return resourceHandler.getImageResource(
                        ResourceHandler.OSRS_BASE_PATH + clueType.getIconImagePath()
                );
            case LMS:
                return resourceHandler.getImageResource(LastManStanding.POINTS_ICON);
            default:
                return null;
        }
    }

    /**
     * Get the name of the achievement
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the progress percentage for the achievement
     *
     * @return Progress percent
     */
    public double getProgressPercent() {
        return ((double) progress) / ((double) threshold) / 100;
    }

    /**
     * Get the date of the achievement completion
     *
     * @return Date of completion
     */
    public Date getCompletionDate() {
        return date;
    }

    /**
     * Check if the achievement has a completion date.
     * This is false if the achievement has not yet been completed,
     * but also if the tracker provided the Unix epoch as a completion date.
     * This occurs if the player was tracked after completing an achievement, the tracker does not know
     * when the achievement occurred.
     *
     * @return Achievement has a completion date
     */
    public boolean hasCompletionDate() {
        return date != null && date.getTime() > 0;
    }

    /**
     * Check if the achievement has been completed
     *
     * @return Achievement completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Get the current progress in the achievement - may be higher than the threshold
     *
     * @return Current progress
     */
    public long getProgress() {
        return progress;
    }

    /**
     * Get the threshold, this is the completion point of the achievement
     *
     * @return Achievement threshold
     */
    public long getThreshold() {
        return threshold;
    }

    /**
     * Get the measure, this may be kills, experience, etc
     *
     * @return Measure
     */
    public String getMeasure() {
        return measure;
    }

    /**
     * Get the metric, this is the target and may be a boss, skill, etc
     *
     * @return Metric
     */
    public String getMetric() {
        return metric;
    }

    /**
     * Get the remaining number of the metric until the threshold is reached
     *
     * @return Remaining number of the metric
     */
    public long getRemaining() {
        if(completed) {
            return 0;
        }
        return threshold - progress;
    }
}

package Runescape.OSRS.Stats;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * Player achievement
 */
public class Achievement {
    private final String name, measure, metric;
    private final int progress, threshold;
    private final boolean completed;
    private final Date date;

    /**
     * Create a player achievement
     *
     * @param name      Name - e.g "500 Abyssal Sire kills"
     * @param measure   Measurement - e.g "kills"
     * @param metric    Metric - e.g "abyssal_sire"
     * @param progress  Current progress/threshold
     * @param threshold Threshold - when achievement is completed
     * @param date      Date of completion (may be null)
     */
    public Achievement(String name, String measure, String metric, int progress, int threshold, @Nullable Date date) {
        this.name = name;
        this.measure = measure;
        this.metric = metric;
        this.progress = progress;
        this.threshold = threshold;
        this.completed = progress >= threshold;
        this.date = date;
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
     * Get the date of the achievement completion
     *
     * @return Date of completion
     */
    public Date getCompletionDate() {
        return date;
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
    public int getProgress() {
        return progress;
    }

    /**
     * Get the threshold, this is the completion point of the achievement
     *
     * @return Achievement threshold
     */
    public int getThreshold() {
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
}

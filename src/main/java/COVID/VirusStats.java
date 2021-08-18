package COVID;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * COVID-19 stats
 */
public class VirusStats {
    private final Integer totalCases, totalRecovered, totalDeaths, newCases, newDeaths, activeCases, vaccineDoses;
    private final Date lastUpdated;
    private final boolean today;

    /**
     * Create the virus stats for a country
     *
     * @param totalCases     Total number of cases the country has had
     * @param totalRecovered Total number of people who have had the virus and recovered
     * @param totalDeaths    Total number of people who have died to the virus
     * @param newCases       Number of new cases as of the date of these stats (today/yesterday)
     * @param newDeaths      Number of new deaths as of the date of these stats (today/yesterday)
     * @param activeCases    Number of active cases as of the date of these stats (today/yesterday)
     * @param vaccineDoses   Total number of vaccine doses administered in the country
     * @param lastUpdated    Date of the last data update
     * @param today          Stats pertain to today's date
     */
    private VirusStats(@Nullable Integer totalCases, @Nullable Integer totalRecovered, @Nullable Integer totalDeaths, @Nullable Integer newCases, @Nullable Integer newDeaths, @Nullable Integer activeCases, @Nullable Integer vaccineDoses, Date lastUpdated, boolean today) {
        this.totalCases = totalCases;
        this.totalRecovered = totalRecovered;
        this.totalDeaths = totalDeaths;
        this.newCases = newCases;
        this.newDeaths = newDeaths;
        this.activeCases = activeCases;
        this.vaccineDoses = vaccineDoses;
        this.lastUpdated = lastUpdated;
        this.today = today;
    }

    public static class VirusStatsBuilder {
        private final boolean today;
        private final Date lastUpdated;
        private Integer totalCases, totalRecovered, totalDeaths, newCases, newDeaths, activeCases, vaccineDoses;

        /**
         * Initialise the virus stats builder
         *
         * @param today       Stats pertain to today's date
         * @param lastUpdated Date of the last data update
         */
        public VirusStatsBuilder(boolean today, Date lastUpdated) {
            this.today = today;
            this.lastUpdated = lastUpdated;
        }

        /**
         * Set the total number of vaccine doses administered in the country
         *
         * @param vaccineDoses Total number of vaccine doses administered
         * @return Builder
         */
        public VirusStatsBuilder setVaccineDoses(@Nullable Integer vaccineDoses) {
            this.vaccineDoses = vaccineDoses;
            return this;
        }

        /**
         * Set the active cases as of the date of these stats (today/yesterday)
         *
         * @param activeCases Total active cases
         * @return Builder
         */
        public VirusStatsBuilder setActiveCases(@Nullable Integer activeCases) {
            this.activeCases = activeCases;
            return this;
        }

        /**
         * Set the new cases as of the date of these stats (today/yesterday)
         *
         * @param newCases Number of new cases
         * @return Builder
         */
        public VirusStatsBuilder setNewCases(@Nullable Integer newCases) {
            this.newCases = newCases;
            return this;
        }

        /**
         * Set the new deaths as of the date of these stats (today/yesterday)
         *
         * @param newDeaths Number of new deaths
         * @return Builder
         */
        public VirusStatsBuilder setNewDeaths(@Nullable Integer newDeaths) {
            this.newDeaths = newDeaths;
            return this;
        }

        /**
         * Set the total number of cases the country has had
         *
         * @param totalCases Total number of virus cases
         * @return Builder
         */
        public VirusStatsBuilder setTotalCases(@Nullable Integer totalCases) {
            this.totalCases = totalCases;
            return this;
        }

        /**
         * Set the total number of people who have died to the virus
         *
         * @param totalDeaths Total number of virus deaths
         * @return Builder
         */
        public VirusStatsBuilder setTotalDeaths(@Nullable Integer totalDeaths) {
            this.totalDeaths = totalDeaths;
            return this;
        }

        /**
         * Set the total number of people who have had the virus and recovered.
         *
         * @param totalRecovered Total number of people who have recovered
         * @return Builder
         */
        public VirusStatsBuilder setTotalRecovered(@Nullable Integer totalRecovered) {
            this.totalRecovered = totalRecovered;
            return this;
        }

        /**
         * Create the virus stats from the builder values
         *
         * @return Virus stats from builder values
         */
        public VirusStats build() {
            return new VirusStats(
                    totalCases,
                    totalRecovered,
                    totalDeaths,
                    newCases,
                    newDeaths,
                    activeCases,
                    vaccineDoses,
                    lastUpdated,
                    today
            );
        }
    }

    /**
     * Get the date at which the virus data was last updated
     *
     * @return Date of last data update
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Get the total number of vaccine doses administered in the country
     *
     * @return Vaccine doses
     */
    @Nullable
    public Integer getVaccineDoses() {
        return vaccineDoses;
    }

    /**
     * Get the active cases as of the date of these stats (today/yesterday)
     *
     * @return Active cases
     */
    @Nullable
    public Integer getActiveCases() {
        return activeCases;
    }

    /**
     * Get the new cases as of the date of these stats (today/yesterday)
     *
     * @return New cases
     */
    @Nullable
    public Integer getNewCases() {
        return newCases;
    }

    /**
     * Get the new deaths as of the date of these stats (today/yesterday)
     *
     * @return New deaths
     */
    @Nullable
    public Integer getNewDeaths() {
        return newDeaths;
    }

    /**
     * Get the total number of cases the country has had
     *
     * @return Total cases
     */
    @Nullable
    public Integer getTotalCases() {
        return totalCases;
    }

    /**
     * Get the total number of people who have died to the virus
     *
     * @return Total deaths
     */
    @Nullable
    public Integer getTotalDeaths() {
        return totalDeaths;
    }

    /**
     * Get the total number of people who have had the virus and recovered.
     *
     * @return Total recovered cases
     */
    @Nullable
    public Integer getTotalRecovered() {
        return totalRecovered;
    }

    /**
     * Stats pertain to today's date
     *
     * @return Stats are from today
     */
    public boolean isTodayStats() {
        return today;
    }
}

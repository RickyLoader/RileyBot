package COD.Assets;

import java.text.DecimalFormat;

/**
 * Wrap Win/Loss, Kill/Death, Hits/Shots data
 */
public class Ratio {
    private final double ratio;
    private final int numerator, denominator;
    private final DecimalFormat commaFormat;

    public Ratio(int numerator, int denominator) {
        if(numerator == 0) {
            this.ratio = 0;
        }
        else if(denominator == 0) {
            this.ratio = numerator;
        }
        else {
            this.ratio = (double) numerator / (double) denominator;
        }

        this.denominator = denominator;
        this.numerator = numerator;
        this.commaFormat = new DecimalFormat("#,###");
    }

    /**
     * Format the ratio to 2 decimal places
     *
     * @param ratio Ratio to be formatted
     * @return Formatted ratio
     */
    public String formatRatio(double ratio) {
        return new DecimalFormat("0.00").format(ratio);
    }

    /**
     * Return the ratio as a percentage instead of decimal
     *
     * @return Percentage formatted ratio
     */
    public String getRatioPercentage() {
        return formatRatio(ratio * 100) + "%";
    }

    /**
     * Get decimal ratio
     *
     * @return Decimal ratio
     */
    public double getRatio() {
        return ratio;
    }

    /**
     * Get positive value (kill, hits, wins)
     *
     * @return Positive value
     */
    public int getNumerator() {
        return numerator;
    }

    /**
     * Get the numerator formatted with commas
     *
     * @return Numerator formatted with commas
     */
    public String formatNumerator() {
        return commaFormat.format(numerator);
    }

    /**
     * Get the denominator formatted with commas
     *
     * @return Denominator formatted with commas
     */
    public String formatDenominator() {
        return commaFormat.format(denominator);
    }

    /**
     * Get negative value (death, shots, losses)
     *
     * @return Negative value
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * Add the numerator and denominator and return the percentage of numerator
     *
     * @return Percentage of numerator
     */
    public String getNumeratorPercentage() {
        double percent = ((double) numerator + (double) denominator) / 100;
        return (int) ((double) numerator / percent) + "%";
    }
}
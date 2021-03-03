package COD.Assets;

import Command.Structure.PieChart;
import Network.ImgurManager;

/**
 * Hold a pie chart and link to image
 */
public class Breakdown {
    private final PieChart chart;
    private final String imageUrl;

    /**
     * Create a breakdown
     * Upload the chart image to Imgur
     *
     * @param chart Pie chart
     */
    public Breakdown(PieChart chart) {
        this.chart = chart;
        this.imageUrl = ImgurManager.uploadImage(chart.getFullImage(true), true);
    }

    /**
     * Get the breakdown chart
     *
     * @return Breakdown chart
     */
    public PieChart getChart() {
        return chart;
    }

    /**
     * Get the URL to the chart image.
     *
     * @return URL to chart image
     */
    public String getImageUrl() {
        return imageUrl;
    }
}

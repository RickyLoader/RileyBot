package COD.Match;

import Command.Structure.PieChart;
import Network.ImgurManager;

/**
 * Hold a pie chart and link to image
 */
public class Breakdown {
    private final PieChart chart;
    private final String title;
    private String imageUrl;

    /**
     * Create a breakdown
     *
     * @param title Breakdown title - e.g "Map"
     * @param chart Pie chart
     */
    public Breakdown(String title, PieChart chart) {
        this.title = title;
        this.chart = chart;
    }

    /**
     * Upload the chart image to imgbb
     *
     * @return URL to the chart image or null
     */
    private String uploadChart() {
        return ImgurManager.alternativeUpload(chart.getFullImage(true), true);
    }

    /**
     * Get the breakdown title - e.g "Map"
     *
     * @return Breakdown title
     */
    public String getTitle() {
        return title;
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
     * If it has not been uploaded yet, upload and save the URL.
     *
     * @return URL to chart image
     */
    public String getImageUrl() {
        if(imageUrl == null) {
            imageUrl = uploadChart();
        }
        return imageUrl;
    }
}

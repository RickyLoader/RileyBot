package Command.Structure;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Create a pie chart image
 */
public class PieChart {
    private final BufferedImage chart, key;
    private final int total;
    private final Font font;

    /**
     * Create a donut chart
     *
     * @param sections Sections to use in chart
     * @param font     Font to use in key
     * @param donut    Display as a donut chart
     */
    public PieChart(Section[] sections, Font font, boolean donut) {
        this.font = font;
        this.total = Arrays.stream(sections).mapToInt(Section::getQuantity).sum();
        this.chart = buildChart(sections, donut);
        this.key = buildKey(sections);
    }

    /**
     * Build an image displaying the donut chart colour key
     *
     * @param sections Sections to build key for
     * @return Image displaying donut chart colour key
     */
    private BufferedImage buildKey(Section[] sections) {
        Font font = this.font.deriveFont(25f);
        FontMetrics fm = new Canvas().getFontMetrics(font);
        int squareSize = 50, gap = 20, y = gap;

        Arrays.sort(sections, Comparator.comparingInt(o -> fm.stringWidth(o.getQuantitySummary(total))));
        int longestSummary = fm.stringWidth(sections[sections.length - 1].getQuantitySummary(total));

        Arrays.sort(sections, Comparator.comparingInt(o -> fm.stringWidth(o.getTitle())));
        int longestTitle = fm.stringWidth(sections[sections.length - 1].getTitle() + ": ");

        BufferedImage key = new BufferedImage(
                squareSize + gap + longestTitle + gap + longestSummary,
                (squareSize * sections.length) + (gap * (sections.length + 1)),
                BufferedImage.TYPE_INT_ARGB
        );

        Arrays.sort(sections, (o1, o2) -> o2.getQuantity() - o1.getQuantity());
        Graphics g = key.getGraphics();

        for(Section section : sections) {
            BufferedImage square = new BufferedImage(
                    squareSize,
                    squareSize,
                    BufferedImage.TYPE_INT_RGB
            );
            Graphics h = square.getGraphics();
            h.setColor(section.getColour());
            h.fillRect(0, 0, squareSize, squareSize);
            BufferedImage sectionImage = new BufferedImage(
                    key.getWidth(),
                    squareSize,
                    BufferedImage.TYPE_INT_ARGB
            );
            h = sectionImage.getGraphics();
            h.setColor(Color.WHITE);
            h.setFont(font);
            h.drawImage(square, 0, 0, null);
            int x = square.getWidth() + gap;
            int mid = (sectionImage.getHeight() / 2) + (h.getFontMetrics().getMaxAscent() / 2);
            h.drawString(section.getTitle() + ": ", x, mid);
            h.drawString(section.getQuantitySummary(total), x + longestTitle + gap, mid);
            h.dispose();

            g.drawImage(sectionImage, 0, y, null);
            y += sectionImage.getHeight() + gap;
        }
        g.dispose();
        return key;
    }

    /**
     * Get the donut chart
     *
     * @return Donut chart
     */
    public BufferedImage getChart() {
        return chart;
    }

    /**
     * Get an image displaying the chart and key
     *
     * @param horizontalJoin Join images horizontally (default is vertical join)
     * @return Image displaying chart and key
     */
    public BufferedImage getFullImage(boolean horizontalJoin) {
        int gap = 20;
        BufferedImage fullImage = new BufferedImage(
                horizontalJoin
                        ? chart.getWidth() + gap + key.getWidth()
                        : Math.max(chart.getWidth(), key.getWidth()),
                horizontalJoin
                        ? Math.max(chart.getHeight(), key.getHeight())
                        : chart.getHeight() + gap + key.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = fullImage.getGraphics();
        int mid = horizontalJoin ? (fullImage.getHeight() / 2) : (fullImage.getWidth() / 2);
        if(horizontalJoin) {
            g.drawImage(chart, 0, mid - (chart.getHeight() / 2), null);
            g.drawImage(key, chart.getWidth() + gap, mid - (key.getHeight() / 2), null);
        }
        else {
            g.drawImage(chart, mid - (chart.getWidth() / 2), 0, null);
            g.drawImage(key, mid - (key.getWidth() / 2), chart.getHeight() + gap, null);
        }
        g.dispose();
        return fullImage;
    }

    /**
     * Get the donut chart colour key
     *
     * @return Donut chart colour key
     */
    public BufferedImage getKey() {
        return key;
    }

    /**
     * Build an image displaying a donut chart from the given sections
     *
     * @param sections Sections to display
     * @param donut    Display as a donut chart
     * @return Image displaying donut chart
     */
    private BufferedImage buildChart(Section[] sections, boolean donut) {
        int radius = 150;
        int diameter = radius * 2;
        int sectionWidth = 80;
        BufferedImage chart = new BufferedImage(
                diameter + sectionWidth,
                diameter + sectionWidth,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = (Graphics2D) chart.getGraphics();
        g.setStroke(new BasicStroke(sectionWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (chart.getWidth() / 2) - radius;
        int y = sectionWidth / 2;
        double ogStartingAngle = 90, startingAngle = ogStartingAngle;
        double completeAngle = 360;
        for(int i = 0; i < sections.length; i++) {
            Section section = sections[i];
            int angle = (i == sections.length - 1)
                    ? (int) (completeAngle - (startingAngle - ogStartingAngle))
                    : (int) (completeAngle * ((double) section.getQuantity() / total));
            g.setColor(section.getColour());
            if(donut) {
                g.drawArc(x, y, diameter, diameter, (int) startingAngle, angle);
            }
            else {
                Arc2D arc = new Arc2D.Double(
                        x,
                        y,
                        diameter,
                        diameter,
                        (int) startingAngle,
                        angle,
                        Arc2D.PIE
                );
                g.fill(arc);
            }
            startingAngle += angle;
        }
        g.dispose();
        return chart;
    }

    /**
     * Donut chart section
     */
    public static class Section {
        private final String title;
        private final int quantity;
        private final Color colour;

        /**
         * Create a pie chart section
         *
         * @param title    Title to use for section
         * @param quantity Quantity of item to build section for
         * @param colour   Section colour
         */
        public Section(String title, int quantity, Color colour) {
            this.title = title;
            this.quantity = quantity;
            this.colour = colour;
        }

        /**
         * Get the quantity of item to build section for
         *
         * @return Quantity
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Get the title of the section
         *
         * @return Title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Get the summary of the section - e.g "15 (25%)"
         *
         * @param total Total to find percentage of for quantity
         * @return String summary - "15 (25%)"
         */
        public String getQuantitySummary(int total) {
            DecimalFormat format = new DecimalFormat("0.00'%'");
            String percent = format.format((double) quantity / (total / (double) 100));
            return quantity + " (" + percent + ")";
        }

        /**
         * Get the colour of the section
         *
         * @return Colour
         */
        public Color getColour() {
            return colour;
        }
    }
}

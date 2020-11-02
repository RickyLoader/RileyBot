package Runescape.Stats;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Hold information on a HCIM death
 */
public class HCIMStatus {
    private boolean dead, disabled;
    private String cause, location, date;

    /**
     * Create a HCIM status
     *
     * @param name Player name
     */
    public HCIMStatus(String name) {
        fetchStatus(name);
    }

    /**
     * Get death status
     *
     * @return Death status
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Get date of death
     *
     * @return Date of death
     */
    public String getDate() {
        return date;
    }

    /**
     * Get location of death
     *
     * @return Location of death
     */
    public String getLocation() {
        return location;
    }

    /**
     * Get cause for death
     *
     * @return Death cause
     */
    public String getCause() {
        return cause;
    }

    /**
     * Check HCIM status by parsing hiscores for death icon
     *
     * @param name Player name
     */
    private void fetchStatus(String name) {
        String url = "https://secure.runescape.com/m=hiscore_hardcore_ironman/ranking?user=" + name;
        try{
            Document d = Jsoup.connect(url).get();
            Element column = d.selectFirst(".hover .col2 .death-icon .death-icon__details");
            this.dead = column != null;
            if(dead) {
                Elements values = column.children();
                this.date = values.get(1).text();
                this.location = values.get(2).text();
                this.cause = values.get(3).text();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

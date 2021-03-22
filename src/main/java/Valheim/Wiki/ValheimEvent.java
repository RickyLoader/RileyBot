package Valheim.Wiki;

/**
 * Valheim random game event details
 */
public class ValheimEvent extends ValheimWikiAsset {
    private final String codename, startMessage, endMessage, creatures;
    private final int duration;

    /**
     * Create a Valheim wiki asset
     *
     * @param pageSummary  Summary of wiki page
     * @param description  Event trigger
     * @param codename     Event codename  - e.g "army_eikthyr"
     * @param startMessage Message when event starts - e.g "Eikthyr rallies the creatures of the forest"
     * @param endMessage   Message when event completes - e.g "The creatures are calming down"
     * @param creatures    Creatures involved - e.g "Boars and Necks"
     * @param duration     Duration of event (in seconds)
     */
    public ValheimEvent(ValheimPageSummary pageSummary, String description, String codename, String startMessage, String endMessage, String creatures, int duration) {
        super(pageSummary, null, description);
        this.codename = codename;
        this.startMessage = startMessage;
        this.endMessage = endMessage;
        this.creatures = creatures;
        this.duration = duration;
    }

    /**
     * Get the codename of the event - e.g "army_eikthyr"
     *
     * @return Event codename
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the duration of the event (in seconds)
     *
     * @return Duration of event
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Get the creatures involved in the event - e.g "Boars and Necks"
     *
     * @return Creatures involved in the event
     */
    public String getCreatures() {
        return creatures;
    }

    /**
     * Get the message that appears when the event completes - e.g "The creatures are calming down"
     *
     * @return Event completion message
     */
    public String getEndMessage() {
        return endMessage;
    }

    /**
     * Get the message that appears when the event starts - e.g "Eikthyr rallies the creatures of the forest"
     *
     * @return Event starting message
     */
    public String getStartMessage() {
        return startMessage;
    }
}

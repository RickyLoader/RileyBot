package LOL.Blitz;

/**
 * Build information
 */
public class BuildData {
    private final Champion champion;
    private final SummonerSpell[] spells;
    private final Item[] startingItems, buildOrder, finalBuild;
    private final Ability[] abilityOrder;
    private final Rune[] runes;
    private final String role;

    /**
     * Create the build data
     *
     * @param champion      Champion
     * @param role          Role
     * @param spells        Summoner spells
     * @param startingItems Starting items
     * @param buildOrder    Item build order
     * @param finalBuild    Final build items
     * @param abilityOrder  Abilities in order of unlock
     * @param runes         Runes
     */
    public BuildData(Champion champion, String role, SummonerSpell[] spells, Item[] startingItems, Item[] buildOrder, Item[] finalBuild, int[] abilityOrder, Rune[] runes) {
        this.champion = champion;
        this.role = role;
        this.spells = spells;
        this.startingItems = startingItems;
        this.buildOrder = buildOrder;
        this.finalBuild = finalBuild;
        Ability[] abilities = new Ability[abilityOrder.length];
        for(int i = 0; i < abilityOrder.length; i++) {
            abilities[i] = champion.getAbility(abilityOrder[i]);
        }
        this.abilityOrder = abilities;
        this.runes = runes;
    }

    /**
     * Get most common runes
     *
     * @return Most common runes
     */
    public Rune[] getRunes() {
        return runes;
    }

    /**
     * Get the role which the data represents
     *
     * @return Role
     */
    public String getRole() {
        return role;
    }

    /**
     * Get the champion abilities in the most common unlock order
     *
     * @return Most common champion ability order
     */
    public Ability[] getAbilityOrder() {
        return abilityOrder;
    }

    /**
     * Get the champion
     *
     * @return Champion
     */
    public Champion getChampion() {
        return champion;
    }

    /**
     * Get the most common item build order
     *
     * @return Most common item build order
     */
    public Item[] getBuildOrder() {
        return buildOrder;
    }

    /**
     * Get the most common final build items
     *
     * @return Most common final build items
     */
    public Item[] getFinalBuild() {
        return finalBuild;
    }

    /**
     * Get the most common starting items
     *
     * @return Most common starting items
     */
    public Item[] getStartingItems() {
        return startingItems;
    }

    /**
     * Get the most common summoner spells
     *
     * @return Most common summoner spells
     */
    public SummonerSpell[] getSpells() {
        return spells;
    }

    /**
     * Get a summary of the build data
     *
     * @return Build data summary
     */
    public String getDescription() {
        return "Here is the most common **"
                + champion.getName()
                + " "
                +
                role
                + "** build data for Platinum and above!";
    }
}

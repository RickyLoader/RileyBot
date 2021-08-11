package Riot.LOL.Blitz;

import Bot.ResourceHandler;

import java.awt.image.BufferedImage;

/**
 * Summoner spell
 */
public class SummonerSpell {
    private final String name;
    private final BufferedImage spellImage;

    /**
     * Create a summoner spell
     *
     * @param name Spell name - Ghost
     * @param key  Spell key - SummonerHaste
     */
    public SummonerSpell(String name, String key) {
        this.spellImage = new ResourceHandler().getImageResource(ResourceHandler.LEAGUE_BASE_PATH + "Summoner/Spells/" + key + ".png");
        this.name = name;
    }

    /**
     * Get the spell image
     *
     * @return Spell image
     */
    public BufferedImage getSpellImage() {
        return spellImage;
    }

    /**
     * Get the summoner spell name
     *
     * @return Summoner spell name
     */
    public String getName() {
        return name;
    }
}

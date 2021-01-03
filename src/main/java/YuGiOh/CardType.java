package YuGiOh;

/**
 * Yu-Gi-Oh card type
 */
public class CardType {
    private final int colour;
    private final TYPE type;

    public enum TYPE {
        SPELL_CARD,
        SKILL_CARD,
        TRAP_CARD,
        NORMAL_MONSTER,
        TOKEN,
        EFFECT_MONSTER,
        SYNCHRO_TUNER_MONSTER,
        UNION_MONSTER,
        SPIRIT_MONSTER,
        TOON_MONSTER,
        FLIP_EFFECT_MONSTER,
        GEMINI_MONSTER,
        RITUAL_EFFECT_MONSTER,
        PENDULUM_NORMAL_MONSTER,
        TUNER_MONSTER,
        FUSION_MONSTER,
        SYNCHRO_MONSTER,
        XYZ_MONSTER,
        PENDULUM_EFFECT_MONSTER,
        LINK_MONSTER,
        MISSING;

        /**
         * Get a card type by name
         *
         * @param name Name of card type - "Normal Monster"
         * @return Card type of name (NORMAL_MONSTER) or MISSING card type
         */
        public static TYPE byName(String name) {
            try {
                return valueOf(name.toUpperCase().replace(" ", "_"));
            }
            catch(IllegalArgumentException e) {
                return MISSING;
            }
        }
    }


    /**
     * Create a CardType and discern the appropriate colour to use
     *
     * @param type Card type
     */
    public CardType(TYPE type) {
        this.type = type;
        this.colour = discernColour();
    }

    /**
     * Get the colour of the type
     *
     * @return Type colour
     */
    public int getColour() {
        return colour;
    }

    /**
     * Discern the colour to use based on the type of card
     *
     * @return Colour
     */
    private int discernColour() {
        switch(type) {
            case TRAP_CARD:
                return 11688069;
            case XYZ_MONSTER:
                return 0;
            case LINK_MONSTER:
                return 852109;
            case TOKEN:
                return 12632256;
            case EFFECT_MONSTER:
                return 15829841;
            case FUSION_MONSTER:
                return 10323384;
            case NORMAL_MONSTER:
                return 16377733;
            case RITUAL_EFFECT_MONSTER:
                return 10597837;
            case SPIRIT_MONSTER:
            case SYNCHRO_MONSTER:
                return 13421772;
            case PENDULUM_EFFECT_MONSTER:
            case SPELL_CARD:
                return 4562802;
        }
        return 0;
    }
}

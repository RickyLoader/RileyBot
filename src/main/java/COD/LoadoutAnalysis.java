package COD;

import COD.Assets.Perk;
import COD.Assets.Weapon;
import COD.Match.Loadout;
import COD.Match.LoadoutWeapon;

import java.util.Random;

/**
 * Analyse a COD loadout
 */
public class LoadoutAnalysis {
    private final Loadout loadout;
    private final String[] analysis;

    /**
     * Create the loadout analysis
     *
     * @param loadout Loadout to analyse
     */
    public LoadoutAnalysis(Loadout loadout) {
        this.loadout = loadout;
        this.analysis = new String[]{
                getRandomItem(getGeneralAnalysis()),
                getRandomItem(getPrimaryAnalysis()),
                getRandomItem(getSecondaryAnalysis()),
                getRandomItem(getPerkAnalysis()),
                getRandomItem(getEquipmentAnalysis())
        };
    }

    /**
     * Get an array of comments on the loadout secondary weapon
     *
     * @return Secondary weapon comments
     */
    private String[] getSecondaryAnalysis() {
        Weapon secondary = loadout.getSecondary().getWeapon();
        if(secondary.getCategory() == Weapon.CATEGORY.MELEE) {
            return new String[]{
                    "Use the " + secondary.getName() + " to rush the spawn!",
                    "The " + secondary.getName() + " is great for clobbering!",
                    "Time to slam some cunts with the " + secondary.getName(),
                    "Remember to zig zag when using the " + secondary.getName(),
                    loadout.getPrimary().getWeapon().getCategory() == Weapon.CATEGORY.OTHER
                            ? "Unlucky."
                            : "Who needs the " + loadout.getPrimary().getName() + " when you have the "
                            + secondary.getName() + " for slamming!",
            };
        }
        return new String[]{
                "It's off-meta sure, but the " + secondary.getName() + " is what you want in a secondary.",
                "The " + secondary.getName() + " is the best secondary to fall back on, very reliable!",
                "I honestly don't see enough people using the " + secondary.getName() + ", it's underrated.",
                secondary.getName().equalsIgnoreCase("jokr")
                        ? "Keep your eyes on the skies!"
                        : "The " + secondary.getName() + " is much better than the JOKR",
                "Enough is enough! I've had it up to here with the " + secondary.getName() + "!",
        };
    }

    /**
     * Get an array of comments on the loadout perks
     *
     * @return Perk comments
     */
    private String[] getPerkAnalysis() {
        Perk[] perks = loadout.getPerks();
        Perk random = perks[new Random().nextInt(perks.length)];
        return new String[]{
                perks[1].getName().equalsIgnoreCase("overkill")
                        ? "I gave you 2 primary weapons you greedy cunt"
                        : perks[1].getName() + "? Overkill is way better",
                "Nothing like " + perks[0].getName() + " and " + perks[2].getName() + " to get you an easy win!",
                random.getName() + " is my favourite " + random.getCategory().name().toLowerCase() + " perk!",
                random.getName() + " is my least favourite " + random.getCategory().name().toLowerCase() + " perk!",
        };
    }

    /**
     * Get an array of comments on the loadout equipment
     *
     * @return Equipment comments
     */
    private String[] getEquipmentAnalysis() {
        boolean c4 = loadout.getLethal().getName().equalsIgnoreCase("c4");
        boolean stim = loadout.getTactical().getName().equalsIgnoreCase("stim");
        String[] stimIngredients = new String[]{
                "Panadol",
                "Nurofen",
                "Meth",
                "Robitussin",
                "HGH",
                "Epinephrine",
                "Snake oil",
                "Ribena",
                "Redbull",
                "Lift+"
        };
        return new String[]{
                stim
                        ? "What do you reckon is even in the stim? "
                        + stimIngredients[new Random().nextInt(stimIngredients.length)] + "?"
                        : "The " + loadout.getTactical().getName()
                        + " will give you a break from slamming yourself with stims.",
                c4
                        ? loadout.getLethal().getName() + " is the worst lethal."
                        : loadout.getLethal().getName() + "? At least it's not C4",
                loadout.getTactical().getName().contains("stun")
                        ? loadout.getTactical().getName() + "s are for cunts who can't hit a moving target"
                        : "I'll take a " + loadout.getTactical().getName() + " over a Stun Grenade any day!"
        };
    }

    /**
     * Get an array of comments on the loadout primary weapon
     *
     * @return Primary weapon comments
     */
    private String[] getPrimaryAnalysis() {
        LoadoutWeapon primary = loadout.getPrimary();
        return new String[]{
                "Lucky! the " + primary.getName() + " is my favourite gun!",
                "The " + primary.getName() + " is like being handed a free win!",
                "You seem like a " + primary.getName() + " kind of person bro",
                primary.getName() + "? Doable.",
                "I hate the " + primary.getName(),
                "The " + primary.getName() + " is the worst primary in the game",
        };
    }

    /**
     * Get a general comment on the loadout as a whole
     *
     * @return General loadout analysis
     */
    private String[] getGeneralAnalysis() {
        LoadoutWeapon primary = loadout.getPrimary();
        LoadoutWeapon secondary = loadout.getSecondary();
        Perk[] perks = loadout.getPerks();
        Weapon.CATEGORY secondaryCategory = secondary.getWeapon().getCategory();

        if(primary.getWeapon().getCategory() == Weapon.CATEGORY.OTHER) {
            if(secondaryCategory == Weapon.CATEGORY.MELEE) {
                return new String[]{
                        "Corner specialist -  " + primary.getName()
                                + " to block the bullets, then " + secondary.getName()
                                + " to clobber them while they reload.",
                        "Nothing like heading in to battle with a plastic shield designed to bounce pebbles away and a "
                                + secondary.getName() + "!",
                        "Well, hope you're not scared of getting up close and personal!",
                        "The " + primary.getName() + " and " + secondary.getName() + " - what else could you want?",
                        "Guess it's clobbering time!",
                        "Charge!",
                        "You'll need to get creative with this one.",
                        "Turtle time!",
                        "At least you'll have your back protected while you slam cunts with the " + secondary.getName()
                };
            }
            else if(secondaryCategory == Weapon.CATEGORY.LAUNCHER) {
                return new String[]{
                        "Guess you're the designated U.A.V shooter downer!",
                        "Well, you're fucked once you fire the 2 rockets from the " + secondary.getName(),
                        "Keep your eyes on the ground for an actual secondary!",
                        "You'd probably just be better off blowing yourself up in spawn with the " + secondary.getName(),
                        "It's not all bad, " + secondary.getName() + " for blowing cunts up and then charge in with the "
                                + primary.getName() + " to clobber the others!",
                        secondary.getName().equalsIgnoreCase("jokr")
                                ? "The JOKR can act as a battering ram once you're out of ammo!"
                                : "at least got the " + secondary.getName() + " and not the JOKR.",
                        secondary.getName().equalsIgnoreCase("pila")
                                ? "A PILA? Good luck"
                                : "At least you got the " + secondary.getName() + " and not the PILA",
                        "Like the dumb cunt version of Captain America!",
                        "If you held the " + secondary.getName() + " vertically and one handed the "
                                + primary.getName() + ", you could pretend you're a medieval knight haha",
                        primary.getName() + " for protection and " + secondary.getName() + " for blowing cunts up.",
                        primary.getName() + " for slamming and " + secondary.getName()
                                + " for when they get a little too close to the plastic.",
                        "As long as you hit both rockets from the " + secondary.getName() + " and then land a good "
                                + loadout.getLethal().getName() + " you'll be all set!"
                };
            }
            return new String[]{
                    "At least your secondary is the " + secondary.getName() + " and not like sticks or some shit",
                    secondaryCategory == Weapon.CATEGORY.LMG
                            ? primary.getName() + " and " + secondary.getName() + ", a budget Juggernaut!"
                            : "The shield will protect you while you crouch around with the " + secondary.getName(),
                    "Do you think the " + primary.getName() + " is hard to hold when it gets shot?",
                    "Use the " + secondary.getName() + " to slam cunts up close, who needs the "
                            + primary.getName() + ".",
                    "Time to bash some cunts with a slab of plastic!",
                    "Fuck yeah, a plastic shield!"
            };
        }

        return new String[]{
                perks[1].getName() + " is red, " + perks[0].getName() + " is blue, you have the " + primary.getName()
                        + " and I'd rather use a shoe.",
                "Look on the bright side, the " + primary.getName() + " has "
                        + primary.getWeapon().getAttachments().length + " attachments and you got "
                        + primary.getAttachments().size() + " of them!",
                "The " + primary.getName() + " and " + secondary.getName() + " is the new meta, the "
                        + loadout.getTactical().getName() + " is what brings it all together.",
                "Nice! You can use the " + primary.getName() + " to engage and then flop out the "
                        + secondary.getName() + " when things get sticky!",
                perks[1].getName() + " and a " + loadout.getTactical().getName() + ", lucky!",
                "Sellotape the " + loadout.getTactical().getName() + " and " + loadout.getLethal().getName()
                        + " together and you'd get a "
                        + loadout.getTactical().getName().toUpperCase()
                        + loadout.getLethal().getName().toUpperCase() + "!",
                "I find that the " + primary.getName() + " works really well with the " + secondary.getName() + ".",
                secondaryCategory == Weapon.CATEGORY.MELEE
                        ? "If the " + primary.getName() + " doesn't kill the target, a good clobbering from the "
                        + secondary.getName() + " will surely do the trick!"
                        : "I'd rather have the swords over the " + secondary.getName() + ".",
                "Did you know that " + primary.getName() + " backwards is "
                        + new StringBuilder(primary.getName()).reverse() + "?"
        };
    }

    /**
     * Get a random item from the provided array
     *
     * @param items Array of items to choose random from
     * @return Random item from given array
     */
    private String getRandomItem(String[] items) {
        return items[new Random().nextInt(items.length)];
    }

    /**
     * Get a random analysis of the loadout
     *
     * @return Loadout analysis
     */
    public String getRandomAnalysis() {
        return getRandomItem(analysis);
    }
}

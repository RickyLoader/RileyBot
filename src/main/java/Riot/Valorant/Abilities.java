package Riot.Valorant;

import Riot.Valorant.Assets.Ability;

/**
 * Valorant agent abilities
 */
public class Abilities {
    private final Ability basicFirst, basicSecond, grenade, ultimate;

    /**
     * Create the set of abilities. Each agent has 2 basic abilities ({@code basicFirst} & {@code basicSecond}}),
     * a grenade (or signature) ability, and an ultimate ability.
     *
     * @param basicFirst  First basic ability - e.g Brimstone's Incendiary grenade launcher
     * @param basicSecond Second basic ability - e.g Brimstone's Sky smoke
     * @param grenade     Grenade/signature ability - e.g Brimstone's Stim beacon
     * @param ultimate    Ultimate ability - e.g Brimstone's Orbital strike
     */
    public Abilities(Ability basicFirst, Ability basicSecond, Ability grenade, Ability ultimate) {
        this.basicFirst = basicFirst;
        this.basicSecond = basicSecond;
        this.grenade = grenade;
        this.ultimate = ultimate;
    }

    /**
     * Get the first basic ability - e.g Brimstone's Incendiary grenade launcher
     *
     * @return First basic ability
     */
    public Ability getFirstBasicAbility() {
        return basicFirst;
    }

    /**
     * Get the second basic ability - e.g Brimstone's Sky smoke
     *
     * @return Second basic ability
     */
    public Ability getSecondBasicAbility() {
        return basicSecond;
    }

    /**
     * Get the grenade/signature ability - e.g Brimstone's Stim beacon
     *
     * @return Grenade/signature ability
     */
    public Ability getGrenadeAbility() {
        return grenade;
    }

    /**
     * Get the ultimate ability - e.g Brimstone's Orbital strike
     *
     * @return Ultimate ability
     */
    public Ability getUltimateAbility() {
        return ultimate;
    }
}

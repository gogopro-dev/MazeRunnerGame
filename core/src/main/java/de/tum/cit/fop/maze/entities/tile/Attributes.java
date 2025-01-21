package de.tum.cit.fop.maze.entities.tile;


public class Attributes {

    protected int immediateHealing;
    protected int immediateCoins;
    protected float damageBoost;
    protected float resistanceBoost;
    protected float vampirism;
    protected int ressurections;
    protected float speedBoost;

    public Attributes(int immediateHealing, int immediateCoins, float damageBoost, float resistanceBoost,
                      float vampirism, int ressurections, float speedBoost
    ) {
        this.immediateHealing = immediateHealing;
        this.immediateCoins =  immediateCoins;
        this.damageBoost = damageBoost;
        this.resistanceBoost = resistanceBoost;
        this.vampirism = vampirism;
        this.ressurections = ressurections;
        this.speedBoost = speedBoost;
    }

    public void sum(Attributes other) {
        this.immediateHealing += other.immediateHealing;
        this.damageBoost += other.damageBoost;
        this.immediateCoins += other.immediateCoins;
        this.resistanceBoost += other.resistanceBoost;
        this.vampirism += other.vampirism;
        this.ressurections += other.ressurections;
        this.speedBoost += other.speedBoost;
    }


}

package de.tum.cit.fop.maze.entities.tile;


/**
 * Represents a set of attributes used to define various properties for entities / players such as healing,
 * coins, damage boost, resistance boost, vampirism, speed boost, and resurrections.
 * These attributes can be manipulated by summing or subtracting their values.
 */
public class Attributes {
    protected int immediateHealing;
    protected int immediateCoins;
    protected int damageBoost;
    protected int resistanceBoost;
    protected float vampirism;
    protected int resurrections;
    protected float speedBoost;

    /**
     * Trivial constructor
     */
    public Attributes(int immediateHealing, int immediateCoins, int damageBoost, int resistanceBoost,
                      float vampirism, int resurrections, float speedBoost
    ) {
        this.immediateHealing = immediateHealing;
        this.immediateCoins =  immediateCoins;
        this.damageBoost = damageBoost;
        this.resistanceBoost = resistanceBoost;
        this.vampirism = vampirism;
        this.resurrections = resurrections;
        this.speedBoost = speedBoost;
    }

    /**
     * Sums Attributes
     *
     * @param other attribute to sum
     */
    public void sum(Attributes other) {
        this.immediateHealing += other.immediateHealing;
        this.damageBoost += other.damageBoost;
        this.immediateCoins += other.immediateCoins;
        this.resistanceBoost += other.resistanceBoost;
        this.vampirism += other.vampirism;
        this.resurrections += other.resurrections;
        this.speedBoost += other.speedBoost;
    }

    /**
     * Subtracts the attributes of another {@code Attributes} object from this {@code Attributes} object.
     *
     * @param other the other {@code Attributes} object whose values will be subtracted from this object
     */
    public void sub(Attributes other) {
        this.immediateHealing -= other.immediateHealing;
        this.damageBoost -= other.damageBoost;
        this.immediateCoins -= other.immediateCoins;
        this.resistanceBoost -= other.resistanceBoost;
        this.vampirism -= other.vampirism;
        this.resurrections -= other.resurrections;
        this.speedBoost -= other.speedBoost;
    }


    @Override
    public String toString() {
        return "Attributes{" +
            "immediateHealing=" + immediateHealing +
            ", immediateCoins=" + immediateCoins +
            ", damageBoost=" + damageBoost +
            ", resistanceBoost=" + resistanceBoost +
            ", vampiricHealing=" + vampirism +
            ", amountOfResurrections=" + resurrections +
            ", speedBoost=" + speedBoost +
            '}';
    }


    /**
     * Generates a descriptive string detailing the effects or attributes of an item based on its
     * properties such as immediate healing, coins, speed boost, damage boost, resistance boost,
     * vampirism, and number of resurrections.
     *
     * @return a string containing the description of the item, summarizing its effects based
     * on non-zero attribute values.
     */
    public String toItemDescription() {
        StringBuilder sb = new StringBuilder();
        if (this.immediateHealing > 0) {
            sb.append("Heals for ")
                .append(this.immediateHealing).append("HP\n");
        }
        if (this.immediateCoins > 0) {
            sb.append("Gives you ")
                .append(this.immediateCoins).append("Coins\n");
        }
        if (this.speedBoost > 0) {
            sb.append("Increases speed by ")
                .append(Math.round(this.speedBoost * 100)).append("% while running\n");
        }
        if (this.damageBoost > 0) {
            sb.append("Increases damage by ")
                .append(this.damageBoost).append("\n");
        }
        if (this.resistanceBoost > 0) {
            sb.append("Increases resistance by ")
                .append(this.resistanceBoost).append("\n");
        }
        if (this.vampirism > 0) {
            sb.append("Increases vampirism by ")
                .append(Math.round(this.vampirism * 100)).append("%\n");
        }
        if (this.resurrections > 0) {
            if (this.resurrections == 1) {
                sb.append("Gives you one resurrection\n");
            } else {
                sb.append("Gives you ").append(this.resurrections).append("resurrections\n");
            }
        }
        return sb.toString().stripTrailing();

    }

    public int getImmediateHealing() {
        return immediateHealing;
    }

    public int getImmediateCoins() {
        return immediateCoins;
    }

    public float getSpeedBoost() {
        return speedBoost;
    }

    public int getResurrections() {
        return resurrections;
    }

    public float getVampirism() {
        return vampirism;
    }

    public int getResistanceBoost() {
        return resistanceBoost;
    }

    public int getDamageBoost() {
        return damageBoost;
    }
}

package de.tum.cit.fop.maze.entities.tile;

public final class CollectableAttributes extends Attributes {

    public final Collectable.CollectableType type;
    public final String textureName;
    public final float frameDuration;
    public final float dropChance;

    public CollectableAttributes(
        Collectable.CollectableType type, String textureName, int immediateHealing, int immediateCoins,
        float dropChance, float damageBoost, float resistanceBoost, float vampirism, float speedBoost,
        int resurrections, float frameDuration
    ) {
        super(immediateHealing, immediateCoins, damageBoost, resistanceBoost,
            vampirism, resurrections, speedBoost);
        assert type != null;
        assert textureName != null;
        this.type = type;
        this.textureName = textureName;
        this.dropChance = dropChance;
        this.frameDuration = frameDuration;
    }

    public void sum(CollectableAttributes other) {
        super.sum(other);
    }
    public String toString() {
        return "CollectableAttributes{" +
            "type=" + type +
            ", textureName='" + textureName + '\'' +
            ", frameDuration=" + frameDuration +
            ", dropChance=" + dropChance +
            ", immediateHealing=" + immediateHealing +
            ", immediateCoins=" + immediateCoins +
            ", damageBoost=" + damageBoost +
            ", resistanceBoost=" + resistanceBoost +
            ", vampiricHealingPercent=" + vampirism +
            ", ressurections=" + ressurections +
            '}';
    }


}

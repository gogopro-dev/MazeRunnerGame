package de.tum.cit.fop.maze.entities.tile;

public final class CollectableAttributes extends Attributes {

    public final Collectable.CollectableType type;
    public final String textureName;
    public final float frameDuration;
    public final float dropChance;
    public final boolean lootContainerPool;
    public final boolean treasurePool;
    public final boolean shopPool;
    public final int shopPrice;

    public final String name;
    public final String description;

    public CollectableAttributes(
        Collectable.CollectableType type, String textureName, int immediateHealing, int immediateCoins,
        float dropChance, float damageBoost, float resistanceBoost, float vampirism, float speedBoost,
        int resurrections, float frameDuration, int shopPrice, String name, String description,
        boolean lootContainerPool, boolean treasurePool, boolean shopPool,
        boolean isConsumable
    ) {
        super(immediateHealing, immediateCoins, damageBoost, resistanceBoost,
            vampirism, resurrections, speedBoost, isConsumable);
        assert type != null;
        assert textureName != null;

        this.type = type;
        this.textureName = textureName;
        this.dropChance = dropChance;
        this.frameDuration = frameDuration;
        this.lootContainerPool = lootContainerPool;
        this.treasurePool = treasurePool;
        this.shopPool = shopPool;
        this.shopPrice = shopPrice;
        this.name = name;
        this.description = description;
    }

    public void sum(CollectableAttributes other) {
        super.sum(other);
    }

    @Override
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
            ", resurrections=" + resurrections +
            ", speedBoost=" + speedBoost +
            ", lootContainerPool=" + lootContainerPool +
            ", treasurePool=" + treasurePool +
            ", shopPool=" + shopPool +
            '}';
    }

    @Override
    public String toPrettyDescription() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name).append(":\n");
        }
        if (description != null) {
            sb.append(description).append("\n");
        }
        sb.append(super.toPrettyDescription());
        return sb.toString();
    }

}

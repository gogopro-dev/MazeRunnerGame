package de.tum.cit.fop.maze.entities.tile;


public class Attributes {

    protected int immediateHealing;
    protected int immediateCoins;
    protected float damageBoost;
    protected float resistanceBoost;
    protected float vampirism;
    protected int resurrections;
    protected float speedBoost;
    /*Итак, мои предложения к коллектблам:
    Сердечки = выпадают с каким то шансом с убитых мобов. Восстанавливают 1(?) фулл сердце
    Монетки = выпадают с мобов в количестве от 1 до 4(рандом). За них можно купить бафы в магазе
    "Монетка дэмэджа" = могут выпасть с бочки с шансом 1%. +15% к общему дамагу по врагам
    "Монетка Защиты" = могут выпасть с бочки с шансом 1%. -15% к получаемому урону от врагов
    Магазин:
    "Амулет воскресения": если вы умерли и есть амулет, то вы воскресните с фулл хп и станете неуязвимым на 3 секунды. После испольщования пропадает. Стоимость: 75 монет
    "Амулет вампира": после каждого убитого врага есть шанс 10% восстановить себе здоровье (рандом от 1 сердца до 3, включая половинчатые значения). Стоимость 100 монет
    "Сапоги скорохода": Увеличивает вашу скорость на 10%. Стоимость: 50 монет */

    public Attributes(int immediateHealing, int immediateCoins, float damageBoost, float resistanceBoost,
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

    public void sum(Attributes other) {
        this.immediateHealing += other.immediateHealing;
        this.damageBoost += other.damageBoost;
        this.immediateCoins += other.immediateCoins;
        this.resistanceBoost += other.resistanceBoost;
        this.vampirism += other.vampirism;
        this.resurrections += other.resurrections;
        this.speedBoost += other.speedBoost;
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

    // Ingore immediately applied ones since
    public String toPrettyDescription() {
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
                .append(Math.round(this.speedBoost * 100)).append("%\n");
        }
        if (this.damageBoost > 0) {
            sb.append("Increases damage by ")
                .append(Math.round(this.damageBoost * 100)).append("%\n");
        }
        if (this.resistanceBoost > 0) {
            sb.append("Increases resistance by ")
                .append(Math.round(this.resistanceBoost * 100)).append("%\n");
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

    public float getResistanceBoost() {
        return resistanceBoost;
    }

    public float getDamageBoost() {
        return damageBoost;
    }
}

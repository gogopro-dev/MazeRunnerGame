package de.tum.cit.fop.maze.entities.tile;


public class Attributes {

    protected int immediateHealing;
    protected int immediateCoins;
    protected float damageBoost;
    protected float resistanceBoost;
    protected float vampirism;
    protected int ressurections;
    protected float speedBoost;
    protected boolean isConsumable;
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
                      float vampirism, int ressurections, float speedBoost, boolean isConsumable
    ) {
        this.immediateHealing = immediateHealing;
        this.immediateCoins =  immediateCoins;
        this.damageBoost = damageBoost;
        this.resistanceBoost = resistanceBoost;
        this.vampirism = vampirism;
        this.ressurections = ressurections;
        this.speedBoost = speedBoost;
        this.isConsumable = isConsumable;
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

    @Override
    public String toString() {
        return "Attributes{" +
            "immediateHealing=" + immediateHealing +
            ", immediateCoins=" + immediateCoins +
            ", damageBoost=" + damageBoost +
            ", resistanceBoost=" + resistanceBoost +
            ", vampiricHealing=" + vampirism +
            ", amountOfResurrections=" + ressurections +
            ", speedBoost=" + speedBoost +
            '}';
    }
}

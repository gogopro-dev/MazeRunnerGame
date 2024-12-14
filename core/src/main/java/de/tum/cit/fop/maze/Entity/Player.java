package de.tum.cit.fop.maze.Entity;

public class Player {
    int hp;
    int stamina;
    int fullHP;
    public Player(){
        hp = 14;
        fullHP = hp;
        stamina = 3;
    }

    public boolean isDead(){
        return hp <= 0;
    }

    public int getFullHP() {
        return fullHP;
    }

    public void setFullHP(int fullHP) {
        this.fullHP = fullHP;
    }

    public void takeDamage(int damage){
        hp -= damage;

    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }
}

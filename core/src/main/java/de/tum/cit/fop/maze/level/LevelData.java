package de.tum.cit.fop.maze.level;

public class LevelData {
    private int score = 1000;
    private float playtime = 0f;


    public float getPlaytime() {
        return playtime;
    }

    public int getScore() {
        return score - (int) (playtime) * 10;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void addPlaytime(float playtime) {
        this.playtime += playtime;
    }
}

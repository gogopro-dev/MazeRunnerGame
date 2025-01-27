package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import java.util.Locale;

public class TimeAndScore {
    private Table timeAndScoreTable = new Table();
    private Label time;
    private Label scoreLabel;
    private float currentScore;
    private float elapsedTime = 0;
    private Stage stage;

    public TimeAndScore( int currentScore,  Label.LabelStyle labelStyle, Stage stage) {

        this.currentScore = currentScore;
        this.stage = stage;
        time = new Label(formatedTime(elapsedTime), labelStyle);
        scoreLabel = new Label(formatedScore(currentScore), labelStyle);

        timeAndScoreTable.add(time);
        timeAndScoreTable.row();
        timeAndScoreTable.add(scoreLabel);
        updateLabelTablePosition();
        stage.addActor(timeAndScoreTable);
    }

    public void updateLabelTablePosition() {
        timeAndScoreTable.setSize(time.getWidth(),
                time.getHeight() + scoreLabel.getHeight());
        float labelPadding = 15f;
        timeAndScoreTable.setPosition((stage.getViewport().getWorldWidth() - timeAndScoreTable.getWidth()) / 2,
                stage.getViewport().getWorldHeight() - timeAndScoreTable.getHeight() - labelPadding);
        timeAndScoreTable.align(Align.center);
    }

    public String formatedTime(float elapsedTime) {
        long seconds = (long) elapsedTime;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format(
                Locale.getDefault(), "Time: %02d:%02d:%02d", hours, minutes % 60, seconds % 60
        );
    }

    public String formatedScore(int score) {
        return String.format(
                Locale.getDefault(), "Score: %06d", score
        );
    }

    private void addScore(int val) {
        currentScore += val;
    }

    public void updateLabels(float deltaTime) {
        elapsedTime += deltaTime;
        time.setText(formatedTime(elapsedTime));
        currentScore = currentScore - deltaTime;
        scoreLabel.setText(formatedScore((int) currentScore));
        System.out.println(currentScore + " " + elapsedTime);
    }

    public int getElapsedTime() {
        return (int) elapsedTime;
    }
    public int getCurrentScore() {
        return (int) currentScore;
    }
}

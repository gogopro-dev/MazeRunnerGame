package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.level.LevelData;

import java.util.Locale;

public class TimeAndScore {
    private final Table timeAndScoreTable = new Table();
    private final Label time;
    private final Label scoreLabel;
    private final Stage stage;
    private final LevelData data;

    public TimeAndScore(LevelData data, Label.LabelStyle labelStyle, Stage stage) {
        this.data = data;
        this.stage = stage;
        time = new Label(formatedTime(), labelStyle);
        scoreLabel = new Label(formatedScore(), labelStyle);

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

    public String formatedTime() {
        long seconds = (long) data.getPlaytime();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format(
            Locale.getDefault(), "Time: %02d:%02d:%02d", hours, minutes % 60, seconds % 60
        );
    }

    public String formatedScore() {
        return String.format(
            Locale.getDefault(), "Score: %06d", data.getScore()
        );
    }

    public void updateLabels() {
        time.setText(formatedTime());
        scoreLabel.setText(formatedScore());
    }

    public void dispose() {
        timeAndScoreTable.clear();
        timeAndScoreTable.remove();
    }
}

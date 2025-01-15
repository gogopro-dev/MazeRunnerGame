package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class TxtLabel extends Label{
    private String text;
    private float x;
    private float y;
    public TxtLabel(Stage stage, String text, float x, float y) {
        super(text, new LabelStyle());
        LabelStyle style = new LabelStyle();
        style.font = new BitmapFont(Gdx.files.local("font/YosterIslandRegular-VqMe.ttf")); //// Load custom font
        //TODO: replace it with a different Background
        style.background = commonFunctions.getColoredDrawable(100, 100, new Color(0, 0, 0, 0));
        this.setStyle(style);
    }
}

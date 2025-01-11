package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.google.gson.Gson;

import java.io.InputStreamReader;

public class Collectable {
    private record attributes (int speed, int stamina, int maxHealth, int damage) {
    }

    private attributes attributes;
    private int id;
    private String textureName;
    transient Stage stage;
    float duration;

    //TODO TextureAtlas
    TextureAtlas textureAtlas;

    Texture tempTexture;

    public Collectable(Stage stage) {
        this.stage = stage;
        duration = 0f;
    }
    //TODO: relocate this method to factory class
    /**
     * Create a new collectable from json.
     * @param json The json input stream
     * @param stage The stage to add the collectable to
     */
    public Collectable fromJson(InputStreamReader json, Stage stage) {
        Gson gson = new Gson();
        Collectable collectable = gson.fromJson(json, Collectable.class);
        collectable.setStage(stage);
        return collectable;
    }

    /**
     * Create a new collectable at the given position.
     * @param x The x-coordinate of the collectable in pixels
     * @param y The y-coordinate of the collectable in pixels
     */
    public void create(int x, int y) {
        textureAtlas = new TextureAtlas(Gdx.files.internal("temporary\\collectables\\buffs.atlas"));


        Image collectableImage = new Image(textureAtlas.findRegion(textureName));
        collectableImage.setPosition(x, y);
        stage.addActor(collectableImage);
    }


    public void render() {
        //TODO Animation
    }


    public void dispose() {
        tempTexture.dispose();
    }

    @Override
    public String toString() {
        return "Collectable{" + "id=" + id + ", textureName='" + textureName + ", attributes=" + attributes.toString() + '\'' + '}';
    }
    public String getTextureName() {
        return textureName;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

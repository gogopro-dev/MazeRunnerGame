package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.Contact;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.level.LevelScreen;

public class Collectable extends TileEntity {

    public record AffectedStats (int speed, int stamina, int maxHealth, int damage) {
    }

    private AffectedStats affectedStats;
    private Animation<TextureRegion> idleAnimation;
    //TODO: add pickupAnimation
    private Animation<TextureRegion> pickupAnimation;
    private boolean pickedUp;
    private int id;
    private String textureName;
    private SpriteBatch batch;
    private float frameDuration = 1f;
    private float elapsedTime;
    private float pickupElapsedTime;

    public Collectable(String idleAnimName, String pickupAnimName) {
        super(1, 1);
        batch = LevelScreen.getInstance().batch;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.local("temporary/collectables/collectables.atlas"));
        idleAnimation = new Animation<>(frameDuration, atlas.findRegions(idleAnimName));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
//        pickupAnimation = new Animation<>(frameDuration, atlas.findRegions(idleAnimName));
    }


    public Collectable fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Collectable.class);

    }


    @Override
    public void render(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        TextureRegion currentFrame;
        elapsedTime+=delta;
        if (!pickedUp){
            currentFrame = idleAnimation.getKeyFrame(elapsedTime, true);
            batch.draw(currentFrame, getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
                    getSpriteDrawWidth(), getSpriteDrawHeight());
        }
        // TODO after creating TextureAtlas for pickup animation delete else and uncomment code below (not tested yet)
        else{
            //TODO: despawn collectable after picking it up
            dispose();
        }
//        else {
//            currentFrame = pickupAnimation.getKeyFrame(pickupElapsedTime);
//            pickupElapsedTime += delta;
//            if(pickupAnimation.isAnimationFinished(pickupElapsedTime)){
//                dispose();
//            }
//        }
    }

    public void dispose() {
    }

    @Override
    public String toString() {
        return "Picked up";
    }
    @Override
    public void onPlayerStartContact(Contact c) {
        super.onPlayerStartContact(c);
        if (!pickedUp){
            pickedUp = true;
            pickupElapsedTime = 0f;
        }

    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }


    public SpriteBatch getBatch() {
        return batch;
    }

}

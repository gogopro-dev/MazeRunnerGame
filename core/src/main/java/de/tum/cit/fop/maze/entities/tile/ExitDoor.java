package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.List;

import static de.tum.cit.fop.maze.Globals.*;

/**
 * ExitDoor class represents the exit door in the game.
 * The door is initially locked and can be opened by the player if he has a key.
 * The door will open and the player can exit the level if the player has a key and interacts with the door.
 */
public class ExitDoor extends TileEntity {
    private boolean isOpen = false;
    /**
     * Creates a new Exit door.
     */

    private transient Animation<TextureRegion> doorOpeningAnimation;
    private transient float elapsedTime = 0;
    private transient TextureRegion texture;
    private transient FixtureDef sensorFixtureDef;

    public ExitDoor() {
        super(3, 3, new BodyDef(), new FixtureDef());

        /// Create the body definition and fixture definition for the door
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            CELL_SIZE_METERS * 1.5f + HITBOX_SAFETY_GAP,
            HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS - CELL_SIZE_METERS * 1.6f
        );
        fixtureDef.shape = shape;
        fixtureDef.isSensor = false;
        fixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        fixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
        init();
    }

        /// Load the door texture and the door opening animation

    public void init() {
        TextureAtlas atlas = Assets.getInstance().getAssetManager()
            .get("assets/anim/tileEntities/tile_entities.atlas", TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> doorFrames =
            atlas.findRegions("door");

        doorOpeningAnimation = new Animation<>(0.175f, doorFrames);
        doorOpeningAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        texture = atlas.findRegion("door_locked");
    }

    @Override
    public void render(float delta) {
        /// if the door is not open, render closed door
        if (!isOpen) {
            batch.draw(
                texture, getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
                getSpriteDrawWidth(), getSpriteDrawHeight()
            );
            return;
        }
        /// if the door is open, render the open door
        if (doorOpeningAnimation.isAnimationFinished(elapsedTime)) {
            batch.draw(doorOpeningAnimation.getKeyFrame(2.8f),
                getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
                getSpriteDrawWidth(), getSpriteDrawHeight());
            return;
        }

        elapsedTime += delta;
        batch.draw(doorOpeningAnimation.getKeyFrame(elapsedTime),
            getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
            getSpriteDrawWidth(), getSpriteDrawHeight()
        );
    }

    @Override
    public void contactTick(float delta) {
        super.contactTick(delta);

        if (!isOpen && LevelScreen.getInstance().player.hasKey()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                LevelScreen.getInstance().hud.deleteDescription();
                /// Delete the previous sensor fixture
                Fixture sensorFixture = body.getFixtureList().get(1);
                body.destroyFixture(sensorFixture);

                /// Create a new sensor fixture with a smaller hitbox
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(
                    (CELL_SIZE_METERS * 1.5f + HITBOX_SAFETY_GAP) / 3f,
                    (CELL_SIZE_METERS * 1.5f + HITBOX_SAFETY_GAP) / 1.5f
                );
                FixtureDef sensorFixtureDef = new FixtureDef();
                sensorFixtureDef.shape = shape;
                sensorFixtureDef.isSensor = true;
                sensorFixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
                sensorFixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
                body.createFixture(sensorFixtureDef);

                /// Open the door
                isOpen = true;
                return;
            }

        }

        /// End the game if the door opening animation is finished
        if (doorOpeningAnimation.isAnimationFinished(elapsedTime)){
            LevelScreen.getInstance().endGame(true);
        }
    }

    @Override
    protected void onPlayerStartContact(Contact c) {
        if (isOpen){
            return;
        }
        /// If the player does not have a key, create a message to the player
        if (!LevelScreen.getInstance().player.hasKey()){
            LevelScreen.getInstance().hud.setItemDescription(
                "You need a key to open the door"
            );
            return;
        }
        LevelScreen.getInstance().hud.setItemDescription(
            "Press Enter to open the door"
        );
    }

    @Override
    protected void onPlayerEndContact(Contact c) {
        LevelScreen.getInstance().hud.deleteDescription();
    }

    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        sensorFixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            CELL_SIZE_METERS * 1.5f + HITBOX_SAFETY_GAP,
            CELL_SIZE_METERS * 1.5f + HITBOX_SAFETY_GAP
        );
        sensorFixtureDef.shape = shape;
        sensorFixtureDef.isSensor = true;
        sensorFixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        sensorFixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
        body.createFixture(sensorFixtureDef);
    }

}

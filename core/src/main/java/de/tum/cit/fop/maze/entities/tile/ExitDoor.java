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
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

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
    private Direction direction;
    private transient Animation<TextureRegion> doorOpeningAnimation;
    private transient float openElapsedTime = 0;
    private transient TextureRegion texture;
    private transient Body wallBody;
    private transient Fixture promptFixture;
    private transient MASound openingSound;


    private ExitDoor() {
        super(3, 3, new BodyDef(), new FixtureDef());
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0, 0);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        fixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
    }

    public ExitDoor(Direction direction) {
        this();
        this.direction = direction;
        init();
    }

    /// Loads the door texture and the door opening animation
    public void init() {
        TextureAtlas atlas = Assets.getInstance().getAssetManager()
            .get("assets/anim/tileEntities/tile_entities.atlas", TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> doorFrames =
            switch (direction) {
                case RIGHT, LEFT -> atlas.findRegions("door_side");
                case UP -> atlas.findRegions("door_front");
                case DOWN -> atlas.findRegions("door_back");
            };


        doorOpeningAnimation = new Animation<>(0.175f, doorFrames);
        doorOpeningAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        texture = switch (direction) {
            case LEFT, RIGHT -> atlas.findRegion("door_side_locked");
            case UP -> atlas.findRegion("door_front_locked");
            case DOWN -> atlas.findRegion("door_back_locked");
        };

        this.openingSound = Assets.getInstance().getSound("exit_open");
        this.openingSound.setVolume(1.25f);
        this.openingSound.setLooping(false);
    }

    @Override
    public void render(float delta) {
        if (isOpen) openElapsedTime += delta;
        /// if the door is not open, render closed door
        float posX = getSpriteDrawPosition().x();
        float posY = getSpriteDrawPosition().y();
        float width = getSpriteDrawWidth();
        float height = getSpriteDrawHeight();
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            height *= 2f;
        }

        TextureRegion texture = !isOpen ? this.texture : doorOpeningAnimation.getKeyFrame(openElapsedTime);
        if (this.direction == Direction.RIGHT) texture.flip(true, false);
        batch.draw(texture, posX, posY, width, height);
        if (this.direction == Direction.RIGHT) texture.flip(true, false);
    }

    @Override
    public void contactTick(float delta) {
        super.contactTick(delta);

        if (!isOpen && LevelScreen.getInstance().player.hasKey()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                LevelScreen.getInstance().hud.deleteDescription();
                /// Delete the prompt fixture
                body.destroyFixture(promptFixture);
                body.getWorld().destroyBody(wallBody);
                /// Open the door
                isOpen = true;
                this.openingSound.setPosition(getPosition().x(), getPosition().y(), 0);
                this.openingSound.play();
                spawnExitTrigger();
                return;
            }

        }

        /// End the game if the door is open
        if (isOpen) {
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

    /**
     * Spawns an exit trigger for the ExitDoor entity. This method is responsible
     * for handling logic specific to setting up the exit trigger based on the
     * direction of the door.
     */
    private void spawnExitTrigger() {
        float x = getPosition().x();
        float y = getPosition().y();
        FixtureDef exitTriggerDef = new FixtureDef();
        PolygonShape exitTriggerShape = new PolygonShape();
        BodyDef exitTriggerBodyDef = new BodyDef();
        exitTriggerBodyDef.type = BodyDef.BodyType.StaticBody;
        exitTriggerDef.shape = exitTriggerShape;
        exitTriggerDef.isSensor = true;
        exitTriggerDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        exitTriggerDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
        if (this.direction == Direction.UP) {
            exitTriggerBodyDef.position.set(x, y + CELL_SIZE_METERS * 1.5f);
            exitTriggerShape.setAsBox(CELL_SIZE_METERS * 3.3f, CELL_SIZE_METERS / 2f);
        } else if (this.direction == Direction.LEFT || this.direction == Direction.RIGHT) {
            exitTriggerBodyDef.position.set(
                x + (direction == Direction.RIGHT ? CELL_SIZE_METERS : -CELL_SIZE_METERS),
                y + CELL_SIZE_METERS * 1.5f);
            exitTriggerShape.setAsBox(CELL_SIZE_METERS / 6f, CELL_SIZE_METERS * 2);
        } else {
            exitTriggerBodyDef.position.set(x, y - CELL_SIZE_METERS * 1.5f);
            exitTriggerShape.setAsBox(CELL_SIZE_METERS * 3.3f, CELL_SIZE_METERS / 2f);
        }
        ///  No need to delete ever as the game ends after this has been triggered;
        Body temp = body.getWorld().createBody(exitTriggerBodyDef);
        temp.createFixture(exitTriggerDef);
        temp.setUserData(this);
        exitTriggerShape.dispose();
    }

    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        /// Not worth rewriting tile entity code, so this lil workaround is pretty much reasonable
        body.destroyFixture(body.getFixtureList().first());


        /// Spawn prompt sensor fixture
        CircleShape promptShape = new CircleShape();
        promptShape.setRadius(CELL_SIZE_METERS * 3f);
        FixtureDef promptFixtureDef = new FixtureDef();
        promptFixtureDef.shape = promptShape;
        promptFixtureDef.isSensor = true;
        promptFixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        promptFixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
        promptFixture = body.createFixture(promptFixtureDef);
        promptShape.dispose();

        FixtureDef hitboxDef = new FixtureDef();
        PolygonShape hitboxShape = new PolygonShape();
        BodyDef hitboxBodyDef = new BodyDef();
        hitboxDef.shape = hitboxShape;
        hitboxDef.isSensor = false;
        hitboxDef.filter.categoryBits = BodyBits.WALL;
        hitboxDef.filter.maskBits = BodyBits.WALL_MASK;
        /// Spawn hitbox body for the upward direction, it will be destroyed as soon as the door opens
        if (direction == Direction.UP) {
            hitboxBodyDef.position.set(x, y + CELL_SIZE_METERS + CELL_SIZE_METERS / 2f);
            hitboxShape.setAsBox(CELL_SIZE_METERS * 3.3f, CELL_SIZE_METERS * 1.5f);
        } else if (direction == Direction.RIGHT || direction == Direction.LEFT) {
            hitboxBodyDef.position.set(
                x + (direction == Direction.RIGHT ? CELL_SIZE_METERS / 2f : -CELL_SIZE_METERS / 2f),
                y);
            hitboxShape.setAsBox(CELL_SIZE_METERS / 2f, CELL_SIZE_METERS * 2);
        } else {
            hitboxBodyDef.position.set(x, y);
            hitboxShape.setAsBox(CELL_SIZE_METERS * 3.3f, CELL_SIZE_METERS * 1.5f);

            /// Spawn permanent this texture specific hitboxes
            BodyDef textureHitboxBodyDef = new BodyDef();
            textureHitboxBodyDef.type = BodyDef.BodyType.StaticBody;
            textureHitboxBodyDef.fixedRotation = true;
            PolygonShape textureHitboxShape = new PolygonShape();
            FixtureDef textureHitboxDef = new FixtureDef();
            textureHitboxShape.setAsBox(CELL_SIZE_METERS / 2.1f, CELL_SIZE_METERS * 1.5f);
            textureHitboxDef.shape = textureHitboxShape;
            textureHitboxDef.isSensor = false;
            textureHitboxDef.filter.categoryBits = BodyBits.WALL;
            textureHitboxDef.filter.maskBits = BodyBits.WALL_MASK;

            textureHitboxBodyDef.position.set(x - CELL_SIZE_METERS * 1.5f, y);
            LevelScreen.getInstance().world.createBody(textureHitboxBodyDef).createFixture(textureHitboxDef);
            textureHitboxBodyDef.position.set(x + CELL_SIZE_METERS * 1.5f, y);
            LevelScreen.getInstance().world.createBody(textureHitboxBodyDef).createFixture(textureHitboxDef);
            textureHitboxShape.dispose();
        }
        wallBody = LevelScreen.getInstance().world.createBody(hitboxBodyDef);
        wallBody.createFixture(hitboxDef);
        hitboxShape.dispose();
    }

}

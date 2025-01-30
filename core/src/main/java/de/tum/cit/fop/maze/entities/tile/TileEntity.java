package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.gson.GSONPostRestorable;
import de.tum.cit.fop.maze.level.LevelScreen;

import static de.tum.cit.fop.maze.Globals.TRAP_SAFETY_PADDING;

/**
 * This class represents a tile entity in the game.
 * Tile entities are static objects in the game that can be interacted with by the player.
 */
public abstract class TileEntity implements Disposable, GSONPostRestorable {

    public boolean toDestroy = false;
    protected int width;
    protected int height;
    protected boolean isOnPlayer = false;
    protected AbsolutePoint savedPosition;

    protected transient Body body;
    protected transient SpriteBatch batch;
    protected final transient OrthographicCamera camera;
    protected transient BodyDef bodyDef;
    protected transient FixtureDef fixtureDef;
    private transient boolean initialized = false;

    /// Queues up tile entity for deletion

    private TileEntity() {
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
        this.batch = LevelScreen.getInstance().batch;
        this.camera = LevelScreen.getInstance().camera;
    }

    public TileEntity(int width, int height) {
        this();
        this.width = width;
        this.height = height;
        createBody();
    }

    public TileEntity(int width, int height, BodyDef bodyDef, FixtureDef fixtureDef) {
        this.width = width;
        this.height = height;
        this.batch = LevelScreen.getInstance().batch;
        this.camera = LevelScreen.getInstance().camera;
        this.bodyDef = bodyDef;
        this.fixtureDef = fixtureDef;
    }

    /**
     * <p>Creates and initializes the physical components of the tile entity used in a physics simulation.</p>
     *
     * <p>This method sets up the body definition, fixture definition, and shape for the entity.
     * The body is defined as a dynamic body with fixed rotation to restrict its movement. The shape
     * of the entity is defined as a polygonal box with dimensions calculated based on the tile's width
     * and height, adjusted by @{code TRAP_SAFETY_PADDING}.</p>
     *
     * <p>The fixture is specified as a sensor, preventing physical collisions while still detecting
     * overlaps. Additionally, the method configures the collision categories and masks to enable
     * specific collision interactions for the tile entity.</p>
     */
    private void createBody() {
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            width * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING,
            height * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING
        );
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        fixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
    }

    /**
     * Calculates the position where the sprite for this tile entity should be drawn.
     * The position is adjusted based on the size of the tile entity and the global cell size in meters.
     * If the tile entity does not have a physical body, a default position of (0, 0) is returned.
     *
     * @return An {@link AbsolutePoint} representing the coordinates where the sprite should be drawn in meters.
     */
    AbsolutePoint getSpriteDrawPosition() {
        if (body == null) {
            return new AbsolutePoint(0, 0);
        }
        return new AbsolutePoint(
            body.getPosition().x - width * Globals.CELL_SIZE_METERS / 2,
            body.getPosition().y - height * Globals.CELL_SIZE_METERS / 2
        );
    }

    float getSpriteDrawHeight() {
        return height * Globals.CELL_SIZE_METERS;
    }

    float getSpriteDrawWidth() {
        return width * Globals.CELL_SIZE_METERS;
    }

    /**
     * Spawns the tile entity at the specified coordinates in the world.
     * This method initializes the physical body of the entity in the physics simulation.
     *
     * @param x The x-coordinate where the tile entity is to be spawned.
     * @param y The y-coordinate where the tile entity is to be spawned.
     * @throws IllegalStateException if the entity has already been spawned.
     */
    public void spawn(float x, float y) {
        World world = LevelScreen.getInstance().world;
        if (body != null) {
            throw new IllegalStateException("Trap");
        }

        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        fixtureDef.shape.dispose();
        body.setUserData(this);
    }

    /**
     * Handles the periodic update logic required when this tile entity is in contact with another entity.
     * This method is called on every tick while the contact persists and is used to perform any
     * necessary actions or updates during this state.
     *
     * @param delta The time elapsed since the last call to this method, in seconds.
     */
    public void contactTick(float delta) {
    }

    public final void startContact(Contact c) {
        this.isOnPlayer = true;
        onPlayerStartContact(c);
    }

    protected void onPlayerStartContact(Contact c) {
    }


    public final void endContact(Contact c) {
        this.isOnPlayer = false;
        onPlayerEndContact(c);
    }

    protected void onPlayerEndContact(Contact c) {
    }

    public AbsolutePoint getPosition() {
        return new AbsolutePoint(body.getPosition().x, body.getPosition().y);
    }

    public void dispose() {
        if (body != null) {
            body.getWorld().destroyBody(body);
        }
    }

    public boolean isOnPlayer() {
        return isOnPlayer;
    }

    public final void restore() {
        createBody();
        init();
    }

    protected abstract void init();

    public AbsolutePoint getSavedPosition() {
        return savedPosition;
    }


    public void initialize() {
        if (initialized) {
            return;
        }
        this.initialized = true;
        init();
    }

    abstract void render(float delta);

    public final void renderTileEntity(float delta) {
        this.savedPosition = getPosition();
        render(delta);
    }

    public Body getBody() {
        return body;
    }

    public boolean isSpawned() {
        return body != null;
    }
}

package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.level.LevelScreen;

import static de.tum.cit.fop.maze.Globals.TRAP_SAFETY_PADDING;

/**
 * This class represents a tile entity in the game.
 * Tile entities are static objects in the game that can be interacted with by the player.
 */
public abstract class TileEntity implements Disposable {
    protected transient Body body;
    protected transient int width;
    protected transient int height;
    protected transient boolean isOnPlayer = false;
    protected transient final SpriteBatch batch;
    protected transient final OrthographicCamera camera;
    protected transient final BodyDef bodyDef;
    protected transient final FixtureDef fixtureDef;
    /// Queues up tile entity for deletion
    public boolean toDestroy = false;


    public TileEntity(int width, int height) {
        this.width = width;
        this.height = height;
        this.batch = LevelScreen.getInstance().batch;
        this.camera = LevelScreen.getInstance().camera;
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

    public TileEntity(int width, int height, BodyDef bodyDef, FixtureDef fixtureDef) {
        this.width = width;
        this.height = height;
        this.batch = LevelScreen.getInstance().batch;
        this.camera = LevelScreen.getInstance().camera;
        this.bodyDef = bodyDef;
        this.fixtureDef = fixtureDef;
    }

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


    protected void spawn(float x, float y) {
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

    public void contactTick(float delta) {
    }

    public void onPlayerStartContact(Contact c) {
        this.isOnPlayer = true;
    }

    public boolean isOnPlayer() {
        return isOnPlayer;
    }

    public void onPlayerEndContact(Contact c) {
        this.isOnPlayer = false;
    }

    public Body getBody() {
        return body;
    }

    public AbsolutePoint getPosition() {
        return new AbsolutePoint(body.getPosition().x, body.getPosition().y);
    }

    public void dispose() {
        if (body != null) {
            body.getWorld().destroyBody(body);
        }
    }

    abstract public void render(float delta);
}

package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.level.LevelScreen;

import static de.tum.cit.fop.maze.Globals.TRAP_SAFETY_PADDING;

/**
 * This class represents a tile entity in the game.
 * Tile entities are static objects in the game that can be interacted with by the player.
 */
public abstract class TileEntity {
    private Body body;
    protected int width;
    protected int height;
    protected boolean isActivated = true;

    public TileEntity(int width, int height) {
        this.width = width;
        this.height = height;
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
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            width * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING,
            height * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING
        );
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        fixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
        body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setUserData(this);
        shape.dispose();
    }

    public void contactTick(float delta) {
    }

    public void onPlayerStartContact(Contact c) {
        this.isActivated = true;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void onPlayerEndContact(Contact c) {
        this.isActivated = false;
    }

    public Body getBody() {
        return body;
    }

    abstract public void render(float delta);
}

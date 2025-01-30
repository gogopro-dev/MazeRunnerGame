package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import de.tum.cit.fop.maze.essentials.BodyBits;
import de.tum.cit.fop.maze.essentials.Globals;

import static de.tum.cit.fop.maze.essentials.Globals.TRAP_SAFETY_PADDING;

/**
 * The Projectile class represents a dynamic entity in the game world that moves
 * with a specified speed and direction. It extends the TileEntity class and is
 * designed to interact with other entities in the game upon contact. It does
 */
public class Projectile extends TileEntity {

    private final float speed;
    private final boolean isFacingRight;
    private boolean foundHit = false;

    public Projectile(
        int width, int height, float speed, boolean isFacingRight
    ) {
        super(width, height, new BodyDef(), new FixtureDef());
        this.speed = speed;
        this.isFacingRight = isFacingRight;
        this.createBody();

    }

    private void createBody() {
        this.bodyDef = new BodyDef();
        this.fixtureDef = new FixtureDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            width * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING,
            height * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING
        );
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = BodyBits.PROJECTILE;
        fixtureDef.filter.maskBits = BodyBits.PROJECTILE_MASK;
    }

    @Override
    protected void init() {
        this.createBody();
    }

    @Override
    void render(float delta) {
    }


    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        body.setLinearVelocity(isFacingRight ? speed : -speed, 0);
        body.setUserData(this);
    }

    void onContact() {
        this.foundHit = true;
        body.setLinearVelocity(Vector2.Zero);
    }

    public boolean isFoundHit() {
        return foundHit;
    }
}

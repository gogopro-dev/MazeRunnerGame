package de.tum.cit.fop.maze.entities.tile;

import box2dLight.PointLight;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.level.LevelScreen;

import static de.tum.cit.fop.maze.Globals.*;

public class ExitDoor extends TileEntity {
    public ExitDoor() {
        super(3, 3, new BodyDef(), new FixtureDef());
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            CELL_SIZE_METERS * 1.5f + HITBOX_SAFETY_GAP,
            HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS * CELL_SIZE_METERS / 2 - CELL_SIZE_METERS / 4
        );
        fixtureDef.shape = shape;
        fixtureDef.isSensor = false;
        fixtureDef.filter.categoryBits = BodyBits.WALL_TRANSPARENT;
        fixtureDef.filter.maskBits = BodyBits.WALL_TRANSPARENT_MASK;

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void onPlayerStartContact(Contact c) {
        // todo remove collision if player has a key
    }

    @Override
    protected void spawn(float x, float y) {
        super.spawn(x, y + HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS * CELL_SIZE_METERS / 2);
    }

}

package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.physics.box2d.Contact;

public class ExitDoor extends TileEntity {
    public ExitDoor() {
        super(3, 3);
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void onPlayerStartContact(Contact c) {
        // todo remove collision if player has a key
    }
}

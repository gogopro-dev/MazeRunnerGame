package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.fop.maze.entities.Player;

public class TileEntityContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Body a = contact.getFixtureA().getBody();
        Body b = contact.getFixtureB().getBody();
        if (a.getUserData() == null || b.getUserData() == null) {
            return;
        }
        if (a.getUserData() instanceof TileEntity && b.getUserData() instanceof Player) {
            ((TileEntity) a.getUserData()).startContact(contact);
        } else if (a.getUserData() instanceof Player && b.getUserData() instanceof TileEntity) {
            ((TileEntity) b.getUserData()).startContact(contact);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Body a = contact.getFixtureA().getBody();
        Body b = contact.getFixtureB().getBody();
        if (a.getUserData() == null || b.getUserData() == null) {
            return;
        }
        if (a.getUserData() instanceof TileEntity && b.getUserData() instanceof TileEntity) {
            return;
        }
        if (a.getUserData() instanceof TileEntity && b.getUserData() instanceof Player) {
            ((TileEntity) a.getUserData()).endContact(contact);
        } else if (a.getUserData() instanceof Player && b.getUserData() instanceof TileEntity) {
            ((TileEntity) b.getUserData()).endContact(contact);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}

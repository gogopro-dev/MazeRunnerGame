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
        System.out.println(a.getUserData() + " " + b.getUserData());
        if (a.getUserData() instanceof TileEntity && b.getUserData() instanceof Player) {
            ((TileEntity) a.getUserData()).onPlayerStartContact(contact);
        } else if (a.getUserData() instanceof Player && b.getUserData() instanceof TileEntity) {
            ((TileEntity) b.getUserData()).onPlayerStartContact(contact);
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
            ((TileEntity) a.getUserData()).onPlayerEndContact(contact);
        } else if (a.getUserData() instanceof Player && b.getUserData() instanceof TileEntity) {
            ((TileEntity) b.getUserData()).onPlayerEndContact(contact);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}

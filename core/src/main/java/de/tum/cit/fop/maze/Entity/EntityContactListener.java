package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import de.tum.cit.fop.maze.BodyBits;

import java.util.SimpleTimeZone;

public class EntityContactListener implements com.badlogic.gdx.physics.box2d.ContactListener {
    @Override
    public void beginContact(com.badlogic.gdx.physics.box2d.Contact contact) {
    }

    @Override
    public void endContact(com.badlogic.gdx.physics.box2d.Contact contact) {
        // TODO Auto-generated method stub
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

}

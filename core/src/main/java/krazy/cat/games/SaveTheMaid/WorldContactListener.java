package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Projectile;
import krazy.cat.games.SaveTheMaid.Enemy;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        // Check if either fixture is a Projectile
        if (fixtureA.getUserData() instanceof Projectile && fixtureB.getUserData() instanceof Enemy) {
            handleProjectileHit((Projectile) fixtureA.getUserData(), (Enemy) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Projectile && fixtureA.getUserData() instanceof Enemy) {
            handleProjectileHit((Projectile) fixtureB.getUserData(), (Enemy) fixtureA.getUserData());
        }
    }

    private void handleProjectileHit(Projectile projectile, Enemy enemy) {
        // Flag the projectile and enemy for destruction or update their states
        projectile.onCollision();
        enemy.onHit();  // Ensure `onHit` method is in your `Enemy` class for handling hit logic
    }

    @Override
    public void endContact(Contact contact) {
        // You can handle end contact here if needed
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }


}

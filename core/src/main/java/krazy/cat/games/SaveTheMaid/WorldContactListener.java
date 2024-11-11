package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import krazy.cat.games.SaveTheMaid.Characters.Enemy;
import krazy.cat.games.SaveTheMaid.Characters.Player;

public class WorldContactListener implements ContactListener {
    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_ENEMY = 0x0002;
    public static final short CATEGORY_PROJECTILE = 0x0004;
    public static final short CATEGORY_GROUND = 0x0008;

    public static final short MASK_GROUND_ONLY = CATEGORY_GROUND;
    public static final short MASK_PLAYER = CATEGORY_GROUND | CATEGORY_PROJECTILE;
    public static final short MASK_ENEMY = CATEGORY_GROUND | CATEGORY_PROJECTILE;
    public static final short MASK_PROJECTILE = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_GROUND;

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getUserData() instanceof Projectile && fixtureB.getUserData() instanceof Enemy) {
            handleProjectileHitEnemy((Projectile) fixtureA.getUserData(), (Enemy) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Projectile && fixtureA.getUserData() instanceof Enemy) {
            handleProjectileHitEnemy((Projectile) fixtureB.getUserData(), (Enemy) fixtureA.getUserData());
        } else if (fixtureA.getUserData() instanceof Projectile && fixtureB.getUserData() instanceof Player) {
            handleProjectileHitPlayer((Projectile) fixtureA.getUserData(), (Player) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Projectile && fixtureA.getUserData() instanceof Player) {
            handleProjectileHitPlayer((Projectile) fixtureB.getUserData(), (Player) fixtureA.getUserData());
        } else if (fixtureA.getUserData() instanceof Projectile && "environment".equals(fixtureB.getUserData())) {
            handleProjectileEnvironmentCollision((Projectile) fixtureA.getUserData());
        } else if (fixtureB.getUserData() instanceof Projectile && "environment".equals(fixtureA.getUserData())) {
            handleProjectileEnvironmentCollision((Projectile) fixtureB.getUserData());
        } else if (fixtureA.getUserData() instanceof Player && fixtureB.getUserData() instanceof Enemy) {
            handleAttackCollision((Player) fixtureA.getUserData(), (Enemy) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Player && fixtureA.getUserData() instanceof Enemy) {
            handleAttackCollision((Player) fixtureB.getUserData(), (Enemy) fixtureA.getUserData());
        }
    }

    @Override
    public void endContact(Contact contact) {
    }


    private void handleProjectileEnvironmentCollision(Projectile projectile) {
        projectile.onCollision(); // Set the projectile for destruction when it hits the environment
    }

    private void handleAttackCollision(Player player, Enemy enemy) {
        player.onStartEnemyAttackCollision(); // Apply damage to the player
    }

    private void handleProjectileHitEnemy(Projectile projectile, Enemy enemy) {
        // Flag the projectile and enemy for destruction or update their states
        projectile.onCollision();
        enemy.onHit();  // Ensure `onHit` method is in your `Enemy` class for handling hit logic
    }

    private void handleProjectileHitPlayer(Projectile projectile, Player player) {
        // Flag the projectile and enemy for destruction or update their states
        projectile.onCollision();
        player.onStartEnemyAttackCollision();
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }


}

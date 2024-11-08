package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getUserData() instanceof Player && "EnemyAttack".equals(fixtureB.getUserData())) {
            handleAttackCollision((Player) fixtureA.getUserData(), (Enemy) fixtureB.getBody().getUserData());
        } else if (fixtureB.getUserData() instanceof Player && "EnemyAttack".equals(fixtureA.getUserData())) {
            handleAttackCollision((Player) fixtureB.getUserData(), (Enemy) fixtureA.getBody().getUserData());
        } else if (fixtureA.getUserData() instanceof Projectile && fixtureB.getUserData() instanceof Enemy) {
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
            handlePlayerEnemyCollision((Player) fixtureA.getUserData(), (Enemy) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Player && fixtureA.getUserData() instanceof Enemy) {
            handlePlayerEnemyCollision((Player) fixtureB.getUserData(), (Enemy) fixtureA.getUserData());
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getUserData() instanceof Player && "EnemyAttack".equals(fixtureB.getUserData())) {
            handleEndAttackCollision((Player) fixtureA.getUserData(), (Enemy) fixtureB.getBody().getUserData());
        } else if (fixtureB.getUserData() instanceof Player && "EnemyAttack".equals(fixtureA.getUserData())) {
            handleEndAttackCollision((Player) fixtureB.getUserData(), (Enemy) fixtureA.getBody().getUserData());
        }
    }


    private void handleProjectileEnvironmentCollision(Projectile projectile) {
        projectile.onCollision(); // Set the projectile for destruction when it hits the environment
    }

    private void handlePlayerEnemyCollision(Player player, Enemy enemy) {
        Gdx.app.log("handlePlayerEnemyCollision", "PLAYER: " + this);
        enemy.onPlayerCollision(); // Handle enemy response, if any, to the collision
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

    private void handleEndAttackCollision(Player player, Enemy enemy) {
        player.onEndEnemyAttackCollision();
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }


}

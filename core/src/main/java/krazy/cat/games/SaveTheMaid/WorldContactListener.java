package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.BaseFriendAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.CatCharacter;
import krazy.cat.games.SaveTheMaid.Characters.Player;
import krazy.cat.games.SaveTheMaid.Sprites.Goal;

public class WorldContactListener implements ContactListener {
    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_ENEMY = 0x0002;
    public static final short CATEGORY_PROJECTILE = 0x0004;
    public static final short CATEGORY_GROUND = 0x0008;
    public static final short CATEGORY_CAT = 0x0010;

    public static final short MASK_GROUND_ONLY = CATEGORY_GROUND;
    public static final short MASK_PLAYER = CATEGORY_GROUND | CATEGORY_PROJECTILE | CATEGORY_CAT;
    public static final short MASK_ENEMY = CATEGORY_GROUND | CATEGORY_PROJECTILE;
    public static final short MASK_PROJECTILE = CATEGORY_GROUND | CATEGORY_PLAYER | CATEGORY_ENEMY;
    public static final short MASK_CAT = CATEGORY_GROUND | CATEGORY_PLAYER;
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getUserData() instanceof Projectile && fixtureB.getUserData() instanceof BaseAICharacter) {
            handleProjectileHitEnemy((Projectile) fixtureA.getUserData(), (BaseAICharacter) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Projectile && fixtureA.getUserData() instanceof BaseAICharacter) {
            handleProjectileHitEnemy((Projectile) fixtureB.getUserData(), (BaseAICharacter) fixtureA.getUserData());
        } else if (fixtureA.getUserData() instanceof Projectile && fixtureB.getUserData() instanceof Player) {
            handleProjectileHitPlayer((Projectile) fixtureA.getUserData(), (Player) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Projectile && fixtureA.getUserData() instanceof Player) {
            handleProjectileHitPlayer((Projectile) fixtureB.getUserData(), (Player) fixtureA.getUserData());
        } else if (fixtureA.getUserData() instanceof Projectile && "environment".equals(fixtureB.getUserData())) {
            handleProjectileEnvironmentCollision((Projectile) fixtureA.getUserData());
        } else if (fixtureB.getUserData() instanceof Projectile && "environment".equals(fixtureA.getUserData())) {
            handleProjectileEnvironmentCollision((Projectile) fixtureB.getUserData());
        } else if (fixtureA.getUserData() instanceof Player && fixtureB.getUserData() instanceof BaseAICharacter) {
            handleAttackCollision((Player) fixtureA.getUserData(), (BaseAICharacter) fixtureB.getUserData());
        } else if (fixtureB.getUserData() instanceof Player && fixtureA.getUserData() instanceof BaseAICharacter) {
            handleAttackCollision((Player) fixtureB.getUserData(), (BaseAICharacter) fixtureA.getUserData());
        } else if (fixtureB.getUserData() instanceof Player && fixtureA.getUserData() instanceof BaseFriendAICharacter) {
            Gdx.app.log("AAAAA", "collision cat and player!!!");
            ((BaseFriendAICharacter) fixtureA.getUserData()).activate( ((Player) fixtureB.getUserData()));
            ((Player) fixtureB.getUserData()).setFriendReference(((BaseFriendAICharacter) fixtureA.getUserData()));
        } else if (fixtureA.getUserData() instanceof Player && fixtureB.getUserData() instanceof BaseFriendAICharacter) {
            Gdx.app.log("AAAAA", "collision cat and player!!!");
            ((BaseFriendAICharacter) fixtureA.getUserData()).activate(((Player) fixtureB.getUserData()));
        }


        Object userDataA = fixtureA.getUserData();
        Object userDataB = fixtureB.getUserData();
        if (userDataA instanceof Goal && userDataB instanceof CatCharacter) {
            ((Goal) userDataA).onCatCollision();
            ((CatCharacter) userDataB).disappear();
            Gdx.app.log("AAAAA", "collision cat and Goal!!!");

        } else if (userDataB instanceof Goal && userDataA instanceof CatCharacter) {
            ((Goal) userDataB).onCatCollision();
            ((CatCharacter) userDataA).disappear();
            Gdx.app.log("AAAAA", "collision cat and Goal!!!");
        }
    }

    @Override
    public void endContact(Contact contact) {
    }


    private void handleProjectileEnvironmentCollision(Projectile projectile) {
        projectile.onCollision(); // Set the projectile for destruction when it hits the environment
    }

    private void handleAttackCollision(Player player, BaseAICharacter enemy) {
        player.onStartEnemyAttackCollision(); // Apply damage to the player
    }

    private void handleProjectileHitEnemy(Projectile projectile, BaseAICharacter enemy) {
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

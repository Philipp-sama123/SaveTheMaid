package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.physics.box2d.*;

import krazy.cat.games.SaveTheMaid.Characters.AI.*;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.DamnedAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.ZombieAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.*;
import krazy.cat.games.SaveTheMaid.Characters.Player.Player;
import krazy.cat.games.SaveTheMaid.Characters.Projectile;
import krazy.cat.games.SaveTheMaid.Characters.ProjectileUp;
import krazy.cat.games.SaveTheMaid.Sprites.*;

public class WorldContactListener implements ContactListener {
    // Collision Categories
    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_ENEMY = 0x0002;
    public static final short CATEGORY_PROJECTILE = 0x0004;
    public static final short CATEGORY_GROUND = 0x0008;
    public static final short CATEGORY_CAT = 0x0010;
    public static final short CATEGORY_DESTROY = 0x0020;
    // Collision Masks
    public static final short MASK_GROUND_ONLY = CATEGORY_GROUND;
    public static final short MASK_PLAYER = CATEGORY_GROUND | CATEGORY_PROJECTILE | CATEGORY_CAT | CATEGORY_DESTROY;
    public static final short MASK_ENEMY = CATEGORY_GROUND | CATEGORY_PROJECTILE | CATEGORY_DESTROY;
    public static final short MASK_ENEMY_BAT = CATEGORY_PROJECTILE;
    public static final short MASK_PROJECTILE = CATEGORY_GROUND | CATEGORY_PLAYER | CATEGORY_ENEMY;
    public static final short MASK_CAT = CATEGORY_GROUND | CATEGORY_PLAYER;

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getUserData();
        Object userDataB = fixtureB.getUserData();

        // Handle projectile collisions
        if (userDataA instanceof Projectile) {
            handleProjectileCollision((Projectile) userDataA, userDataB);
        } else if (userDataB instanceof Projectile) {
            handleProjectileCollision((Projectile) userDataB, userDataA);
        }
        if (userDataA instanceof ProjectileUp) {
            handleProjectileUpCollision((ProjectileUp) userDataA, userDataB);
        } else if (userDataB instanceof ProjectileUp) {
            handleProjectileUpCollision((ProjectileUp) userDataB, userDataA);
        }
        // Handle player and enemy collisions
        else if (userDataA instanceof Player && userDataB instanceof BaseAICharacter) {
            handleAttackCollision((Player) userDataA, (BaseAICharacter) userDataB);
        } else if (userDataB instanceof Player && userDataA instanceof BaseAICharacter) {
            handleAttackCollision((Player) userDataB, (BaseAICharacter) userDataA);
        }
        // Handle player and water collisions
        else if (userDataA instanceof Player && userDataB instanceof WaterEffect) {
            ((Player) userDataA).onStartEnemyAttackCollision();
        } else if (userDataB instanceof Player && userDataA instanceof WaterEffect) {
            ((Player) userDataB).onStartEnemyAttackCollision();
        }
        // Handle player and friend interactions
        else if (userDataA instanceof Player && userDataB instanceof BaseFriendAICharacter) {
            handleFriendInteraction((Player) userDataA, (BaseFriendAICharacter) userDataB);
        } else if (userDataB instanceof Player && userDataA instanceof BaseFriendAICharacter) {
            handleFriendInteraction((Player) userDataB, (BaseFriendAICharacter) userDataA);
        }
        // Handle player collecting apples
        else if (userDataA instanceof Player && userDataB instanceof Apple) {
            ((Apple) userDataB).onPlayerCollision((Player) userDataA);
        } else if (userDataB instanceof Player && userDataA instanceof Apple) {
            ((Apple) userDataA).onPlayerCollision((Player) userDataB);
        }
        // Handle cat reaching goal
        else if (userDataA instanceof Goal && userDataB instanceof CatCharacter) {
            handleGoalCollision((Goal) userDataA, (CatCharacter) userDataB);
        } else if (userDataB instanceof Goal && userDataA instanceof CatCharacter) {
            handleGoalCollision((Goal) userDataB, (CatCharacter) userDataA);
        }
        // Check if player touches the ground
        if (userDataA instanceof Player && fixtureB.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((Player) userDataA).increaseGroundedCount();
        } else if (userDataB instanceof Player && fixtureA.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((Player) userDataB).increaseGroundedCount();
        }
        // Check if enemy touches the ground
        if (userDataA instanceof BaseAICharacter && fixtureB.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((BaseAICharacter) userDataA).increaseGroundedCount();
        } else if (userDataB instanceof BaseAICharacter && fixtureA.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((BaseAICharacter) userDataB).increaseGroundedCount();
        }
        // Handle collision with the destroy zone
        if ("destroy".equals(userDataA)) {
            if (userDataB instanceof Player) {
                ((Player) userDataB).die();
            } else if (userDataB instanceof BaseAICharacter) {
                ((BaseAICharacter) userDataB).onDie();
            }
        } else if ("destroy".equals(userDataB)) {
            if (userDataA instanceof Player) {
                ((Player) userDataA).die();
            } else if (userDataA instanceof BaseAICharacter) {
                ((BaseAICharacter) userDataA).onDie();
            }
        }

// In beginContact
        if (userDataA instanceof EdgeSensorData) {
            handleEdgeSensorContact((EdgeSensorData) userDataA, true);
        }
        if (userDataB instanceof EdgeSensorData) {
            handleEdgeSensorContact((EdgeSensorData) userDataB, true);
        }

    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getUserData();
        Object userDataB = fixtureB.getUserData();

        // Check if player leaves the ground
        if (userDataA instanceof Player && fixtureB.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((Player) userDataA).decreaseGroundedCount();
        } else if (userDataB instanceof Player && fixtureA.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((Player) userDataB).decreaseGroundedCount();
        }
        // Check if enemy leaves the ground
        if (userDataA instanceof BaseAICharacter && fixtureB.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((BaseAICharacter) userDataA).decreaseGroundedCount();
        } else if (userDataB instanceof BaseAICharacter && fixtureA.getFilterData().categoryBits == CATEGORY_GROUND) {
            ((BaseAICharacter) userDataB).decreaseGroundedCount();
        }

        // --- Handle end of edge sensor collisions ---
        if (userDataA instanceof EdgeSensorData) {
            handleEdgeSensorContact((EdgeSensorData) userDataA, false);
        }
        if (userDataB instanceof EdgeSensorData) {
            handleEdgeSensorContact((EdgeSensorData) userDataB, false);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Pre-solve logic if needed
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Post-solve logic if needed
    }

    // --- Private Helper Methods ---
    private void handleEdgeSensorContact(EdgeSensorData sensorData, boolean isContact) {
        if (sensorData.character == null) return;

        switch (sensorData.side) {
            case "left":
                sensorData.character.setLeftEdgeGrounded(isContact);
                break;
            case "right":
                sensorData.character.setRightEdgeGrounded(isContact);
                break;
        }
    }

    private void handleProjectileCollision(Projectile projectile, Object target) {
        if (target instanceof BaseAICharacter) {
            projectile.onCollision();
            ((BaseAICharacter) target).onHit();
        } else if (target instanceof Player) {
            projectile.onCollision();
            ((Player) target).onStartEnemyAttackCollision();
        } else if ("environment".equals(target)) {
            projectile.onCollision();
        }
    }

    private void handleProjectileUpCollision(ProjectileUp projectileUp, Object target) {
        if (target instanceof BaseAICharacter) {
            projectileUp.onCollision();
            ((BaseAICharacter) target).onHit();
        } else if (target instanceof Player) {
            projectileUp.onCollision();
            ((Player) target).onStartEnemyAttackCollision();
        } else if ("environment".equals(target)) {
            projectileUp.onCollision();
        }
    }

    private void handleAttackCollision(Player player, BaseAICharacter enemy) {
        player.onStartEnemyAttackCollision();
    }

    private void handleFriendInteraction(Player player, BaseFriendAICharacter friend) {
        friend.activate(player);
        player.setFriendReference(friend);
    }

    private void handleGoalCollision(Goal goal, CatCharacter cat) {
        goal.onCatCollision();
        cat.disappear();
    }
}

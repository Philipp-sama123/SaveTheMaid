
package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.BaseEnemy;

public class HitState implements State {
    private boolean animationStarted = false;

    @Override
    public void enter(BaseEnemy enemy) {
        Gdx.app.log("HitState", "Enemy has entered HitState.");

        enemy.health -= enemy.currentDamage;
        if (enemy.health <= 0) {
            enemy.health = 0;
            enemy.getStateMachine().changeState(new DeathState());
        } else {
            // Set the enemy's animation to the hit animation
            enemy.setAnimation(AnimationSetZombie.ZombieAnimationType.HIT);
            enemy.getBody().setLinearVelocity(0, enemy.getBody().getLinearVelocity().y); // Stop horizontal movement
            animationStarted = true;
        }
        enemy.startAttackCooldown();
    }

    @Override
    public void update(BaseEnemy enemy, float dt, Vector2 playerPosition) {
        // Wait for the hit animation to complete
        if (animationStarted && enemy.isHitAnimationFinished()) {
            animationStarted = false;
            // Decide the next state (e.g., ChaseState if the player is close, IdleState otherwise)
            StateMachine stateMachine = enemy.getStateMachine();

            if (!enemy.canAttack()) {
                stateMachine.changeState(new IdleState());
            } else if (enemy.isPlayerInRange(playerPosition)) {
                stateMachine.changeState(new ChaseState());
            } else {
                stateMachine.changeState(new IdleState());
            }

        }
    }

    @Override
    public void exit(BaseEnemy enemy) {
        Gdx.app.log("HitState", "Enemy is exiting HitState.");
    }
}

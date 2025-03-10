
package krazy.cat.games.SaveTheMaid.Characters.AI.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.AI.StateMachine;
import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;

public class HitState implements State {
    private boolean animationStarted = false;

    @Override
    public void enter(BaseAICharacter<?> enemy) {
        enemy.health -= enemy.currentDamage;
        if (enemy.health <= 0) {
            enemy.health = 0;
            enemy.getStateMachine().changeState(enemy.deathState);
        } else {
            // Set the enemy's animation to the hit animation
            enemy.hit();
            animationStarted = true;
            enemy.HIT_SOUND.play();
        }
        enemy.startAttackCooldown();
    }

    @Override
    public void update(BaseAICharacter<?> enemy, float dt, Vector2 playerPosition) {
        // Wait for the hit animation to complete
        if (animationStarted && enemy.isHitAnimationFinished()) {
            animationStarted = false;
            // Decide the next state (e.g., ChaseState if the player is close, IdleState otherwise)
            StateMachine stateMachine = enemy.getStateMachine();

            if (!enemy.canAttack()) {
                stateMachine.changeState(enemy.idleState);
            } else if (enemy.isPlayerInRange(playerPosition)) {
                stateMachine.changeState(enemy.chaseState);
            } else {
                stateMachine.changeState(enemy.idleState);
            }

        }
    }

    @Override
    public void exit(BaseAICharacter<?> enemy) {
        Gdx.app.log("HitState", "Enemy is exiting HitState.");
    }
}

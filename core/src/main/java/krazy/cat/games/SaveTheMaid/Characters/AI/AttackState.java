package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.BaseAICharacter;

public class AttackState implements State {
    @Override
    public void enter(BaseAICharacter enemy) {
        if (enemy.canAttack()) {
            enemy.attack();
            enemy.attackSound.play();
        } else {
            enemy.getStateMachine().changeState(new IdleState()); // Switch back if cooldown is active
        }
    }


    @Override
    public void update(BaseAICharacter enemy, float deltaTime, Vector2 playerPosition) {
        if (enemy.isAttackAnimationFinished()) {
            enemy.getStateMachine().changeState(new IdleState());
        }
    }

    @Override
    public void exit(BaseAICharacter enemy) {
        enemy.deactivateAttackCollider();
    }
}

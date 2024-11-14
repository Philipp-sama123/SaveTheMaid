package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.BaseAICharacter;

public class ChaseState implements State {
    @Override
    public void enter(BaseAICharacter enemy) {
        enemy.chase();
    }

    @Override
    public void update(BaseAICharacter enemy, float deltaTime, Vector2 playerPosition) {
        if (enemy.isPlayerInRange(playerPosition)) {
            if (enemy.isInAttackRange(playerPosition)) {
                enemy.getStateMachine().changeState(new AttackState());
            } else {
                enemy.moveToPlayer(playerPosition);
            }
        } else {
            enemy.getStateMachine().changeState(new IdleState());
        }
    }

    @Override
    public void exit(BaseAICharacter enemy) {
        // Cleanup or stop movement if necessary
    }
}

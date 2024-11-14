package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.BaseAICharacter;

public class IdleState implements State {
    @Override
    public void enter(BaseAICharacter enemy) {
        enemy.idle();
    }



    @Override
    public void update(BaseAICharacter enemy, float deltaTime, Vector2 playerPosition) {
        if (!enemy.canAttack()) return;

        if (enemy.isPlayerInRange(playerPosition)) {
            enemy.getStateMachine().changeState(new ChaseState());
        }
    }

    @Override
    public void exit(BaseAICharacter enemy) {
        // Any cleanup or reset if needed
    }

}

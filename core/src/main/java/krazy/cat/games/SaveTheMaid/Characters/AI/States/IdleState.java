package krazy.cat.games.SaveTheMaid.Characters.AI.States;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;

public class IdleState implements State {
    @Override
    public void enter(BaseAICharacter<?> enemy) {
        enemy.idle();
    }



    @Override
    public void update(BaseAICharacter<?> enemy, float deltaTime, Vector2 playerPosition) {
        if (!enemy.canAttack()) return;

        if (enemy.isPlayerInRange(playerPosition)) {
            enemy.getStateMachine().changeState(enemy.chaseState);
        }
    }

    @Override
    public void exit(BaseAICharacter<?> enemy) {
        // Any cleanup or reset if needed
    }

}

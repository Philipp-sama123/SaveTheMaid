package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.BaseAICharacter;

public class StateMachine {
    private State currentState;
    private BaseAICharacter enemy;

    public StateMachine(BaseAICharacter enemy) {
        this.enemy = enemy;
    }

    public void changeState(State newState) {
        Gdx.app.log("CHANGE STATE", "from state: " + currentState + " to " + newState);

        if (currentState != null) {
            currentState.exit(enemy);
        }
        currentState = newState;
        currentState.enter(enemy);
    }

    public void update(float deltaTime, Vector2 playerPosition) {
        if (currentState != null) {
            currentState.update(enemy, deltaTime, playerPosition);
        }
    }
}

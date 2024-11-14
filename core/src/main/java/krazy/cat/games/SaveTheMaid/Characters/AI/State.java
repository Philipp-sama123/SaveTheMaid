package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.BaseAICharacter;

public interface State {
    void enter(BaseAICharacter enemy);
    void update(BaseAICharacter enemy, float deltaTime, Vector2 playerPosition);
    void exit(BaseAICharacter enemy);
}

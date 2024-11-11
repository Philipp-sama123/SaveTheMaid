package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.BaseEnemy;

public interface State {
    void enter(BaseEnemy enemy);
    void update(BaseEnemy enemy, float deltaTime, Vector2 playerPosition);
    void exit(BaseEnemy enemy);
}

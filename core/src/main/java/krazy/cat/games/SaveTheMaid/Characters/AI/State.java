package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.Enemy;

public interface State {
    void enter(Enemy enemy);
    void update(Enemy enemy, float deltaTime, Vector2 playerPosition);
    void exit(Enemy enemy);
}

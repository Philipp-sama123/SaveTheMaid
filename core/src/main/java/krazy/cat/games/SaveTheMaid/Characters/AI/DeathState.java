package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.Enemy;

public class DeathState implements State {
    @Override
    public void enter(Enemy enemy) {
        enemy.setAnimation(AnimationSetZombie.ZombieAnimationType.DEATH);
        enemy.disableCollision();
    }

    @Override
    public void update(Enemy enemy, float deltaTime, Vector2 playerPosition) {
        if (enemy.isDeathAnimationComplete()) {
            enemy.isDestroyed = true;
        }
    }

    @Override
    public void exit(Enemy enemy) {
        // No exit logic for death
    }
}

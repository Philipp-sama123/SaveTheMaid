package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.BaseAICharacter;

public class DeathState implements State {
    @Override
    public void enter(BaseAICharacter enemy) {
        enemy.deathSound.play();
        enemy.die();
    }

    @Override
    public void update(BaseAICharacter enemy, float deltaTime, Vector2 playerPosition) {
        if (enemy.isDeathAnimationComplete()) {
            enemy.isDestroyed = true;
        }
    }

    @Override
    public void exit(BaseAICharacter enemy) {
        // No exit logic for death
    }
}

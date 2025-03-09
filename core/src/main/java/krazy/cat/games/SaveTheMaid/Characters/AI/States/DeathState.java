package krazy.cat.games.SaveTheMaid.Characters.AI.States;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;

public class DeathState implements State {
    @Override
    public void enter(BaseAICharacter enemy) {
        enemy.spawnPickupAtDeathPosition(enemy.getBody().getPosition().cpy());

        enemy.DEATH_SOUND.play();
        enemy.die();
        enemy.registerKillOnGameScreen(); // ToDo: remove and improve as soon as possible (!)
        // Capture the enemy's position before any state changes
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

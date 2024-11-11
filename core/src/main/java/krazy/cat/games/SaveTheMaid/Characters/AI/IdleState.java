package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.Enemy;

public class IdleState implements State {
    @Override
    public void enter(Enemy enemy) {
        enemy.setAnimation(AnimationSetZombie.ZombieAnimationType.IDLE);
        enemy.getBody().setLinearVelocity(0, 0);
    }


    @Override
    public void update(Enemy enemy, float deltaTime, Vector2 playerPosition) {
        if (!enemy.canAttack()) return;

        if (enemy.isPlayerInRange(playerPosition)) {
            enemy.getStateMachine().changeState(new ChaseState());
        }
    }

    @Override
    public void exit(Enemy enemy) {
        // Any cleanup or reset if needed
    }

}

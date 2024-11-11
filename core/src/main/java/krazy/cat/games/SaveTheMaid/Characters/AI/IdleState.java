package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.BaseEnemy;

public class IdleState implements State {
    @Override
    public void enter(BaseEnemy enemy) {
        enemy.setAnimation(AnimationSetZombie.ZombieAnimationType.IDLE);
        enemy.getBody().setLinearVelocity(0, 0);
    }


    @Override
    public void update(BaseEnemy enemy, float deltaTime, Vector2 playerPosition) {
        if (!enemy.canAttack()) return;

        if (enemy.isPlayerInRange(playerPosition)) {
            enemy.getStateMachine().changeState(new ChaseState());
        }
    }

    @Override
    public void exit(BaseEnemy enemy) {
        // Any cleanup or reset if needed
    }

}

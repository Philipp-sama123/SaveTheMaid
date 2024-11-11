package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.BaseEnemy;

public class ChaseState implements State {
    @Override
    public void enter(BaseEnemy enemy) {
        enemy.setAnimation(AnimationSetZombie.ZombieAnimationType.WALK);
    }

    @Override
    public void update(BaseEnemy enemy, float deltaTime, Vector2 playerPosition) {
        if (enemy.isPlayerInRange(playerPosition)) {
            if (enemy.isInAttackRange(playerPosition)) {
                enemy.getStateMachine().changeState(new AttackState());
            } else {
                enemy.moveToPlayer(playerPosition);
            }
        } else {
            enemy.getStateMachine().changeState(new IdleState());
        }
    }

    @Override
    public void exit(BaseEnemy enemy) {
        // Cleanup or stop movement if necessary
    }
}

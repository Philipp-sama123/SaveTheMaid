package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.BaseEnemy;

public class AttackState implements State {
    @Override
    public void enter(BaseEnemy enemy) {
        if (enemy.canAttack()) {
            enemy.setAnimation(AnimationSetZombie.ZombieAnimationType.ATTACK);
            enemy.activateAttackCollider();
            enemy.updateAttackColliderPosition();
            enemy.startAttackCooldown(); // Start the cooldown after initiating the attack
        } else {
            enemy.getStateMachine().changeState(new IdleState()); // Switch back if cooldown is active
        }
    }

    @Override
    public void update(BaseEnemy enemy, float deltaTime, Vector2 playerPosition) {
        if (enemy.isAttackAnimationFinished()) {
            enemy.getStateMachine().changeState(new IdleState());
        }
    }

    @Override
    public void exit(BaseEnemy enemy) {
        enemy.deactivateAttackCollider();
    }
}

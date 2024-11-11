package krazy.cat.games.SaveTheMaid.Characters.AI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.Enemy;

public class AttackState implements State {
    @Override
    public void enter(Enemy enemy) {
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
    public void update(Enemy enemy, float deltaTime, Vector2 playerPosition) {
        if (enemy.isAttackAnimationFinished()) {
            enemy.getStateMachine().changeState(new IdleState());
        }
    }

    @Override
    public void exit(Enemy enemy) {
        enemy.deactivateAttackCollider();
    }
}

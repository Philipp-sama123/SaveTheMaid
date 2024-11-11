package krazy.cat.games.SaveTheMaid.Characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.AI.IdleState;
import krazy.cat.games.SaveTheMaid.Characters.AI.StateMachine;

public class BaseEnemy {
    public boolean isDestroyed;
    public int health = 100;
    public int currentDamage = 20;
    protected World world;
    protected float stateTime;
    protected StateMachine stateMachine;

    public BaseEnemy(World world, Vector2 position) {
        this.world = world;
        this.stateTime = 0f;

        defineEnemy(position);
        deactivateAttackCollider();
        stateMachine = new StateMachine(this);
        stateMachine.changeState(new IdleState());
    }

    public void deactivateAttackCollider() {
    }

    protected void defineEnemy(Vector2 position) {
    }

    public void setAnimation(AnimationSetZombie.ZombieAnimationType zombieAnimationType) {
    }

    public boolean isPlayerInRange(Vector2 playerPosition) {
        return false;
    }

    public boolean isInAttackRange(Vector2 playerPosition) {
        return false;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public void moveToPlayer(Vector2 playerPosition) {
    }

    public boolean canAttack() {
        return false;
    }

    public void updateAttackColliderPosition() {
    }

    public void activateAttackCollider() {
    }

    public void startAttackCooldown() {
    }

    public boolean isAttackAnimationFinished() {
        return false;
    }

    public boolean isDeathAnimationComplete() {
        return false;
    }

    public void disableCollision() {
    }

    public Body getBody() {
        return null;
    }

    public boolean isHitAnimationFinished() {
        return false;
    }
}

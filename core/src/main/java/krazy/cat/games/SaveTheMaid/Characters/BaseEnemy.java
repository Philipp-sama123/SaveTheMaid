package krazy.cat.games.SaveTheMaid.Characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.AnimationSetBat;
import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.AI.HitState;
import krazy.cat.games.SaveTheMaid.Characters.AI.IdleState;
import krazy.cat.games.SaveTheMaid.Characters.AI.StateMachine;

public abstract class BaseEnemy {
    public boolean isDestroyed;
    public int health = 100;
    public int currentDamage = 20;

    public float attackCooldownTimer = 0f;
    public float attackColliderUpdateTimer = 0f; // Timer to keep track of elapsed time

    protected World world;
    protected float stateTime;
    protected StateMachine stateMachine;

    protected Body body;
    protected Fixture attackCollider;

    public BaseEnemy(World world, Vector2 position) {
        this.world = world;
        this.stateTime = 0f;

        defineEnemy(position);
        deactivateAttackCollider();
        stateMachine = new StateMachine(this);
        stateMachine.changeState(new IdleState());
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public void onHit() {
        stateMachine.changeState(new HitState());
    }

    public abstract void deactivateAttackCollider();

    public abstract void draw(Batch batch);

    protected abstract void defineEnemy(Vector2 position);

    public abstract void setAnimation(AnimationSetZombie.ZombieAnimationType type);

    public abstract void setAnimation(AnimationSetBat.BatAnimationType type);

    public abstract boolean isPlayerInRange(Vector2 playerPosition);

    public abstract boolean isInAttackRange(Vector2 playerPosition);

    public abstract void moveToPlayer(Vector2 playerPosition);

    public abstract boolean canAttack();

    public abstract void updateAttackColliderPosition();

    public abstract void activateAttackCollider();

    public abstract void startAttackCooldown();

    public abstract boolean isAttackAnimationFinished();

    public abstract boolean isDeathAnimationComplete();

    public abstract void disableCollision();

    public abstract Body getBody();

    public abstract boolean isHitAnimationFinished();

    public abstract void attack();

    public abstract void chase();

    public abstract void die();

    public abstract void hit();

    public abstract void idle();

    public abstract void update(float dt, Vector2 position);

    public abstract void dispose();
}

package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_GROUND_ONLY;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.AnimationSetBat;
import krazy.cat.games.SaveTheMaid.AnimationSetRat;
import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.AI.HitState;
import krazy.cat.games.SaveTheMaid.Characters.AI.IdleState;
import krazy.cat.games.SaveTheMaid.Characters.AI.StateMachine;

public abstract class BaseEnemy {
    protected static float ATTACK_COOLDOWN = 1.5f; // Time to reset attack collider
    protected static  float ATTACK_RANGE = 25f;
    protected static final float MOVEMENT_SPEED = 15f;
    protected static final float ATTACK_COLLIDER_UPDATE_DELAY = .4f; // Delay in seconds for updating the collider position


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
    protected boolean attackColliderActive;

    public BaseEnemy(World world, Vector2 position) {
        this.world = world;
        this.stateTime = 0f;

        defineEnemy(position);
        deactivateAttackCollider();
        stateMachine = new StateMachine(this);
        stateMachine.changeState(new IdleState());
    }

    public void disableCollision() {
        body.getFixtureList().forEach(fixture -> {
            Filter filter = fixture.getFilterData();
            filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
            fixture.setFilterData(filter);
        });
    }

    public void onHit() {
        stateMachine.changeState(new HitState());
    }

    public void activateAttackCollider() {
        attackColliderActive = true;
    }

    public void deactivateAttackCollider() {
        attackColliderActive = false;
        Filter filter = attackCollider.getFilterData();
        filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
        attackCollider.setFilterData(filter);
    }

    public boolean canAttack() {
        return attackCooldownTimer <= 0; // Can attack only if cooldown has expired
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public void startAttackCooldown() {
        attackCooldownTimer = ATTACK_COOLDOWN; // Reset cooldown timer
    }

    public boolean isInAttackRange(Vector2 playerPosition) {
        return body.getPosition().dst(playerPosition) < ATTACK_RANGE;
    }

    public boolean isPlayerInRange(Vector2 playerPosition) {
        return body.getPosition().dst(playerPosition) < 500;
    }

    protected abstract void defineEnemy(Vector2 position);

    public abstract void setAnimation(AnimationSetZombie.ZombieAnimationType type);

    public abstract void setAnimation(AnimationSetBat.BatAnimationType type);

    public abstract void setAnimation(AnimationSetRat.RatAnimationType type);

    public abstract void moveToPlayer(Vector2 playerPosition);

    public abstract void updateAttackColliderPosition();

    public abstract boolean isAttackAnimationFinished();

    public abstract boolean isDeathAnimationComplete();

    public abstract boolean isHitAnimationFinished();

    public abstract void attack();

    public abstract void chase();

    public abstract void die();

    public abstract void hit();

    public abstract void idle();

    public abstract void update(float dt, Vector2 position);

    public abstract void draw(Batch batch);

    public abstract void dispose();
}

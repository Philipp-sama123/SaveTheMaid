package krazy.cat.games.SaveTheMaid.Characters.AI;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_GROUND_ONLY;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Characters.AI.States.HitState;
import krazy.cat.games.SaveTheMaid.Characters.AI.States.IdleState;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;


public abstract class BaseAICharacter<T extends Enum<T>> {
    protected static float ATTACK_COOLDOWN = 1.5f; // Time to reset attack collider
    protected static float ATTACK_RANGE = 25f / PPM;
    protected static final float MOVEMENT_SPEED = 15f / PPM;
    protected static final float ATTACK_COLLIDER_UPDATE_DELAY = .4f; // Delay in seconds for updating the collider position

    public Sound ATTACK_SOUND = GameAssetManager.getInstance().get(AssetPaths.SWIPE_SOUND, Sound.class);
    public Sound HIT_SOUND = GameAssetManager.getInstance().get(AssetPaths.PLAYER_HIT_SOUND, Sound.class);
    public Sound DEATH_SOUND = GameAssetManager.getInstance().get(AssetPaths.ZOMBIE_ATTACK_SOUND, Sound.class);

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

    protected boolean isFacingLeft = false;
    public boolean isDestroyed = false;

    protected Texture healthBar;


    public BaseAICharacter(World world, Vector2 position) {
        this.world = world;
        this.stateTime = 0f;
        healthBar = GameAssetManager.getInstance().get(AssetPaths.HEALTH_BAR_SIMPLE, Texture.class);

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
        if (attackCollider == null) {
            return;
        }
        attackColliderActive = false;
        Filter filter = attackCollider.getFilterData();
        filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
        attackCollider.setFilterData(filter);
    }

    public abstract boolean canAttack();

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

    public abstract void setAnimation(T animationType);

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

    public void drawHealthBar(Batch batch) {
        batch.setColor(1,1,1,.35f);
        batch.draw(healthBar, body.getPosition().x - (healthBar.getWidth() / PPM) / 2, body.getPosition().y + 15 / PPM, healthBar.getWidth() / PPM * ((float) health / 100), healthBar.getHeight() / PPM);
        batch.setColor(1,1,1,1);

    }

    public void dispose() {
        if (world != null && body != null) {
            if (attackCollider != null) body.destroyFixture(attackCollider);
            world.destroyBody(body);
            body = null;
        }
        //animationSet.dispose(); ToDo: OOOOUU this is bad (!) when this is activated all the other rats get a null :O
    }
}

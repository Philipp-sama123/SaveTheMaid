package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_GROUND_ONLY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.AnimationSetBat;
import krazy.cat.games.SaveTheMaid.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.AI.HitState;
import krazy.cat.games.SaveTheMaid.Characters.AI.IdleState;
import krazy.cat.games.SaveTheMaid.Characters.AI.StateMachine;

public class BatEnemy extends BaseEnemy {
    private static final float ATTACK_RANGE = 25f;
    private static final float MOVEMENT_SPEED = 15f;
    private static final float ATTACK_COOLDOWN = 1.5f; // Time to reset attack collider
    private static final float ATTACK_COLLIDER_UPDATE_DELAY = .4f; // Delay in seconds for updating the collider position

    private final AnimationSetBat animationSet;

    private AnimationSetBat.BatAnimationType currentState;
    private AnimationSetBat.BatAnimationType previousState;
    public float stateTime;

    public boolean attackColliderActive = false;
    private boolean isFacingLeft = false;
    public boolean isDestroyed = false;

    public BatEnemy(World world, Vector2 position) {
        super(world, position);
        this.currentState = AnimationSetBat.BatAnimationType.MOVE1;

        Texture spriteSheet = new Texture("Characters/Bat/Bat_v1/Sprite Sheet/Bat_v1_Sheet.png");
        this.animationSet = new AnimationSetBat(spriteSheet);
    }

    public void update(float dt, Vector2 playerPosition) {
        if (isDestroyed && !isDeathAnimationComplete()) {
            disableCollision();
            return;
        }
        if (isDestroyed) {
            return;
        }
        stateTime += dt;

        stateMachine.update(dt, playerPosition);
        // Handle attack cooldown
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= dt;
        }
        adjustFacingDirection();

        if (attackColliderUpdateTimer >= ATTACK_COLLIDER_UPDATE_DELAY) {
            updateAttackColliderPosition();
            attackColliderUpdateTimer = 0f; // Reset the timer after updating
        }
    }

    public void disableCollision() {
        body.getFixtureList().forEach(fixture -> {
            Filter filter = fixture.getFilterData();
            filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
            fixture.setFilterData(filter);
        });
    }

    public boolean canAttack() {
        return attackCooldownTimer <= 0; // Can attack only if cooldown has expired
    }

    public void startAttackCooldown() {
        attackCooldownTimer = ATTACK_COOLDOWN; // Reset cooldown timer
    }

    @Override
    public void draw(Batch batch) {
        if (isDestroyed && isDeathAnimationComplete()) return;

        boolean looping = currentState != AnimationSetBat.BatAnimationType.DEATH2;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        batch.draw(
            currentFrame,
            body.getPosition().x - 21,
            body.getPosition().y - 20,
            40,
            42
        );
    }


    protected void defineEnemy(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(10f, 10f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        fixtureDef.filter.categoryBits = CATEGORY_ENEMY;
        fixtureDef.filter.maskBits = MASK_ENEMY;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
        // Initialize the attack collider as a sensor initially
        createAttackCollider();

        body.setGravityScale(0f);
    }

    @Override
    public void setAnimation(AnimationSetZombie.ZombieAnimationType zombieAnimationType) {

    }

    private void createAttackCollider() {
        PolygonShape attackShape = new PolygonShape();
        float xOffset = isFacingLeft ? -12f : 12f;
        attackShape.setAsBox(6f, 6f, new Vector2(xOffset, 0), 0);

        FixtureDef attackFixtureDef = new FixtureDef();
        attackFixtureDef.shape = attackShape;
        attackFixtureDef.isSensor = true; // Ensure it’s always a sensor

        attackFixtureDef.filter.categoryBits = CATEGORY_PROJECTILE;
        attackFixtureDef.filter.maskBits = MASK_PROJECTILE;

        attackCollider = body.createFixture(attackFixtureDef);
        attackCollider.setUserData(this);
        attackShape.dispose();

        attackColliderActive = false;
    }

    public void updateAttackColliderPosition() {
        if (attackCollider == null) return;
        if (attackColliderActive) {
            // Calculate the offset based on facing direction
            float xOffset = isFacingLeft ? -12f : 12f;
            float yOffset = 0f; // Adjust Y offset if necessary

            // Move the collider to the attack position relative to the enemy's body
            PolygonShape attackShape = (PolygonShape) attackCollider.getShape();
            attackShape.setAsBox(6f, 6f, new Vector2(xOffset, yOffset), 0);
            Filter filter = attackCollider.getFilterData();
            filter.categoryBits = CATEGORY_PROJECTILE;
            filter.maskBits = MASK_PROJECTILE;
            attackCollider.setFilterData(filter);

        } else {
            // this is a hack for "disabling" the collider
            Filter filter = attackCollider.getFilterData();
            filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
            attackCollider.setFilterData(filter);
        }
    }

    private void adjustFacingDirection() {
        float velocityX = body.getLinearVelocity().x;

        if (velocityX < 0 && !isFacingLeft) {
            animationSet.flipFramesHorizontally();
            isFacingLeft = true;
        } else if (velocityX > 0 && isFacingLeft) {
            animationSet.flipFramesHorizontally();
            isFacingLeft = false;
        }
    }

    public void activateAttackCollider() {
        attackColliderActive = true;
    }

    public void deactivateAttackCollider() {
        attackColliderActive = false;
        PolygonShape attackShape = (PolygonShape) attackCollider.getShape();
        attackShape.setAsBox(0, 0, new Vector2(0, 0), 0);
        Filter filter = attackCollider.getFilterData();
        filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
        attackCollider.setFilterData(filter);
    }

    public void moveToPlayer(Vector2 playerPosition) {
        Vector2 direction = playerPosition.cpy().sub(body.getPosition()).nor();
        direction.scl(MOVEMENT_SPEED);

        body.setLinearVelocity(direction.x, direction.y);

        if (direction.len() > 0) {
            currentState = AnimationSetBat.BatAnimationType.MOVE2;
        } else {
            currentState = AnimationSetBat.BatAnimationType.MOVE1;
        }
    }

    public void setAnimation(AnimationSetBat.BatAnimationType type) {
        currentState = type;
        stateTime = 0;
    }

    public boolean isInAttackRange(Vector2 playerPosition) {
        return body.getPosition().dst(playerPosition) < ATTACK_RANGE;
    }

    public boolean isPlayerInRange(Vector2 playerPosition) {
        return body.getPosition().dst(playerPosition) < 500;
    }

    public boolean isDeathAnimationComplete() {
        return animationSet.getAnimation(AnimationSetBat.BatAnimationType.DEATH2).isAnimationFinished(stateTime);
    }

    public boolean isAttackAnimationFinished() {
        return animationSet.getAnimation(AnimationSetBat.BatAnimationType.GRAB).isAnimationFinished(stateTime);
    }

    public boolean isHitAnimationFinished() {
        return animationSet.getAnimation(AnimationSetBat.BatAnimationType.HIT).isAnimationFinished(stateTime);
    }

    @Override
    public void attack() {
        setAnimation(AnimationSetBat.BatAnimationType.GRAB);
        activateAttackCollider();
        updateAttackColliderPosition();
        startAttackCooldown(); // Start the cooldown after initiating the attack
    }

    @Override
    public void chase() {
        setAnimation(AnimationSetBat.BatAnimationType.MOVE1);
    }

    @Override
    public void die() {
        setAnimation(AnimationSetBat.BatAnimationType.DEATH2);
        disableCollision();
    }

    @Override
    public void hit() {
        setAnimation(AnimationSetBat.BatAnimationType.HIT);
        body.setLinearVelocity(0, body.getLinearVelocity().y); // Stop horizontal movement
    }

    @Override
    public void idle() {
        setAnimation(AnimationSetBat.BatAnimationType.MOVE1);
        body.setLinearVelocity(0, 0);
    }

    @Override
    public Body getBody() {
        return body;
    }

    public void dispose() {
        if (world != null && body != null) {
            if (attackCollider != null) body.destroyFixture(attackCollider);
            world.destroyBody(body);
            body = null;
        }
    }
}

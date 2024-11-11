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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.AnimationSetBat;
import krazy.cat.games.SaveTheMaid.AnimationSetRat;
import krazy.cat.games.SaveTheMaid.AnimationSetZombie;

public class ZombieEnemy extends BaseEnemy {
    private static final float MOVEMENT_SPEED = 15f;
    private static final float ATTACK_COLLIDER_UPDATE_DELAY = .4f; // Delay in seconds for updating the collider position

    private final AnimationSetZombie animationSet;

    private AnimationSetZombie.ZombieAnimationType currentState;
    private AnimationSetZombie.ZombieAnimationType previousState;

    public float stateTime;
    private boolean isFacingLeft = true;
    public boolean isDestroyed = false;

    public ZombieEnemy(World world, Vector2 position) {
        super(world, position);
        this.currentState = AnimationSetZombie.ZombieAnimationType.IDLE;

        Texture spriteSheet = new Texture("Characters/Zombie/Colors/Grey.png");
        this.animationSet = new AnimationSetZombie(spriteSheet);
    }

    public void update(float dt, Vector2 playerPosition) {
        if (isDestroyed) {
            disableCollision();
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

    @Override
    public void draw(Batch batch) {
        if (isDestroyed && isDeathAnimationComplete()) return;

        boolean looping = currentState != AnimationSetZombie.ZombieAnimationType.DEATH;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        batch.draw(
            currentFrame,
            body.getPosition().x - 32,
            body.getPosition().y - 20,
            64,
            64
        );
    }


    protected void defineEnemy(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f, 20f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        fixtureDef.filter.categoryBits = CATEGORY_ENEMY;
        fixtureDef.filter.maskBits = MASK_ENEMY;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        // Initialize the attack collider as a sensor initially
        createAttackCollider();
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


    public void moveToPlayer(Vector2 playerPosition) {
        Vector2 direction = playerPosition.cpy().sub(body.getPosition()).nor();
        direction.scl(MOVEMENT_SPEED);

        body.setLinearVelocity(direction.x, body.getLinearVelocity().y);

        if (direction.len() > 0) {
            currentState = AnimationSetZombie.ZombieAnimationType.WALK;
        } else {
            currentState = AnimationSetZombie.ZombieAnimationType.IDLE;
        }
    }

    @Override
    public void setAnimation(AnimationSetZombie.ZombieAnimationType type) {
        currentState = type;
        stateTime = 0;
    }

    @Override
    public void setAnimation(AnimationSetBat.BatAnimationType type) {
    }

    @Override
    public void setAnimation(AnimationSetRat.RatAnimationType type) {

    }

    @Override
    public boolean isDeathAnimationComplete() {
        return animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.DEATH).isAnimationFinished(stateTime);
    }
    @Override
    public boolean isAttackAnimationFinished() {
        return animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.ATTACK).isAnimationFinished(stateTime);
    }
    @Override
    public boolean isHitAnimationFinished() {
        return animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.HIT).isAnimationFinished(stateTime);
    }

    @Override
    public void attack() {
        setAnimation(AnimationSetZombie.ZombieAnimationType.ATTACK);
        activateAttackCollider();
        updateAttackColliderPosition();
        startAttackCooldown(); // Start the cooldown after initiating the attack
    }

    @Override
    public void chase() {
        setAnimation(AnimationSetZombie.ZombieAnimationType.WALK);
    }

    @Override
    public void die() {
        setAnimation(AnimationSetZombie.ZombieAnimationType.DEATH);
        disableCollision();
    }

    @Override
    public void hit() {
        setAnimation(AnimationSetZombie.ZombieAnimationType.HIT);
        body.setLinearVelocity(0, body.getLinearVelocity().y); // Stop horizontal movement
    }

    @Override
    public void idle() {
        setAnimation(AnimationSetZombie.ZombieAnimationType.IDLE);
        body.setLinearVelocity(0, 0);
    }

    @Override
    public void dispose() {
        if (world != null && body != null) {
            if (attackCollider != null) body.destroyFixture(attackCollider);
            world.destroyBody(body);
            body = null;
        }
    }
}

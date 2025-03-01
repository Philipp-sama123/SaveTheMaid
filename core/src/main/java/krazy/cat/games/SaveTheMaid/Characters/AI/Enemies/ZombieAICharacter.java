package krazy.cat.games.SaveTheMaid.Characters.AI.Enemies;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
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

import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetZombie;
import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class ZombieAICharacter extends BaseAICharacter<AnimationSetZombie.ZombieAnimationType> {
    private final AnimationSetZombie animationSet;

    private AnimationSetZombie.ZombieAnimationType currentState;
    private AnimationSetZombie.ZombieAnimationType previousState;

    public ZombieAICharacter(World world, Vector2 position, BaseLevel gameScreen) {
        super(world, position,gameScreen);
        this.currentState = AnimationSetZombie.ZombieAnimationType.IDLE;

        Texture spriteSheet = GameAssetManager.getInstance().get(AssetPaths.ZOMBIE_GREY_TEXTURE, Texture.class);
        this.animationSet = new AnimationSetZombie(spriteSheet);
        isFacingLeft = true;

    }

    @Override
    public boolean canAttack() {
        return attackCooldownTimer <= 0; // Can attack only if cooldown has expired
    }

    @Override
    public void update(float dt, Vector2 playerPosition) {
        if (isDestroyed && !isDeathAnimationComplete()) {
            disableCollision();
            return;
        }
        if (isDestroyed && isDeathAnimationComplete()) {
            dispose();
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

        drawHealthBar(batch);

        batch.draw(
            currentFrame,
            body.getPosition().x - 32 / PPM,
            body.getPosition().y - 20 / PPM,
            64 / PPM,
            64 / PPM
        );

    }

    @Override
    protected void defineEnemy(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f / PPM, 20f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        fixtureDef.filter.categoryBits = CATEGORY_ENEMY;
        fixtureDef.filter.maskBits = MASK_ENEMY;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        // Initialize the attack collider as a sensor initially
        createAttackCollider();
    }

    @Override
    public void updateAttackColliderPosition() {
        if (attackCollider == null) return;
        if (attackColliderActive) {
            // Calculate the offset based on facing direction
            float xOffset = isFacingLeft ? -12f / PPM : 12f / PPM;
            float yOffset = 0f; // Adjust Y offset if necessary

            // Move the collider to the attack position relative to the enemy's body
            PolygonShape attackShape = (PolygonShape) attackCollider.getShape();
            attackShape.setAsBox(6f / PPM, 6f / PPM, new Vector2(xOffset, yOffset), 0);
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

    @Override
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
        ATTACK_SOUND.play();
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

    private void createAttackCollider() {
        PolygonShape attackShape = new PolygonShape();
        float xOffset = isFacingLeft ? -12f / PPM : 12f / PPM;
        attackShape.setAsBox(6f / PPM, 6f / PPM, new Vector2(xOffset, 0), 0);

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

}

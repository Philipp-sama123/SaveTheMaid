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

import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetRat;
import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class RatAICharacter extends BaseAICharacter<AnimationSetRat.RatAnimationType> {
    private final AnimationSetRat animationSet;

    private AnimationSetRat.RatAnimationType currentState;
    private AnimationSetRat.RatAnimationType previousState;

    public RatAICharacter(World world, Vector2 position, BaseLevel baseLevel) {
        super(world, position,baseLevel);
        this.currentState = AnimationSetRat.RatAnimationType.IDLE;

        Texture spriteSheet = GameAssetManager.getInstance().get(
            AssetPaths.RAT_TEXTURE,
            Texture.class
        );
        this.animationSet = new AnimationSetRat(spriteSheet);
        ATTACK_RANGE = 30f / PPM;
    }

    @Override
    public boolean canAttack() {
        return attackCooldownTimer <= 0; // Can attack only if cooldown has expired
    }

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

        boolean looping = currentState != AnimationSetRat.RatAnimationType.DEATH_1;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        batch.draw(
            currentFrame,
            body.getPosition().x - (float) AnimationSetRat.FRAME_WIDTH / 2 / PPM,
            body.getPosition().y - 12 / PPM,
            AnimationSetRat.FRAME_WIDTH / PPM,
            AnimationSetRat.FRAME_HEIGHT / PPM
        );
        drawHealthBar(batch);
    }

    protected void defineEnemy(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(14f / PPM, 10f / PPM);

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
        float xOffset = isFacingLeft ? -16f / PPM : 16f / PPM;
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

    @Override
    public void moveToPlayer(Vector2 playerPosition) {
        Vector2 direction = playerPosition.cpy().sub(body.getPosition()).nor();
        direction.scl(MOVEMENT_SPEED);

        body.setLinearVelocity(direction.x, body.getLinearVelocity().y);

        if (direction.len() > 0) {
            currentState = AnimationSetRat.RatAnimationType.MOVE_1;
        } else {
            currentState = AnimationSetRat.RatAnimationType.IDLE;
        }
    }

    @Override
    public void setAnimation(AnimationSetRat.RatAnimationType type) {
        currentState = type;
        stateTime = 0;
    }

    @Override
    public boolean isDeathAnimationComplete() {
        return animationSet.getAnimation(AnimationSetRat.RatAnimationType.DEATH_1).isAnimationFinished(stateTime);
    }

    @Override
    public boolean isAttackAnimationFinished() {
        return animationSet.getAnimation(AnimationSetRat.RatAnimationType.ATTACK_1).isAnimationFinished(stateTime);
    }

    @Override
    public boolean isHitAnimationFinished() {
        return animationSet.getAnimation(AnimationSetRat.RatAnimationType.HIT).isAnimationFinished(stateTime);
    }

    @Override
    public void attack() {
        setAnimation(AnimationSetRat.RatAnimationType.ATTACK_1);
        ATTACK_SOUND.play();
        activateAttackCollider();
        updateAttackColliderPosition();
        startAttackCooldown(); // Start the cooldown after initiating the attack
    }

    @Override
    public void chase() {
        setAnimation(AnimationSetRat.RatAnimationType.MOVE_1);
    }

    @Override
    public void die() {
        setAnimation(AnimationSetRat.RatAnimationType.DEATH_1);
        disableCollision();
    }

    @Override
    public void hit() {
        setAnimation(AnimationSetRat.RatAnimationType.HIT);
        body.setLinearVelocity(0, body.getLinearVelocity().y); // Stop horizontal movement
    }

    @Override
    public void idle() {
        setAnimation(AnimationSetRat.RatAnimationType.IDLE);
        body.setLinearVelocity(0, 0);
    }

}

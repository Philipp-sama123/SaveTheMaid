package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.BaseFriendAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetFemaleAgent;
import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetFemaleAgent.AnimationType;
import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;
import krazy.cat.games.SaveTheMaid.Projectile;

public class Player {
    // --- Managers ---
    private final PlayerEffectManager playerEffectManager;
    private final PlayerSoundManager playerSoundManager;
    private PlayerColliderManager playerColliderManager;

    // --- Constants ---
    private static final int MAX_JUMPS = 2;
    private static final float SLIDE_IMPULSE = 1.5f;
    private static final float SLIDE_DURATION = 1.0f;
    private static final float PROJECTILE_VELOCITY_X = 2.0f;
    private static final float PROJECTILE_VELOCITY_Y = 1.5f;
    private static final float DEATH_SCREEN_DELAY = 5.0f; // Delay before showing death screen

    // --- Movement speeds (in meters per second) ---
    private final float slowSpeed = 25 / PPM;
    private final float walkSpeed = 50 / PPM;
    private final float runSpeed = 100 / PPM;

    // --- Fields ---
    private BaseLevel baseLevel = null; // TODO REMOVE!
    private World world;
    private Body body;
    private AnimationSetFemaleAgent animationSetAgent;
    // ToDo Projectile Manager
    private Array<Projectile> projectiles;
    private Texture projectileTexture;

    // Health
    private final int maxHealth = 100;
    private int currentHealth = maxHealth;

    private float deathTimer = 0f;

    // Movement/State flagsØ
    private int jumpCount = 0;
    private int groundedCount = 0;
    private boolean isShooting = false;
    private boolean isShootingUp = false;
    protected boolean isFacingRight = false;
    private boolean isCrouching = false;
    private boolean isSliding = false;
    private boolean isDead = false;
    private float slideTime;
    private float stateTime;
    private AnimationType currentAnimationState = AnimationType.IDLE;
    // (lastFootstepFrame is now handled inside PlayerSoundManager)

    private BaseFriendAICharacter friendAICharacter;

    // --- Constructors ---
    public Player(World world) {
        this.world = world;
        playerEffectManager = new PlayerEffectManager(this);
        playerSoundManager = new PlayerSoundManager(this);
        definePlayer();
    }

    public Player(World world, BaseLevel baseLevel) {
        this(world);
        this.baseLevel = baseLevel;
    }

    // --- Initialization Methods ---
    private void definePlayer() {
        animationSetAgent = new AnimationSetFemaleAgent(
            GameAssetManager.getInstance().get(AssetPaths.PLAYER_TEXTURE, Texture.class)
        );
        playerEffectManager.loadEffects();
        // Create the Box2D body.
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(100 / PPM, 100 / PPM); // Convert to meters
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        // ToDo: projectileManager
        // Initialize projectiles and load the projectile texture.
        projectileTexture = GameAssetManager.getInstance().get(AssetPaths.AGENT_PIXEL_BULLET_TEXTURE, Texture.class);
        projectiles = new Array<>();

        // Delegate collider creation to the PlayerColliderManager.
        playerColliderManager = new PlayerColliderManager(this, world, body);
    }

    // --- Update & Draw Methods ---
    public void update(float delta) {
        if (isDead) {
            Animation<TextureRegion> deathAnimation = animationSetAgent.getCurrentFrame(currentAnimationState);
            float deathAnimDuration = deathAnimation.getAnimationDuration();
            stateTime = Math.min(stateTime + delta, deathAnimDuration);
            deathTimer += delta;
            if (deathTimer >= DEATH_SCREEN_DELAY && baseLevel != null) {
                baseLevel.showGameOverScreen();
            }
        } else {
            stateTime += delta;
        }

        // Handle footstep sounds.
        Animation<TextureRegion> currentAnim = animationSetAgent.getCurrentFrame(currentAnimationState);
        playerSoundManager.handleFootstepSound(currentAnimationState, currentAnim, stateTime);

        // ToDo: projectileManager
        updateProjectiles(delta);

        if (isShooting) {
            handleShootingAnimation();
        }
        if (isShootingUp) {
            handleShootingUpAnimation();
        }
        playerEffectManager.updateEffects(delta);

        if (isSliding) {
            slideTime += delta;
            if (slideTime >= SLIDE_DURATION) {
                isSliding = false;
                playerColliderManager.restoreCollider();
            }
        }
    }

    public void draw(Batch batch) {
        setBatchColorForDamage(batch);
        drawAnimation(batch, getCurrentFrame());
        batch.setColor(1, 1, 1, 1);

        // ToDo: projectileManager
        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }
        playerEffectManager.drawEffects(batch);
    }

    // --- Player Actions ---
    public void jump() {
        if (isSliding) return;
        if (jumpCount < MAX_JUMPS) {
            body.setLinearVelocity(body.getLinearVelocity().x, 1.5f);
            jumpCount++;
            stateTime = 0;
            currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
            playerSoundManager.playJumpSound();
            playerEffectManager.triggerJumpEffect();
            if (friendAICharacter != null) {
                friendAICharacter.jump();
            }
        }
    }

    public void slide() {
        if (isSliding || isDead) return;
        playerColliderManager.rotateColliderForSlide();
        isSliding = true;
        slideTime = 0;
        float slideImpulseX = isFacingRight ? SLIDE_IMPULSE : -SLIDE_IMPULSE;
        body.applyLinearImpulse(new Vector2(slideImpulseX, 0), body.getWorldCenter(), true);
        currentAnimationState = isShooting ? AnimationType.SLIDE_SHOOT : AnimationType.SLIDE;

        playerSoundManager.playSlideSound();
        playerEffectManager.triggerSlideEffect();

        if (friendAICharacter != null) {
            friendAICharacter.slide();
        }
    }

    public void shootUp() {
        if (isGrounded() && !isShootingUp && !isShooting && !isSliding) {
            isShootingUp = true;
            stateTime = 0f;
            // ToDo ProjectileManager
            Vector2 position = body.getPosition().cpy().add(0, 40 / PPM);
            Vector2 velocity = new Vector2(0, PROJECTILE_VELOCITY_Y);
            projectiles.add(new Projectile(world, position, velocity, projectileTexture));

            playerSoundManager.playShootSound();
        }
    }

    public void shoot() {
        if (!isShooting && !isShootingUp) {
            isShooting = true;
            stateTime = 0f;
            playerSoundManager.playShootSound();
            // ToDo: projectileManager
            Vector2 position = body.getPosition().cpy().add(
                isFacingRight ? 20 / PPM : -20 / PPM,
                isCrouching ? 2 / PPM : 10 / PPM);
            Vector2 velocity = new Vector2(isFacingRight ? PROJECTILE_VELOCITY_X : -PROJECTILE_VELOCITY_X, 0);
            if (isSliding) {
                position.y -= 16 / PPM;
                position.x += isFacingRight ? 16 / PPM : -16 / PPM;
                currentAnimationState = AnimationType.SLIDE_SHOOT;
            }
            projectiles.add(new Projectile(world, position, velocity, projectileTexture));
        }
    }

    /**
     * Called when the player takes damage.
     */
    private void takeDamage(float damage) {
        if (isDead) return;
        playerSoundManager.playHitSound();
        currentHealth -= damage;
        playerEffectManager.triggerBloodEffect();
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            onDeath();
        }
    }

    private void onDeath() {
        jumpCount = 0;
        isShooting = false;
        isShootingUp = false;
        currentAnimationState = AnimationType.DEATH;
        stateTime = 0;
        body.setLinearVelocity(0, body.getLinearVelocity().y);
    }

    public void crouch(boolean isCrouching) {
        if (isDead) return;
        this.isCrouching = isCrouching;
        if (isCrouching) {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
    }

    public void move(float moveInput) {
        if (isDead || isSliding) return;
        float accelerationFactor = 0.1f;
        float targetSpeed = !isCrouching ? calculateTargetSpeed(moveInput) : 0;
        smoothSpeedTransition(targetSpeed, accelerationFactor);
        if (moveInput != 0) {
            isFacingRight = moveInput > 0;
            adjustFrameOrientation();
        }
        updateAnimationStateBasedOnMovement();
    }

    public void onStartEnemyAttackCollision() {
        if (!isSliding) {
            takeDamage(5);
        }
    }

    // --- Animation & Rendering Helpers ---
    private TextureRegion getCurrentFrame() {
        boolean looping = currentAnimationState != AnimationType.DEATH;
        return animationSetAgent.getCurrentFrame(currentAnimationState, stateTime, looping);
    }

    private void drawAnimation(Batch batch, TextureRegion currentFrame) {
        float posX = isFacingRight ? body.getPosition().x - 26 / PPM : body.getPosition().x - 36 / PPM;
        float posY = body.getPosition().y - 24 / PPM;
        if (isSliding) {
            batch.draw(currentFrame, posX, body.getPosition().y - 36 / PPM, 64 / PPM, 64 / PPM);
        } else {
            batch.draw(currentFrame, posX, posY, 64 / PPM, 64 / PPM);
        }
    }

    private void setBatchColorForDamage(Batch batch) {
        if (playerEffectManager.isShowBloodEffect() && !isDead) {
            batch.setColor(1, 0, 0, 1);
        } else {
            batch.setColor(1, 1, 1, 1);
        }
    }

    private void adjustFrameOrientation() {
        if (isFacingRight != animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        }
    }

    private void updateAnimationStateBasedOnMovement() {
        float currentVelocityX = body.getLinearVelocity().x;
        float currentVelocityY = body.getLinearVelocity().y;
        if (isSliding) {
            currentAnimationState = isShooting ? AnimationType.SLIDE_SHOOT : AnimationType.SLIDE;
        } else if (isShootingUp) {
            if (currentVelocityY > 0) {
                currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
            } else if (currentVelocityY < 0) {
                currentAnimationState = isShooting ? AnimationType.FALL_SHOOT : AnimationType.FALL;
            } else if (currentVelocityX != 0) {
                boolean isRunning = Math.abs(currentVelocityX) >= (runSpeed - 10 / PPM);
                currentAnimationState = isRunning ? AnimationType.RUN_SHOOT_UP : AnimationType.WALK_SHOOT_UP;
            } else {
                currentAnimationState = isCrouching ? AnimationType.CROUCH_SHOOT_UP : AnimationType.STAND_SHOOT_UP;
            }
        } else {
            if (currentVelocityY > 0.f) {
                currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
            } else if (currentVelocityY < 0.f) {
                currentAnimationState = isShooting ? AnimationType.FALL_SHOOT : AnimationType.FALL;
            } else if (currentVelocityX != 0.f) {
                boolean isRunning = Math.abs(currentVelocityX) >= (runSpeed - 10 / PPM);
                currentAnimationState = isShooting
                    ? (isRunning ? AnimationType.RUN_SHOOT : AnimationType.WALK_SHOOT)
                    : (isRunning ? AnimationType.RUN : AnimationType.WALK);
            } else {
                currentAnimationState = isCrouching
                    ? (isShooting ? AnimationType.CROUCH_SHOOT : AnimationType.CROUCH_IDLE)
                    : (isShooting ? AnimationType.STAND_SHOOT : AnimationType.IDLE);
            }
        }
    }

    // --- Shooting Animation Helpers ---
    private void handleShootingAnimation() {
        Animation<TextureRegion> shootAnimation = animationSetAgent.getCurrentFrame(currentAnimationState);
        if (shootAnimation.isAnimationFinished(stateTime)) {
            isShooting = false;
            stateTime = 0f;
            updateAnimationStateBasedOnMovement();
        }
    }

    private void handleShootingUpAnimation() {
        Animation<TextureRegion> shootAnimation = animationSetAgent.getCurrentFrame(currentAnimationState);
        if (shootAnimation.isAnimationFinished(stateTime)) {
            isShootingUp = false;
            stateTime = 0f;
            updateAnimationStateBasedOnMovement();
        }
    }

    // --- Movement Helpers ---
    private float calculateTargetSpeed(float moveInput) {
        float absInput = Math.abs(moveInput);
        float targetSpeed = 0f;
        if (absInput > 0.05f && absInput < 0.5f) {
            targetSpeed = slowSpeed * Math.signum(moveInput);
        } else if (absInput > 0.5f && absInput < 0.75f) {
            targetSpeed = walkSpeed * Math.signum(moveInput);
        } else if (absInput > 0.75f) {
            targetSpeed = runSpeed * Math.signum(moveInput);
        }
        if (!isGrounded()) {
            targetSpeed /= 2;
        }
        return targetSpeed;
    }

    private void smoothSpeedTransition(float targetSpeed, float factor) {
        float currentVelocityX = body.getLinearVelocity().x;
        float smoothedVelocityX = currentVelocityX + (targetSpeed - currentVelocityX) * factor;
        body.setLinearVelocity(smoothedVelocityX, body.getLinearVelocity().y);
    }

    // --- Grounded & Friend Helpers ---
    public boolean isGrounded() {
        return groundedCount > 0;
    }

    public void increaseGroundedCount() {
        jumpCount = 0;
        groundedCount++;
    }

    public void decreaseGroundedCount() {
        if (groundedCount > 0) {
            groundedCount--;
        }
    }

    public void setFriendReference(BaseFriendAICharacter friend) {
        friendAICharacter = friend;
    }

    public void removeFriend() {
        friendAICharacter = null;
    }

    public void appleHeal() {
        currentHealth = maxHealth;
    }

    // --- Projectile & Body Accessors ---
    // ToDo: projectileManager
    private void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);
            if (projectile.isDestroyed()) {
                projectiles.removeIndex(i);
            }
        }
    }

    // ToDo: projectileManager
    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

    public Body getBody() {
        return body;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void die() {
        if (isDead) return;
        takeDamage(100);
        playerEffectManager.triggerDestroyEffect();
    }
}

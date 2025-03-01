package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.JUMP_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.PLAYER_FOOTSTEP_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.PLAYER_HIT_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.PLAYER_SLIDE_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.SHOOT_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.Utils.createAnimation;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PLAYER;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.BaseFriendAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetFemaleAgent;
import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetFemaleAgent.AnimationType;
import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;
import krazy.cat.games.SaveTheMaid.Projectile;

// ToDo: PlayerEffectsManager (!)
public class Player {

    // --- Constants ---
    private static final int MAX_JUMPS = 2;
    private static final float SLIDE_IMPULSE = 1.5f;
    private static final float SLIDE_DURATION = 1.0f;
    private static final float PROJECTILE_VELOCITY_X = 2.0f;
    private static final float PROJECTILE_VELOCITY_Y = 1.5f;
    private static final int JUMP_EFFECT_Y_OFFSET = 25;
    private static final float SLIDE_COLLIDER_VERTICAL_OFFSET = -32 / PPM;
    private static final float BLOOD_EFFECT_SCALE = 0.5f; // 50% of original size
    private static final float JUMP_EFFECT_SCALE = 1.5f;
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
    private Array<Projectile> projectiles;
    private Texture projectileTexture;

    // Health
    private final int maxHealth = 100;
    private int currentHealth = maxHealth;

    // Effect animations and timers
    private Animation<TextureRegion> jumpEffectAnimation;
    private Animation<TextureRegion> bloodEffectAnimation;
    private Animation<TextureRegion> slideEffectAnimation;
    private Animation<TextureRegion> destroyEffectAnimation;

    private boolean showJumpEffect;
    private boolean showSlideEffect;
    private boolean showBloodEffect;
    private boolean showDestroyEffect;

    private float jumpEffectTime;
    private float bloodEffectTime;
    private float slideEffectTime;
    private float destroyEffectTime;

    private float deathTimer = 0f;

    // Movement/State flags
    private int jumpCount = 0;
    private int groundedCount = 0;
    private boolean isShooting = false;
    private boolean isShootingUp = false;
    private boolean isFacingRight = false;
    private boolean isCrouching = false;
    private boolean isSliding = false;
    private boolean isDead = false;
    private float slideTime;
    private float stateTime;
    private AnimationType currentAnimationState = AnimationType.IDLE;
    private int lastFootstepFrame = -1; // To prevent duplicate sound triggering

    // Sounds
    private Sound jumpSound;
    private Sound hitSound;
    private Sound shootSound;
    private Sound footstepSound;
    private Sound slideSound;

    private BaseFriendAICharacter friendAICharacter;

    // --- Constructors ---
    public Player(World world) {
        this.world = world;
        initializePlayerAssets();
    }

    public Player(World world, BaseLevel baseLevel) {
        this(world);
        this.baseLevel = baseLevel;
    }

    // --- Initialization Methods ---
    private void initializePlayerAssets() {
        animationSetAgent = new AnimationSetFemaleAgent(
            GameAssetManager.getInstance().get(AssetPaths.PLAYER_TEXTURE, Texture.class)
        );

        loadEffects();
        definePlayer();
        initializeSounds();
    }

    private void loadEffects() {
        Texture jumpSpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_JUMP_EFFECT_TEXTURE, Texture.class);
        Texture bloodSpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_BLOOD_EFFECT_TEXTURE, Texture.class);
        Texture slideSmokeSpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_SLIDE_EFFECT_TEXTURE, Texture.class);
        Texture destroySpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_DESTROY_EFFECT_TEXTURE, Texture.class);

        jumpEffectAnimation = createAnimation(jumpSpriteSheet, 32, 16, 10, 0.05f);
        bloodEffectAnimation = createAnimation(bloodSpriteSheet, 110, 86, 7, 0.1f);
        slideEffectAnimation = createAnimation(slideSmokeSpriteSheet, 48, 32, 9, 0.1f);
        destroyEffectAnimation = createAnimation(destroySpriteSheet, 64, 64, 13, 0.1f);
    }

    private void initializeSounds() {
        jumpSound = GameAssetManager.getInstance().getAssetManager().get(JUMP_SOUND);
        shootSound = GameAssetManager.getInstance().getAssetManager().get(SHOOT_SOUND);
        hitSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_HIT_SOUND);
        footstepSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_FOOTSTEP_SOUND);
        slideSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_SLIDE_SOUND);
    }

    private void definePlayer() {
        // Body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(100 / PPM, 100 / PPM); // Convert to meters
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        // Initialize projectiles and texture
        projectileTexture = GameAssetManager.getInstance().get(AssetPaths.AGENT_PIXEL_BULLET_TEXTURE, Texture.class);
        projectiles = new Array<>();

        // --- Create Capsule-like Collider ---
        // Central rectangle
        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(8f / PPM, 16f / PPM);
        FixtureDef rectFixtureDef = new FixtureDef();
        rectFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        rectFixtureDef.filter.maskBits = MASK_PLAYER;
        rectFixtureDef.shape = rectShape;
        body.createFixture(rectFixtureDef).setUserData(this);
        rectShape.dispose();

        // Top circle
        CircleShape topCircle = new CircleShape();
        topCircle.setRadius(8f / PPM);
        topCircle.setPosition(new Vector2(0, 16f / PPM));
        FixtureDef topCircleFixtureDef = new FixtureDef();
        topCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        topCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        topCircleFixtureDef.shape = topCircle;
        body.createFixture(topCircleFixtureDef).setUserData(this);
        topCircle.dispose();

        // Bottom circle
        CircleShape bottomCircle = new CircleShape();
        bottomCircle.setRadius(8f / PPM);
        bottomCircle.setPosition(new Vector2(0, -16f / PPM));
        FixtureDef bottomCircleFixtureDef = new FixtureDef();
        bottomCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        bottomCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        bottomCircleFixtureDef.shape = bottomCircle;
        body.createFixture(bottomCircleFixtureDef).setUserData(this);
        bottomCircle.dispose();
    }

    // --- Update & Draw Methods ---
    public void update(float delta) {
        if (isDead) {
            Animation<TextureRegion> deathAnimation = animationSetAgent.getCurrentFrame(currentAnimationState);
            float deathAnimDuration = deathAnimation.getAnimationDuration();

            stateTime = Math.min(stateTime + delta, deathAnimDuration);
            deathTimer += delta; // accumulate death timer

            if (deathTimer >= DEATH_SCREEN_DELAY && baseLevel != null) {
                baseLevel.showGameOverScreen();
            }
        } else {
            stateTime += delta;
        }

        handleFootstepSound();
        updateProjectiles(delta);

        if (isShooting) {
            handleShootingAnimation();
        }
        if (isShootingUp) {
            handleShootingUpAnimation();
        }

        updateEffects(delta);

        if (isSliding) {
            slideTime += delta;
            if (slideTime >= SLIDE_DURATION) {
                isSliding = false;
                restoreCollider();
            }
        }
    }

    /**
     * Update the timers for the jump, slide, and blood effects.
     */
    private void updateEffects(float delta) {
        if (showJumpEffect) {
            jumpEffectTime += delta;
            if (jumpEffectAnimation.isAnimationFinished(jumpEffectTime)) {
                showJumpEffect = false;
            }
        }
        if (showSlideEffect) {
            slideEffectTime += delta;
            if (slideEffectAnimation.isAnimationFinished(slideEffectTime)) {
                showSlideEffect = false;
            }
        }
        if (showBloodEffect) {
            bloodEffectTime += delta;
            if (bloodEffectAnimation.isAnimationFinished(bloodEffectTime)) {
                showBloodEffect = false;
            }
        }
        if (showDestroyEffect) {
            destroyEffectTime += delta;
            if (destroyEffectAnimation.isAnimationFinished(destroyEffectTime)) {
                showDestroyEffect = false;
            }
        }
    }

    public void draw(Batch batch) {
        setBatchColorForDamage(batch);
        drawAnimation(batch, getCurrentFrame());
        batch.setColor(1, 1, 1, 1);

        // Draw projectiles
        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }

        // Draw slide effect
        if (showSlideEffect) {
            TextureRegion slideEffectFrame = slideEffectAnimation.getKeyFrame(slideEffectTime);
            // Ensure correct horizontal orientation
            if ((isFacingRight && slideEffectFrame.isFlipX()) || (!isFacingRight && !slideEffectFrame.isFlipX())) {
                slideEffectFrame.flip(true, false);
            }
            float effectPosX = (body.getPosition().x) - (slideEffectFrame.getRegionWidth() / 2f / PPM)
                - (isFacingRight ? 20 / PPM : -20 / PPM);
            float effectPosY = (body.getPosition().y) - 25 / PPM;
            batch.draw(slideEffectFrame, effectPosX, effectPosY,
                slideEffectFrame.getRegionWidth() / PPM, slideEffectFrame.getRegionHeight() / PPM);
        }

        // Draw jump effect
        if (showJumpEffect) {
            TextureRegion jumpEffectFrame = jumpEffectAnimation.getKeyFrame(jumpEffectTime);
            float effectPosX = body.getPosition().x - (jumpEffectFrame.getRegionWidth() * JUMP_EFFECT_SCALE / 2f / PPM);
            float effectPosY = body.getPosition().y - JUMP_EFFECT_Y_OFFSET / PPM;
            batch.draw(jumpEffectFrame, effectPosX, effectPosY,
                jumpEffectFrame.getRegionWidth() * JUMP_EFFECT_SCALE / PPM,
                jumpEffectFrame.getRegionHeight() * JUMP_EFFECT_SCALE / PPM);
        }

        // Draw blood effect
        if (showBloodEffect) {
            TextureRegion bloodEffectFrame = bloodEffectAnimation.getKeyFrame(bloodEffectTime);
            float effectPosX = body.getPosition().x - ((bloodEffectFrame.getRegionWidth() / PPM) * BLOOD_EFFECT_SCALE / 2);
            float effectPosY = body.getPosition().y;
            batch.draw(bloodEffectFrame, effectPosX, effectPosY,
                (bloodEffectFrame.getRegionWidth() / PPM) * BLOOD_EFFECT_SCALE,
                (bloodEffectFrame.getRegionHeight() / PPM) * BLOOD_EFFECT_SCALE);
        }
        // Draw blood effect
        if (showDestroyEffect) {
            TextureRegion destroyEffectFrame = destroyEffectAnimation.getKeyFrame(destroyEffectTime);
            float effectPosX = body.getPosition().x - (destroyEffectFrame.getRegionWidth() / 2f / PPM);
            float effectPosY = body.getPosition().y - (destroyEffectFrame.getRegionHeight() / 2f / PPM);
            batch.draw(destroyEffectFrame, effectPosX, effectPosY,
                (destroyEffectFrame.getRegionWidth() / PPM),
                (destroyEffectFrame.getRegionHeight() / PPM));
        }
    }

    // --- Player Actions ---
    public void jump() {
        if (isSliding) return;
        if (jumpCount < MAX_JUMPS) {
            body.setLinearVelocity(body.getLinearVelocity().x, 1.5f);
            jumpCount++;
            stateTime = 0;
            currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
            if (jumpSound != null) {
                jumpSound.play(.25f);
            }
            jumpEffectTime = 0;
            showJumpEffect = true;
            if (friendAICharacter != null) {
                friendAICharacter.jump();
            }
        }
    }

    public void slide() {
        if (isSliding || isDead) return;

        rotateColliderForSlide();
        isSliding = true;
        slideTime = 0;

        float slideImpulseX = isFacingRight ? SLIDE_IMPULSE : -SLIDE_IMPULSE;
        body.applyLinearImpulse(new Vector2(slideImpulseX, 0), body.getWorldCenter(), true);
        currentAnimationState = isShooting ? AnimationType.SLIDE_SHOOT : AnimationType.SLIDE;
        if (slideSound != null) {
            slideSound.play(.25f);
        }
        slideEffectTime = 0;
        showSlideEffect = true;
        if (friendAICharacter != null) {
            friendAICharacter.slide();
        }
    }

    /**
     * Shoots a projectile upward if the player is on the ground and not busy with other actions.
     */
    public void shootUp() {
        if (isGrounded() && !isShootingUp && !isShooting && !isSliding) {
            isShootingUp = true;
            stateTime = 0f;
            Vector2 position = body.getPosition().cpy().add(0, 40 / PPM);
            Vector2 velocity = new Vector2(0, PROJECTILE_VELOCITY_Y);
            shootSound.play();
            projectiles.add(new Projectile(world, position, velocity, projectileTexture));
        }
    }

    public void shoot() {
        if (!isShooting && !isShootingUp) {
            isShooting = true;
            stateTime = 0f;
            Vector2 position = body.getPosition().cpy().add(isFacingRight ? 20 / PPM : -20 / PPM,
                isCrouching ? 2 / PPM : 10 / PPM);
            Vector2 velocity = new Vector2(isFacingRight ? PROJECTILE_VELOCITY_X : -PROJECTILE_VELOCITY_X, 0);

            if (isSliding) {
                position.y -= 16 / PPM;
                position.x += isFacingRight ? 16 / PPM : -16 / PPM;
                currentAnimationState = AnimationType.SLIDE_SHOOT;
            }
            shootSound.play();
            projectiles.add(new Projectile(world, position, velocity, projectileTexture));
        }
    }

    /**
     * Called when the player takes damage.
     */
    private void takeDamage(float damage) {
        if (isDead) return;
        hitSound.play();
        currentHealth -= damage;
        showBloodEffect = true;
        bloodEffectTime = 0;
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

    // --- Collider Handling ---

    /**
     * Rotates the collider to a horizontal (sliding) orientation.
     */
    private void rotateColliderForSlide() {
        Array<Fixture> fixturesToDestroy = new Array<>();
        for (Fixture fixture : body.getFixtureList()) {
            fixturesToDestroy.add(fixture);
        }
        for (Fixture fixture : fixturesToDestroy) {
            body.destroyFixture(fixture);
        }
        PolygonShape rotatedShape = new PolygonShape();
        rotatedShape.setAsBox(24f / PPM, 8f / PPM, new Vector2(0, SLIDE_COLLIDER_VERTICAL_OFFSET / 2), 0);
        FixtureDef slideFixtureDef = new FixtureDef();
        slideFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        slideFixtureDef.filter.maskBits = MASK_PLAYER;
        slideFixtureDef.shape = rotatedShape;
        body.createFixture(slideFixtureDef).setUserData(this);
        rotatedShape.dispose();
    }

    /**
     * Restores the original collider shape (capsule shape).
     */
    private void restoreCollider() {
        Array<Fixture> fixturesToDestroy = new Array<>(body.getFixtureList());
        for (Fixture fixture : fixturesToDestroy) {
            body.destroyFixture(fixture);
        }
        // Central rectangle
        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(8f / PPM, 16f / PPM);
        FixtureDef rectFixtureDef = new FixtureDef();
        rectFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        rectFixtureDef.filter.maskBits = MASK_PLAYER;
        rectFixtureDef.shape = rectShape;
        body.createFixture(rectFixtureDef).setUserData(this);
        rectShape.dispose();

        // Top circle
        CircleShape topCircle = new CircleShape();
        topCircle.setRadius(8f / PPM);
        topCircle.setPosition(new Vector2(0, 16f / PPM));
        FixtureDef topCircleFixtureDef = new FixtureDef();
        topCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        topCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        topCircleFixtureDef.shape = topCircle;
        body.createFixture(topCircleFixtureDef).setUserData(this);
        topCircle.dispose();

        // Bottom circle
        CircleShape bottomCircle = new CircleShape();
        bottomCircle.setRadius(8f / PPM);
        bottomCircle.setPosition(new Vector2(0, -16f / PPM));
        FixtureDef bottomCircleFixtureDef = new FixtureDef();
        bottomCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        bottomCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        bottomCircleFixtureDef.shape = bottomCircle;
        body.createFixture(bottomCircleFixtureDef).setUserData(this);
        bottomCircle.dispose();
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
        if (showBloodEffect && !isDead) {
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
    private void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);
            if (projectile.isDestroyed()) {
                projectiles.removeIndex(i);
            }
        }
    }

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
        showDestroyEffect = true;
    }

    // --- Footstep Sound Handling ---
    private void handleFootstepSound() {
        Gdx.app.log("handleFootstepSound", "ENTRY");
        Gdx.app.log("handleFootstepSound", "currentAnimationState: " + currentAnimationState);

        if (currentAnimationState == AnimationType.WALK || currentAnimationState == AnimationType.RUN) {
            Animation<TextureRegion> currentAnimation = animationSetAgent.getCurrentFrame(currentAnimationState);
            if (stateTime > currentAnimation.getAnimationDuration()) {
                stateTime -= currentAnimation.getAnimationDuration();
                Gdx.app.log("StateTime Reset", "stateTime reset after animation loop");
            }
            int currentFrameIndex = currentAnimation.getKeyFrameIndex(stateTime);
            Gdx.app.log("handleFootstepSound", "currentFrameIndex: " + currentFrameIndex);

            if ((currentFrameIndex == 0 || currentFrameIndex == 4) && currentFrameIndex != lastFootstepFrame) {
                if (footstepSound != null) {
                    footstepSound.stop();
                    footstepSound.play(.25f,
                        currentAnimationState == AnimationType.WALK ? 0.f : .5f, 0.0f);
                }
                Gdx.app.log("TRIGGER", "FOOTSTEP PLAYING at frame: " + currentFrameIndex);
                lastFootstepFrame = currentFrameIndex;
            }

            if (currentAnimation.isAnimationFinished(stateTime)) {
                lastFootstepFrame = -1;
            }
        } else {
            lastFootstepFrame = -1;
        }
    }
}

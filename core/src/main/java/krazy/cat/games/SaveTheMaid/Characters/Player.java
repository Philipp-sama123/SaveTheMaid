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
import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;
import krazy.cat.games.SaveTheMaid.Projectile;

// ToDo: PlayerEffectsManager (!)
public class Player {
    private static final int MAX_JUMPS = 2;
    private static final float SLIDE_IMPULSE = 1.5f;
    private static final float SLIDE_DURATION = 1.f;
    private final float PROJECTILE_VELOCITY_X = 2.f;
    private final float PROJECTILE_VELOCITY_Y = 1.5f;
    private final int JUMP_EFFECT_Y_OFFSET = 40;
    private final int BLOOD_EFFECT_X_OFFSET = 25;
    private final float SLIDE_COLLIDER_VERTICAL_OFFSET = -32 / PPM;
    private BaseLevel baseLevel = null; // TODO REMOVE !

    private int jumpCount = 0;

    float slowSpeed = 25 / PPM;
    float walkSpeed = 50 / PPM;
    float runSpeed = 100 / PPM;

    private AnimationSetFemaleAgent animationSetAgent;
    public World world;
    private Body body;

    private float stateTime;
    private AnimationType currentAnimationState = AnimationType.IDLE;

    private Array<Projectile> projectiles;
    private Texture projectileTexture;

    // Effects
    private Animation<TextureRegion> jumpEffectAnimation;
    private Animation<TextureRegion> bloodEffectAnimation;
    private Animation<TextureRegion> slideEffectAnimation;

    private boolean showJumpEffect;
    private boolean showSlideEffect;
    private boolean showBloodEffect;

    private float jumpEffectTime;
    private float bloodEffectTime;
    private float slideEffectTime;

    private final int maxHealth = 100;
    private int currentHealth = maxHealth;

    private boolean isShooting = false;
    private boolean isShootingUp = false;
    private boolean isFacingRight = false;
    private boolean isCrouching = false;
    private boolean isSliding = false;
    private boolean isDead = false;

    private int lastFootstepFrame = -1; // To prevent duplicate sound triggering
    private float slideTime;

    private Sound jumpSound;
    private Sound hitSound;
    private Sound shootSound;
    private Sound footstepSound;
    private Sound slideSound;
    private BaseFriendAICharacter friendAICharacter;
    private int groundedCount = 0;

    public boolean isGrounded() {
        return groundedCount > 0;
    }

    public void increaseGroundedCount() {
        jumpCount = 0; // mjeeh
        groundedCount++;
    }

    public void decreaseGroundedCount() {
        if (groundedCount > 0) groundedCount--;
    }

    public Player(World world) {
        this.world = world;
        initializePlayerAssets();
    }

    public Player(World world, BaseLevel baseLevel) {
        this(world);
        this.baseLevel = baseLevel;
    }

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

        jumpEffectAnimation = createAnimation(jumpSpriteSheet, 252, 40, 4, 0.1f);
        bloodEffectAnimation = createAnimation(bloodSpriteSheet, 110, 86, 5, 0.1f);
        slideEffectAnimation = createAnimation(slideSmokeSpriteSheet, 50, 32, 8, 0.1f);
    }

    public void update(float delta) {
        if (isDead) {
            handleDeath();
            // ToDo: fix the end of the animation somehow (!)
        }

        handleFootstepSound();
        stateTime += delta;

        updateProjectiles(delta);

        if (isShooting) {
            handleShootingAnimation();
        }
        if (isShootingUp) {
            handleShootingUpAnimation();
        }

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
        if (isSliding) {
            slideTime += delta;

            if (slideTime >= SLIDE_DURATION) {
                isSliding = false;
                restoreCollider();
            }
        }
    }

    private void definePlayer() {
        // Body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(100 / PPM, 100 / PPM); // Convert to meters
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        // Initialize the projectiles and texture
        projectileTexture = GameAssetManager.getInstance().get(AssetPaths.AGENT_PIXEL_BULLET_TEXTURE, Texture.class);
        projectiles = new Array<>();

        // Central rectangle shape
        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(8f / PPM, 16f / PPM); // Adjusted for capsule middle

        // Fixture for rectangle part of capsule
        FixtureDef playerFixtureDef = new FixtureDef();
        playerFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        playerFixtureDef.filter.maskBits = MASK_PLAYER;
        playerFixtureDef.shape = rectShape;
        body.createFixture(playerFixtureDef).setUserData(this);

        // Top circle
        CircleShape topCircle = new CircleShape();
        topCircle.setRadius(8f / PPM); // Radius matches rectangle width
        topCircle.setPosition(new Vector2(0, 16f / PPM)); // Centered above the rectangle

        // Fixture for top circle
        FixtureDef topCircleFixtureDef = new FixtureDef();
        topCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        topCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        topCircleFixtureDef.shape = topCircle;
        body.createFixture(topCircleFixtureDef).setUserData(this);

        // Bottom circle
        CircleShape bottomCircle = new CircleShape();
        bottomCircle.setRadius(8f / PPM); // Radius matches rectangle width
        bottomCircle.setPosition(new Vector2(0, -16f / PPM)); // Centered below the rectangle

        // Fixture for bottom circle
        FixtureDef bottomCircleFixtureDef = new FixtureDef();
        bottomCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        bottomCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        bottomCircleFixtureDef.shape = bottomCircle;
        body.createFixture(bottomCircleFixtureDef).setUserData(this);

        // Dispose of shapes to free memory
        rectShape.dispose();
        topCircle.dispose();
        bottomCircle.dispose();
    }

    private void initializeSounds() {
        jumpSound = GameAssetManager.getInstance().getAssetManager().get(JUMP_SOUND);
        shootSound = GameAssetManager.getInstance().getAssetManager().get(SHOOT_SOUND);
        hitSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_HIT_SOUND);
        footstepSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_FOOTSTEP_SOUND);
        slideSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_SLIDE_SOUND);
    }

    public void draw(Batch batch) {
        setBatchColorForDamage(batch);
        drawAnimation(batch, getCurrentUpperBodyFrame());
        batch.setColor(1, 1, 1, 1);

        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }
        // Draw the slide effect
        if (showSlideEffect) {
            TextureRegion slideEffectFrame = slideEffectAnimation.getKeyFrame(slideEffectTime);

            // Check if the current frame needs to be flipped
            if ((isFacingRight && slideEffectFrame.isFlipX()) || (!isFacingRight && !slideEffectFrame.isFlipX())) {
                slideEffectFrame.flip(true, false); // Flip horizontally
            }

            float effectPosX = (body.getPosition().x) - ((float) slideEffectFrame.getRegionWidth() / 2 / PPM) - (isFacingRight ? 20 / PPM : -20 / PPM);
            float effectPosY = (body.getPosition().y) - 25 / PPM;

            batch.draw(slideEffectFrame, effectPosX, effectPosY, slideEffectFrame.getRegionWidth() / PPM, slideEffectFrame.getRegionHeight() / PPM);
        }

        // Draw the jump effect
        if (showJumpEffect) {
            TextureRegion jumpEffectFrame = jumpEffectAnimation.getKeyFrame(jumpEffectTime);
            float effectPosX = (body.getPosition().x) - ((float) jumpEffectFrame.getRegionWidth() / 2 / PPM);

            float effectPosY = (body.getPosition().y) - JUMP_EFFECT_Y_OFFSET / PPM; // Position slightly below player
            batch.draw(jumpEffectFrame, effectPosX, effectPosY, jumpEffectFrame.getRegionWidth() / PPM, jumpEffectFrame.getRegionHeight() / PPM);
        }

        // Draw the blood effect
        if (showBloodEffect) {
            TextureRegion bloodEffectFrame = bloodEffectAnimation.getKeyFrame(bloodEffectTime);
            float effectPosX = body.getPosition().x - ((float) bloodEffectFrame.getRegionWidth() / 2 / PPM) + BLOOD_EFFECT_X_OFFSET / PPM;
            float effectPosY = body.getPosition().y;  // Place slightly below the player
            batch.draw(bloodEffectFrame, effectPosX, effectPosY, bloodEffectFrame.getRegionWidth() / PPM, bloodEffectFrame.getRegionHeight() / PPM);
        }

    }

    public void jump() {
        if (isSliding) return;
        if (jumpCount < MAX_JUMPS) {
            body.setLinearVelocity(body.getLinearVelocity().x, 1.5f); // Scaled jump velocity
            // Apply the impulse to the body's center
            //  body.applyLinearImpulse(new Vector2(0, 1.5f), body.getWorldCenter(), true);
            jumpCount++;
            stateTime = 0;
            currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;

            // Start jump effect animation
            if (jumpSound != null)
                jumpSound.play(.25f);
            jumpEffectTime = 0;
            showJumpEffect = true;
            if (friendAICharacter != null)
                friendAICharacter.jump();
        }
    }

    public void slide() {
        if (isSliding || isDead) return; // Do not slide if already sliding or dead

        // Rotate collider
        rotateColliderForSlide();

        isSliding = true;
        slideTime = 0;

        float slideImpulseX = isFacingRight ? SLIDE_IMPULSE : -SLIDE_IMPULSE;
        body.applyLinearImpulse(new Vector2(slideImpulseX, 0), body.getWorldCenter(), true);

        // Set animation state to slide or slide+shoot
        currentAnimationState = isShooting ? AnimationType.SLIDE_SHOOT : AnimationType.SLIDE;

        if (slideSound != null)
            slideSound.play(.25f);
        slideEffectTime = 0;
        showSlideEffect = true;
        if (friendAICharacter != null)
            friendAICharacter.slide();
    }

    // Method to rotate the collider for sliding
    private void rotateColliderForSlide() {
        // Remove the original fixture
        Array<Fixture> fixturesToDestroy = new Array<>();
        for (Fixture fixture : body.getFixtureList()) {
            fixturesToDestroy.add(fixture);
        }
        for (Fixture fixture : fixturesToDestroy) {
            body.destroyFixture(fixture);
        }
        // Create a new rotated fixture
        PolygonShape rotatedShape = new PolygonShape();

        rotatedShape.setAsBox(24f / PPM, 8f / PPM, new Vector2(0, SLIDE_COLLIDER_VERTICAL_OFFSET / 2), 0); // Dimensions flipped for a 90-degree rotation

        FixtureDef slideFixtureDef = new FixtureDef();
        slideFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        slideFixtureDef.filter.maskBits = MASK_PLAYER;
        slideFixtureDef.shape = rotatedShape;

        body.createFixture(slideFixtureDef).setUserData(this);
        rotatedShape.dispose();
    }

    public void removeFriend() {
        friendAICharacter = null;
    }

    // Restore the original collider
    private void restoreCollider() {
        // Remove all existing fixtures
        Array<Fixture> fixturesToDestroy = new Array<>(body.getFixtureList());
        for (Fixture fixture : fixturesToDestroy) {
            body.destroyFixture(fixture);
        }

        // Recreate the central rectangle shape
        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(8f / PPM, 16f / PPM); // Adjusted for the middle part of the capsule

        FixtureDef rectFixtureDef = new FixtureDef();
        rectFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        rectFixtureDef.filter.maskBits = MASK_PLAYER;
        rectFixtureDef.shape = rectShape;
        body.createFixture(rectFixtureDef).setUserData(this);
        rectShape.dispose();

        // Recreate the top circle
        CircleShape topCircle = new CircleShape();
        topCircle.setRadius(8f / PPM); // Matches rectangle width
        topCircle.setPosition(new Vector2(0, 16f / PPM)); // Positioned above the rectangle

        FixtureDef topCircleFixtureDef = new FixtureDef();
        topCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        topCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        topCircleFixtureDef.shape = topCircle;
        body.createFixture(topCircleFixtureDef).setUserData(this);
        topCircle.dispose();

        // Recreate the bottom circle
        CircleShape bottomCircle = new CircleShape();
        bottomCircle.setRadius(8f / PPM); // Matches rectangle width
        bottomCircle.setPosition(new Vector2(0, -16f / PPM)); // Positioned below the rectangle

        FixtureDef bottomCircleFixtureDef = new FixtureDef();
        bottomCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        bottomCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        bottomCircleFixtureDef.shape = bottomCircle;
        body.createFixture(bottomCircleFixtureDef).setUserData(this);
        bottomCircle.dispose();
    }


    private void onDeath() {
        // Reset jump, movement, or shooting states
        jumpCount = 0;
        isShooting = false;
        isShootingUp = false;

        // Set an animation type to dying or idle if you have one
        currentAnimationState = AnimationType.DEATH;  // Assuming a 'DEAD' animation type exists
        stateTime = 0;
        body.setLinearVelocity(0, body.getLinearVelocity().y);
    }

    private void handleDeath() {
        if (animationSetAgent.getCurrentFrame(currentAnimationState).isAnimationFinished(stateTime)) {
            stateTime = animationSetAgent.getCurrentFrame(currentAnimationState).getAnimationDuration();
            if (baseLevel != null) {
                baseLevel.showGameOverScreen();
            }
            return;
        }
        Gdx.app.log("handleDeath", "ToDo: Handle Death!!");
    }

    public void crouch(boolean isCrouching) {
        if (isDead)
            return;

        this.isCrouching = isCrouching;
        if (isCrouching)
            body.setLinearVelocity(0, body.getLinearVelocity().y);
    }

    public void move(float moveInput) {
        if (isDead || isSliding)
            return;

        float accelerationFactor = 0.1f;

        // Determine the target speed based on input strength
        if (!isCrouching) {
            float targetSpeed = calculateTargetSpeed(moveInput);
            smoothSpeedTransition(targetSpeed, accelerationFactor);
        } else {
            smoothSpeedTransition(0, accelerationFactor);
        }

        // Update facing direction and animation state
        if (moveInput != 0) {
            isFacingRight = moveInput > 0;
            adjustFrameOrientation();
        }

        updateAnimationStateBasedOnMovement();
    }


    public void shootUp() {
        if (isGrounded() && !isShootingUp && !isShooting && !isSliding) {
            isShootingUp = true;
            stateTime = 0f;

            // Spawn above the character and set velocity straight up
            Vector2 position = body.getPosition().add(0, 40 / PPM); // Adjust height if necessary
            Vector2 velocity = new Vector2(0, PROJECTILE_VELOCITY_Y);           // Set to move vertically up
            shootSound.play();
            projectiles.add(new Projectile(world, position, velocity, projectileTexture));
        }
    }

    public void shoot() {
        if (!isShooting && !isShootingUp) {
            isShooting = true;
            stateTime = 0f;

            // Determine position offset and velocity based on facing direction
            Vector2 position = body.getPosition().add(isFacingRight ? 20 / PPM : -20 / PPM, isCrouching ? 2 / PPM : 10 / PPM);

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

    private void takeDamage(float damage) {
        if (isDead) return;  // Ignore damage if already dead
        hitSound.play();
        currentHealth -= damage;
        showBloodEffect = true;  // Trigger blood effect for feedback
        bloodEffectTime = 0;

        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            onDeath();
        }
    }

    private TextureRegion getCurrentUpperBodyFrame() {
        return animationSetAgent.getCurrentFrame(currentAnimationState, stateTime, true);
    }


    private void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);
            if (projectile.isDestroyed()) {
                projectiles.removeIndex(i);
            }
        }
    }


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

    private float calculateTargetSpeed(float moveInput) {
        float targetSpeed = 0;
        float absMoveInput = Math.abs(moveInput);

        if (absMoveInput > 0.05f && absMoveInput < 0.5f) {
            targetSpeed = slowSpeed * Math.signum(moveInput);
        } else if (absMoveInput > 0.5f && absMoveInput < 0.75f) {
            targetSpeed = walkSpeed * Math.signum(moveInput);
        } else if (Math.abs(moveInput) > 0.75f) {
            targetSpeed = runSpeed * Math.signum(moveInput);
        }
        if (!isGrounded())
            targetSpeed /= 2;

        return targetSpeed;
    }

    private void smoothSpeedTransition(float targetSpeed, float factor) {
        float currentVelocityX = body.getLinearVelocity().x;
        float smoothedVelocityX = currentVelocityX + (targetSpeed - currentVelocityX) * factor;
        body.setLinearVelocity(new Vector2(smoothedVelocityX, body.getLinearVelocity().y));
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
                currentAnimationState = isShooting ? (isRunning ? AnimationType.RUN_SHOOT : AnimationType.WALK_SHOOT)
                    : (isRunning ? AnimationType.RUN : AnimationType.WALK);
            } else {
                currentAnimationState = isCrouching ? (isShooting ? AnimationType.CROUCH_SHOOT : AnimationType.CROUCH_IDLE)
                    : (isShooting ? AnimationType.STAND_SHOOT : AnimationType.IDLE);
            }
        }
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

    public void onStartEnemyAttackCollision() {
        if (!isSliding)
            takeDamage(5);  // Perform actual hit logic here, e.g., reducing health
    }

    public void setFriendReference(BaseFriendAICharacter friend) {
        friendAICharacter = friend;
    }

    public void appleHeal() {
        currentHealth = maxHealth;
    }

    private void handleFootstepSound() {
        Gdx.app.log("handleFootstepSound", "ENTRY");
        Gdx.app.log("handleFootstepSound", "currentAnimationState: " + currentAnimationState);

        if (currentAnimationState == AnimationType.WALK || currentAnimationState == AnimationType.RUN) {
            Animation<TextureRegion> currentAnimation = animationSetAgent.getCurrentFrame(currentAnimationState);
            Gdx.app.log("handleFootstepSound", "currentAnimation: " + currentAnimation);

            // Reset stateTime to prevent overflow and handle looping
            if (stateTime > currentAnimation.getAnimationDuration()) {
                stateTime -= currentAnimation.getAnimationDuration();
                Gdx.app.log("StateTime Reset", "stateTime reset after animation loop");
            }
            int currentFrameIndex = currentAnimation.getKeyFrameIndex(stateTime);
            Gdx.app.log("handleFootstepSound", "currentFrameIndex: " + currentFrameIndex);

            if ((currentFrameIndex == 0 || currentFrameIndex == 4) && currentFrameIndex != lastFootstepFrame) {
                if (footstepSound != null) {
                    footstepSound.stop();
                    footstepSound.play(.25f, currentAnimationState == AnimationType.WALK ? 0.f : .5f, 0.0f);
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
}

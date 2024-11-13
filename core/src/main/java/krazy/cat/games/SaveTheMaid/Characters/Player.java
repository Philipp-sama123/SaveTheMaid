package krazy.cat.games.SaveTheMaid.Characters;

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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import krazy.cat.games.SaveTheMaid.AnimationSetFemaleAgent;
import krazy.cat.games.SaveTheMaid.AnimationSetFemaleAgent.AnimationType;
import krazy.cat.games.SaveTheMaid.Projectile;

public class Player {
    private static final int MAX_JUMPS = 3;
    private int jumpCount = 0;

    float slowSpeed = 25;
    float walkSpeed = 50;
    float runSpeed = 100;

    private final AnimationSetFemaleAgent animationSetAgent;
    public World world;
    public Body body;

    private float stateTime;
    private AnimationType currentAnimationState = AnimationType.IDLE;

    public boolean isCrouching = false;
    private boolean isShooting = false;
    private boolean isShootingUp = false;
    private boolean isFacingRight = false;

    private Array<Projectile> projectiles;
    private Texture projectileTexture;
    private float damageTimer;

    private Animation<TextureRegion> jumpEffectAnimation;
    private float jumpEffectTime;
    private boolean showJumpEffect;
    private Animation<TextureRegion> bloodEffectAnimation;
    private float bloodEffectTime;
    private boolean showBloodEffect;

    public final int maxHealth = 100;         // Max health value
    public int currentHealth = maxHealth;
    private boolean isDead = false;

    private static final float SLIDE_IMPULSE = 700;        // Set desired slide speed
    private static final float SLIDE_DURATION = 1.f;    // Duration of the slide in seconds
    private boolean isSliding = false;                   // Check if currently sliding
    private float slideTime;                             // Track slide duration
    private Sound jumpSound;
    private Sound hitSound;
    private Sound shootSound;

    public Player(World world) {
        this.world = world;
        animationSetAgent = new AnimationSetFemaleAgent(
            new Texture("Characters/FemaleAgent/Body/Black.png"),
            new Texture("Characters/FemaleAgent/Feet/Red.png")
        );
        // Load jump sprite sheet and split into frames
        Texture jumpSpriteSheet = new Texture("JumpEffect.png");
        TextureRegion[][] tmpFrames = TextureRegion.split(jumpSpriteSheet, 252, 40);

        // Convert the 2D array to a 1D array of TextureRegion for Animation
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            jumpFrames.add(tmpFrames[0][i]);  // Assuming there's only one row with four frames
        }

        // Create the jump animation with a frame duration (adjust duration as needed)
        jumpEffectAnimation = new Animation<>(0.1f, jumpFrames, Animation.PlayMode.NORMAL);
        // Load jump sprite sheet and split into frames
        Texture bloodSpriteSheet = new Texture("PlayerBloodEffect.png");
        TextureRegion[][] tmpFramesBlood = TextureRegion.split(bloodSpriteSheet, 110, 86);

        Array<TextureRegion> bloodFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            bloodFrames.add(tmpFramesBlood[0][i]);  // Assuming there's only one row with four frames
        }
        bloodEffectAnimation = new Animation<>(0.1f, bloodFrames, Animation.PlayMode.NORMAL);

        definePlayer();
        initializeSounds();
    }

    public void update(float delta) {
        if (isDead) {
            handleDeath();
        }

        stateTime += delta;

        updateProjectiles(delta);
        checkGrounded();

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
            }
        }
    }

    private void definePlayer() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(100, 100);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        projectileTexture = new Texture("Characters/FemaleAgent/PixelBullet16x16.png");
        projectiles = new Array<>();

        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(8f, 24f);

        FixtureDef playerFixtureDef = new FixtureDef();
        playerFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        playerFixtureDef.filter.maskBits = MASK_PLAYER;
        playerFixtureDef.shape = rectShape;
        body.createFixture(playerFixtureDef).setUserData(this);

        rectShape.dispose();
    }

    private void initializeSounds() {
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Jump.wav"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("SFX/PlayerHit.wav"));
    }

    public void draw(Batch batch) {
        setBatchColorForDamage(batch);
        drawAnimation(batch, getCurrentUpperBodyFrame(), getCurrentLowerBodyFrame());
        batch.setColor(1, 1, 1, 1);

        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }
        // Draw the jump effect
        if (showJumpEffect) {
            TextureRegion jumpEffectFrame = jumpEffectAnimation.getKeyFrame(jumpEffectTime);
            float effectPosX = body.getPosition().x - (float) jumpEffectFrame.getRegionWidth() / 2;
            float effectPosY = body.getPosition().y - 40;  // Place slightly below the player
            batch.draw(jumpEffectFrame, effectPosX, effectPosY, jumpEffectFrame.getRegionWidth(), jumpEffectFrame.getRegionHeight());
        }
        // Draw the blood effect
        if (showBloodEffect) {
            TextureRegion bloodEffectFrame = bloodEffectAnimation.getKeyFrame(bloodEffectTime);
            float effectPosX = body.getPosition().x - (float) bloodEffectFrame.getRegionWidth() / 2 + 25;
            float effectPosY = body.getPosition().y;  // Place slightly below the player
            batch.draw(bloodEffectFrame, effectPosX, effectPosY, bloodEffectFrame.getRegionWidth(), bloodEffectFrame.getRegionHeight());
        }

    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }


    public void jump() {
        if (isSliding) return;
        if (jumpCount < MAX_JUMPS) {
            body.setLinearVelocity(body.getLinearVelocity().x, 500);
            jumpCount++;
            stateTime = 0;
            currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;

            // Start jump effect animation
            jumpEffectTime = 0;

            jumpSound.play();
            showJumpEffect = true;
        }
    }

    public void slide() {
        if (isSliding || isDead) return;  // Do not slide if already sliding or dead

        isSliding = true;
        slideTime = 0;

        float slideImpulseX = isFacingRight ? SLIDE_IMPULSE : -SLIDE_IMPULSE;
        body.applyLinearImpulse(new Vector2(slideImpulseX, 0), body.getWorldCenter(), true);        // Set animation state to slide or slide+shoot
        currentAnimationState = isShooting ? AnimationType.SLIDE_SHOOT : AnimationType.SLIDE;
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
        if (animationSetAgent.getUpperBodyAnimation(currentAnimationState).isAnimationFinished(stateTime)) {
            stateTime = animationSetAgent.getUpperBodyAnimation(currentAnimationState).getAnimationDuration();
            // Additional logic here, like setting the player's speed to zero or triggering game over
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
        boolean isGrounded = Math.abs(body.getLinearVelocity().y) < 0.01f;

        // Determine the target speed based on input strength
        if (!isCrouching) {
            float targetSpeed = calculateTargetSpeed(moveInput, isGrounded);
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
        boolean isGrounded = Math.abs(body.getLinearVelocity().y) < 0.01f;

        if (isGrounded && !isShootingUp && !isShooting && !isSliding) {
            isShootingUp = true;
            stateTime = 0f;

            // Spawn above the character and set velocity straight up
            Vector2 position = body.getPosition().add(0, 40); // Adjust height if necessary
            Vector2 velocity = new Vector2(0, 1000);           // Set to move vertically up
            shootSound.play();
            projectiles.add(new Projectile(world, position, velocity, projectileTexture));
        }
    }

    public void shoot() {
        if (!isShooting && !isShootingUp) {
            isShooting = true;
            stateTime = 0f;

            // Determine position offset and velocity based on facing direction
            Vector2 position = body.getPosition().add(isFacingRight ? 20 : -20, isCrouching ? 2 : 10);
            // TODO: MAYBE WITH AN IMPULSE (!)
            Vector2 velocity = new Vector2(isFacingRight ? 1000 : -1000, 0);

            if (isSliding) {
                position.y -= 16;
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
        return animationSetAgent.getUpperBodyFrame(currentAnimationState, stateTime, true);
    }

    private TextureRegion getCurrentLowerBodyFrame() {
        return animationSetAgent.getLowerBodyFrame(currentAnimationState, stateTime, true);
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


    private void checkGrounded() {
        if (Math.abs(body.getLinearVelocity().y) < 0.01f && jumpCount > 0) {
            jumpCount = 0;
        }
    }

    private void handleShootingAnimation() {

        Animation<TextureRegion> shootAnimation = animationSetAgent.getUpperBodyAnimation(currentAnimationState);
        if (shootAnimation.isAnimationFinished(stateTime)) {
            isShooting = false;
            stateTime = 0f;
            updateAnimationStateBasedOnMovement();
        }
    }

    private void handleShootingUpAnimation() {
        Animation<TextureRegion> shootAnimation = animationSetAgent.getUpperBodyAnimation(currentAnimationState);
        if (shootAnimation.isAnimationFinished(stateTime)) {
            isShootingUp = false;
            stateTime = 0f;
            updateAnimationStateBasedOnMovement();
        }
    }

    private float calculateTargetSpeed(float moveInput, boolean isGrounded) {
        float targetSpeed = 0;
        float absMoveInput = Math.abs(moveInput);

        if (absMoveInput > 0.05f && absMoveInput < 0.5f) {
            targetSpeed = slowSpeed * Math.signum(moveInput);
        } else if (absMoveInput > 0.5f && absMoveInput < 0.75f) {
            targetSpeed = walkSpeed * Math.signum(moveInput);
        } else if (Math.abs(moveInput) > 0.75f) {
            targetSpeed = runSpeed * Math.signum(moveInput);
        }
        if (!isGrounded)
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
        if (isFacingRight != animationSetAgent.isUpperBodyFramesFlipped()) {
            animationSetAgent.flipUpperBodyFramesHorizontally();
            animationSetAgent.flipLowerBodyFramesHorizontally();
        }
    }

    private void updateAnimationStateBasedOnMovement() {
        int currentVelocityX = Math.round(body.getLinearVelocity().x);
        int currentVelocityY = Math.round(body.getLinearVelocity().y);
        if (isSliding) {
            currentAnimationState = isShooting ? AnimationType.SLIDE_SHOOT : AnimationType.SLIDE;
        } else if (isShootingUp) {
            if (currentVelocityY > 0) {
                currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
            } else if (currentVelocityY < 0) {
                currentAnimationState = isShooting ? AnimationType.FALL_SHOOT : AnimationType.FALL;
            } else if (currentVelocityX != 0) {
                boolean isRunning = Math.abs(currentVelocityX) >= (runSpeed - 10);
                currentAnimationState = isRunning ? AnimationType.RUN_SHOOT_UP : AnimationType.WALK_SHOOT_UP;
            } else {
                currentAnimationState = isCrouching ? AnimationType.CROUCH_SHOOT_UP : AnimationType.STAND_SHOOT_UP;
            }
        } else {
            if (currentVelocityY > 0) {
                currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
            } else if (currentVelocityY < 0) {
                currentAnimationState = isShooting ? AnimationType.FALL_SHOOT : AnimationType.FALL;
            } else if (currentVelocityX != 0) {
                boolean isRunning = Math.abs(currentVelocityX) >= (runSpeed - 10);
                currentAnimationState = isShooting ? (isRunning ? AnimationType.RUN_SHOOT : AnimationType.WALK_SHOOT)
                    : (isRunning ? AnimationType.RUN : AnimationType.WALK);
            } else {
                currentAnimationState = isCrouching ? (isShooting ? AnimationType.CROUCH_SHOOT : AnimationType.CROUCH_IDLE)
                    : (isShooting ? AnimationType.STAND_SHOOT : AnimationType.IDLE);
            }
            Gdx.app.log("ANIM", currentAnimationState + " currentanim");
        }
    }

    private void drawAnimation(Batch batch, TextureRegion upperBodyFrame, TextureRegion lowerBodyFrame) {
        float posX = isFacingRight ? body.getPosition().x - 26 : body.getPosition().x - 36;
        float posY = body.getPosition().y - 24;
        if (isSliding) {
            batch.draw(upperBodyFrame, posX, body.getPosition().y - 36, 64, 64);
            batch.draw(lowerBodyFrame, posX, body.getPosition().y - 36, 64, 64);
        } else {
            batch.draw(upperBodyFrame, posX, posY, 64, 64);
            batch.draw(lowerBodyFrame, posX, posY, 64, 64);
        }
    }

    public void onStartEnemyAttackCollision() {
        if (!isSliding)
            takeDamage(5);  // Perform actual hit logic here, e.g., reducing health
    }

}

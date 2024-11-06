package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Gdx;
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

import krazy.cat.games.SaveTheMaid.AnimationSetFemaleAgent.AnimationType;

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

    private boolean isCrouching = false;
    private boolean isShooting = false;
    private boolean isShootingUp = false;
    private boolean isFacingRight = false;

    private Array<Projectile> projectiles;
    private Texture projectileTexture;
    private boolean damaged;
    private float damageTimer;

    public Player(World world) {
        this.world = world;
        animationSetAgent = new AnimationSetFemaleAgent(
            new Texture("Characters/FemaleAgent/Body/Black.png"),
            new Texture("Characters/FemaleAgent/Feet/Red.png")
        );
        definePlayer();
    }

    public void update(float delta) {
        stateTime += delta;
        updateProjectiles(delta);
        updateDamageEffect(delta);
        checkGrounded();

        if (isShooting) {
            handleShootingAnimation();
        }
        if (isShootingUp) {
            handleShootingUpAnimation();
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

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectShape;
        body.createFixture(fixtureDef).setUserData(this);

        rectShape.dispose();
    }

    public void draw(Batch batch) {
        setBatchColorForDamage(batch);
        drawAnimation(batch, getCurrentUpperBodyFrame(), getCurrentLowerBodyFrame());
        batch.setColor(1, 1, 1, 1);

        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

    public void jump() {
        if (jumpCount < MAX_JUMPS) {
            //  body.applyLinearImpulse(new Vector2(0, 1000), body.getWorldCenter(), true);
            body.setLinearVelocity(body.getLinearVelocity().x, 500);
            jumpCount++;
            stateTime = 0;
            currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
        }
    }

    public void move(float moveInput) {
        float accelerationFactor = 0.1f;
        boolean isGrounded = Math.abs(body.getLinearVelocity().y) < 0.01f;

        // Determine the target speed based on input strength
        float targetSpeed = calculateTargetSpeed(moveInput, isGrounded);
        smoothSpeedTransition(targetSpeed, accelerationFactor);

        // Update facing direction and animation state
        if (moveInput != 0) {
            isFacingRight = moveInput > 0;
            adjustFrameOrientation();
        }

        updateAnimationStateBasedOnMovement();
    }

    public void shootUp() {
        boolean isGrounded = Math.abs(body.getLinearVelocity().y) < 0.01f;

        if (isGrounded &&!isShootingUp && !isShooting) {
            isShootingUp = true;
            stateTime = 0f;

            // Spawn above the character and set velocity straight up
            Vector2 position = body.getPosition().add(0, 40); // Adjust height if necessary
            Vector2 velocity = new Vector2(0, 1000);           // Set to move vertically up

            projectiles.add(new Projectile(world, position, velocity, projectileTexture));
        }
    }

    public void shoot() {
        if (!isShooting && !isShootingUp) {
            isShooting = true;
            stateTime = 0f;

            // Determine position offset and velocity based on facing direction
            Vector2 position = body.getPosition().add(isFacingRight ? 20 : -20, 10);
            Vector2 velocity = new Vector2(isFacingRight ? 1000 : -1000, 0);

            // Add a new projectile with specified rotation
            projectiles.add(new Projectile(world, position, velocity, projectileTexture));
        }
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }

    public void startRedFlashEffect(float duration) {
        this.damaged = true;
        this.damageTimer = duration;
    }

    public void onEnemyCollision() {
        setDamaged(true);
        startRedFlashEffect(2f);
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

    private void updateDamageEffect(float delta) {
        if (damaged) {
            damageTimer -= delta;
            if (damageTimer <= 0) {
                damaged = false;
                damageTimer = 0;
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
        if (damaged) {
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
        if (isShootingUp) {
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

        }
    }

    private void drawAnimation(Batch batch, TextureRegion upperBodyFrame, TextureRegion lowerBodyFrame) {
        float posX = isFacingRight ? body.getPosition().x - 26 : body.getPosition().x - 36;
        float posY = body.getPosition().y - 24;

        batch.draw(upperBodyFrame, posX, posY, 64, 64);
        batch.draw(lowerBodyFrame, posX, posY, 64, 64);
    }

}

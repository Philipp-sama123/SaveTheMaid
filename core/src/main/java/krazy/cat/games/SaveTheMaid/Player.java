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
    private final AnimationSetFemaleAgent animationSetAgent;
    public World world;
    public Body body;

    private float stateTime;
    private AnimationType currentAnimationState = AnimationType.IDLE_CHARISMATIC;

    private boolean isCrouching = false;
    private boolean isShooting = false;
    private boolean isFacingRightLowerBody;
    private boolean isFacingRightUpperBody;

    private Array<Projectile> projectiles;
    private Texture projectileTexture;

    public Player(World world) {
        super();
        this.world = world;
        animationSetAgent = new AnimationSetFemaleAgent(
            new Texture("Characters/FemaleAgent/Body/Black.png"),
            new Texture("Characters/FemaleAgent/Feet/Red.png")
        );

        definePlayer();
    }

    public void update(float dt) {
        stateTime += dt;
        // Remove if destroyed
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(dt);

            // Remove if destroyed
            if (projectile.isDestroyed()) {
                projectiles.removeIndex(i);
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
        // Create a rectangular shape for the player
        PolygonShape rectShape = new PolygonShape();
        float halfWidth = 8f;  // Narrower width
        float halfHeight = 24f;  // Shorter height
        rectShape.setAsBox(halfWidth, halfHeight);  // Create the rectangle

        // Fixture definition with adjusted properties
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectShape;
        // Attach the fixture to the body
        body.createFixture(fixtureDef);

        // Clean up the shape to free resources
        rectShape.dispose();
    }

    public void draw(Batch batch) {
        // Draw the upper body animation frame
        if (isShooting) {
            // Check if the shooting animation has finished
            Animation<TextureRegion> shootAnimation = animationSetAgent.getUpperBodyAnimation(currentAnimationState);
            if (shootAnimation.isAnimationFinished(stateTime)) {
                float animationDuration = shootAnimation.getAnimationDuration();
                Gdx.app.log("RESET SHOOT", "RESET SHOOTING!!!!");
                Gdx.app.log("Animation Duration", "Duration: " + animationDuration);
                Gdx.app.log("stateTime", "stateTime: " + stateTime);

                isShooting = false;
                stateTime = 0f;
            }
        }// Setting walking/running animation based on speed
        if (body.getLinearVelocity().y > 0) {
            currentAnimationState = isShooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
        } else if (body.getLinearVelocity().y < 0) {
            currentAnimationState = isShooting ? AnimationType.FALL_SHOOT : AnimationType.FALL;
        } else if (body.getLinearVelocity().x != 0) {
            currentAnimationState = isShooting ? (Math.abs(body.getLinearVelocity().x) > 250 ? AnimationType.RUN_SHOOT : AnimationType.WALK_SHOOT) : (Math.abs(body.getLinearVelocity().x) > 250 ? AnimationType.RUN : AnimationType.WALK);
        } else {
            // No Velocity in any direction
            if (isCrouching) {
                currentAnimationState = isShooting ? AnimationType.CROUCH_SHOOT : AnimationType.CROUCH_IDLE;
            } else {
                currentAnimationState = isShooting ? AnimationType.STAND_SHOOT : AnimationType.IDLE;
            }
        }
        if (body.getLinearVelocity().x > 0) {
            isFacingRightLowerBody = true;
            isFacingRightUpperBody = true;
        } else if (body.getLinearVelocity().x < 0) {
            isFacingRightLowerBody = false;
            isFacingRightUpperBody = false;
        } else {

        }

        adjustLowerBodyFrameOrientation();
        adjustUpperBodyFrameOrientation();

        batch.draw(
            getCurrentUpperBodyFrame(), // The current upper body frame
            isFacingRightUpperBody ? body.getPosition().x - 26 : body.getPosition().x - 36, // X position
            body.getPosition().y - 24, // Y position
            64, // Width of the sprite
            64 // Height of the sprite
        );

        // Optionally, if you also want to draw the lower body frame:
        batch.draw(
            getCurrentLowerBodyFrame(), // The current lower body frame
            isFacingRightLowerBody ? body.getPosition().x - 26 : body.getPosition().x - 36, // X position
            body.getPosition().y - 24, // Y position
            64, // Width of the sprite
            64 // Height of the sprite
        );
        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }
    }

    // Return projectiles array so GameScreen can access if needed
    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

    public void jump() {
        // Only apply vertical impulse for jump, keeping horizontal velocity intact
      //  if (Math.abs(body.getLinearVelocity().y) < 0.01f) { // Ensure grounded before jumping
            body.applyLinearImpulse(new Vector2(0, 750), body.getWorldCenter(), true);
            stateTime = 0;
     //   }
    }


    public void move(float moveInput) {
        // Maximum speed threshold for running
        float maxSpeed = 250f;
        float acceleration = 10f * moveInput;
        float friction = 5f;

        // Apply acceleration up to the max speed
        if (Math.abs(body.getLinearVelocity().x) < maxSpeed) {
            body.applyLinearImpulse(new Vector2(acceleration, 0), body.getWorldCenter(), true);
        }

        // Apply friction when the input is near zero
        if (Math.abs(moveInput) < 0.05f) {
            float newVelocityX = body.getLinearVelocity().x * (1 - friction * Gdx.graphics.getDeltaTime());
            body.setLinearVelocity(new Vector2(newVelocityX, body.getLinearVelocity().y));
        }
    }


    public TextureRegion getCurrentUpperBodyFrame() {
        return animationSetAgent.getLowerBodyFrame(currentAnimationState, stateTime, true);
    }

    public TextureRegion getCurrentLowerBodyFrame() {
        return animationSetAgent.getUpperBodyFrame(currentAnimationState, stateTime, true);
    }

    private void adjustUpperBodyFrameOrientation() {
        if (isFacingRightUpperBody && !animationSetAgent.isUpperBodyFramesFlipped()) {
            animationSetAgent.flipUpperBodyFramesHorizontally();

        } else if (!isFacingRightUpperBody && animationSetAgent.isUpperBodyFramesFlipped()) {
            animationSetAgent.flipUpperBodyFramesHorizontally();
        }
    }

    private void adjustLowerBodyFrameOrientation() {
        if (isFacingRightLowerBody && !animationSetAgent.isLowerBodyFramesFlipped()) {
            animationSetAgent.flipLowerBodyFramesHorizontally();

        } else if (!isFacingRightLowerBody && animationSetAgent.isLowerBodyFramesFlipped()) {
            animationSetAgent.flipLowerBodyFramesHorizontally();
        }
    }

    public void shoot() {
        if (!isShooting) {
            isShooting = true;
            stateTime = 0f;
            // Spawn Projectile

            // Spawn projectile with animation
            Vector2 position = body.getPosition().add(isFacingRightUpperBody ? 20 : -20, 10);
            Vector2 velocity = new Vector2(isFacingRightUpperBody ? 1000 : -1000, 0);
            Projectile projectile = new Projectile(world, position, velocity, projectileTexture);
            projectiles.add(projectile);
        }
    }
}

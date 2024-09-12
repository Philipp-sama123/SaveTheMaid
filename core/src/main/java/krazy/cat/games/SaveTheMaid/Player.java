package krazy.cat.games.SaveTheMaid;


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

import krazy.cat.games.SaveTheMaid.AnimationSetFemaleAgent.AnimationType;

public class Player {
    private final AnimationSetFemaleAgent animationSetAgent;
    public World world;
    public Body body;

    private float stateTime;
    private AnimationType currentAnimationState = AnimationType.IDLE_CHARISMATIC;

    private boolean isCrouching;
    private boolean isShooting;
    private boolean isFacingRightLowerBody;
    private boolean isFacingRightUpperBody;

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
    }

    private void definePlayer() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(100, 100);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

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
                isShooting = false;
                stateTime = 0f;
            }
        }
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
        } else {
            isFacingRightLowerBody = false;
            isFacingRightUpperBody = false;
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
    }

    /*
            public void updateAnimationState(SpriteBatch batch) {
                if (isShooting) {
                    // Check if the shooting animation has finished
                    Animation<TextureRegion> shootAnimation = animationSetAgent.getUpperBodyAnimation(currentAnimationState);
                    if (shootAnimation.isAnimationFinished(stateTime)) {
                        isShooting = false;
                        stateTime = 0f;
                    }
                }
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
                batch.draw(getCurrentUpperBodyFrame(),body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2, 64, 64);
                batch.draw(getCurrentLowerBodyFrame(), body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2, 64, 64);
            }*/
    public void jump() {
        body.applyLinearImpulse(new Vector2(0, 250f), body.getWorldCenter(), true);
    }

    public void moveLeft() {
        if (body.getLinearVelocity().x > 0)
            body.setLinearVelocity(new Vector2());
        if (body.getLinearVelocity().x > -500.f)
            body.applyLinearImpulse(new Vector2(-5f, 0), body.getWorldCenter(), true);   // Move left
    }

    public void moveRight() {
        if (body.getLinearVelocity().x < 0)
            body.setLinearVelocity(new Vector2());
        if (body.getLinearVelocity().x < 500.f)
            body.applyLinearImpulse(new Vector2(5f, 0), body.getWorldCenter(), true);    // Move right

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
}

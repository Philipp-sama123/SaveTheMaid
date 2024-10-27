package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Enemy {
    private World world;
    public Body body;
    private AnimationSetZombie animationSet;
    private AnimationSetZombie.ZombieAnimationType currentState;
    private AnimationSetZombie.ZombieAnimationType previousState;
    private float stateTime;
    private boolean isFacingRight = true;
    private int health = 100;
    private boolean isDestroyed = false;
    private boolean isHit = false;
    private boolean readyToDispose = false;

    public Enemy(World world, Vector2 position) {
        this.world = world;
        this.stateTime = 0f;
        this.currentState = AnimationSetZombie.ZombieAnimationType.IDLE;

        Texture spriteSheet = new Texture("Characters/Zombie/Colors/Grey.png");
        this.animationSet = new AnimationSetZombie(spriteSheet);

        defineEnemy(position);
    }

    private void defineEnemy(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f, 20f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    public void update(float dt) {
        if (isDestroyed && !deathAnimationCompleted()) {
            // Continue death animation
            stateTime += dt;
            return;
        } else if (isDestroyed && deathAnimationCompleted()) {
            // Mark for removal from world and dispose of resources
            readyToDispose = true;
            dispose();
            return;
        }

        stateTime += dt;

        // If in HIT state, check if animation has completed before returning to previous state
        if (isHit && animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.HIT).isAnimationFinished(stateTime)) {
            currentState = previousState;
            isHit = false;
            stateTime = 0f;
        } else if (!isHit) { // Only update state if not hit
            if (body.getLinearVelocity().x != 0) {
                currentState = AnimationSetZombie.ZombieAnimationType.WALK;
            } else {
                currentState = AnimationSetZombie.ZombieAnimationType.IDLE;
            }
        }

        adjustFacingDirection();
    }

    private boolean deathAnimationCompleted() {
        return animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.DEATH).isAnimationFinished(stateTime);
    }

    private void adjustFacingDirection() {
        if (body.getLinearVelocity().x > 0 && !isFacingRight) {
            isFacingRight = true;
            animationSet.flipFramesHorizontally();
        } else if (body.getLinearVelocity().x < 0 && isFacingRight) {
            isFacingRight = false;
            animationSet.flipFramesHorizontally();
        }
    }

    public void draw(Batch batch) {
        if (isDestroyed && deathAnimationCompleted()) return;

        boolean looping = currentState != AnimationSetZombie.ZombieAnimationType.DEATH;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);
        batch.draw(
            currentFrame,
            isFacingRight ? body.getPosition().x - 32 : body.getPosition().x + 32,
            body.getPosition().y - 20,
            isFacingRight ? 64 : -64,
            64
        );
    }

    public void takeDamage(int damage) {
        if (isDestroyed || isHit) return; // Avoid further hits if already destroyed or in HIT state
        Gdx.app.log("DAMAGE", "Damage enemy " + damage);
        health -= damage;
        if (health <= 0) {
            health = 0;
            currentState = AnimationSetZombie.ZombieAnimationType.DEATH;
            isDestroyed = true;
            stateTime = 0f; // Reset state time to start DEATH animation
        } else {
            previousState = currentState;
            currentState = AnimationSetZombie.ZombieAnimationType.HIT;
            isHit = true;
            stateTime = 0f;
        }
    }

    public void onHit() {
        takeDamage(20);  // Adjust damage value based on your game mechanics
    }

    public boolean isReadyToDispose() {
        return readyToDispose;
    }

    public void dispose() {
        animationSet.dispose();
        if (world != null && body != null) {
            world.destroyBody(body);
            body = null;
        }
    }
}

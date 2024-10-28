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
    private static final float ATTACK_RANGE = 50f; // Adjust for attack range
    private static final float MOVEMENT_SPEED = 15f; // Adjust for desired speed

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

    public void update(float dt, Vector2 playerPosition) {
        if (isDestroyed && !deathAnimationCompleted()) {
            stateTime += dt;
            return;
        } else if (isDestroyed && deathAnimationCompleted()) {
            readyToDispose = true;
            dispose();
            return;
        }

        stateTime += dt;

        if (isHit && animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.HIT).isAnimationFinished(stateTime)) {
            currentState = previousState;
            isHit = false;
            stateTime = 0f;
        } else if (!isHit) {
            if (isPlayerInRange(playerPosition)) {
                currentState = AnimationSetZombie.ZombieAnimationType.ATTACK;
                body.setLinearVelocity(0, body.getLinearVelocity().y); // Stop moving while attacking
            } else {
                moveToPlayer(playerPosition);
            }
        }

        adjustFacingDirection();
    }

    private void moveToPlayer(Vector2 playerPosition) {
        // Calculate direction vector to the player
        Vector2 direction = playerPosition.cpy().sub(body.getPosition()).nor();
        direction.scl(MOVEMENT_SPEED); // Scale by movement speed

        // Set enemy velocity in the direction of the player
        body.setLinearVelocity(direction.x, body.getLinearVelocity().y);

        // Update state to WALK if the enemy is moving
        if (direction.len() > 0) {
            currentState = AnimationSetZombie.ZombieAnimationType.WALK;
        } else {
            currentState = AnimationSetZombie.ZombieAnimationType.IDLE;
        }
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

    private boolean isPlayerInRange(Vector2 playerPosition) {
        return body.getPosition().dst(playerPosition) < ATTACK_RANGE;
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
        if (isDestroyed || isHit) return;
        Gdx.app.log("DAMAGE", "Damage enemy " + damage);
        health -= damage;
        if (health <= 0) {
            health = 0;
            currentState = AnimationSetZombie.ZombieAnimationType.DEATH;
            isDestroyed = true;
            stateTime = 0f;
        } else {
            previousState = currentState;
            currentState = AnimationSetZombie.ZombieAnimationType.HIT;
            isHit = true;
            stateTime = 0f;
        }
    }

    public void onHit() {
        takeDamage(20);
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

    public void onPlayerCollision() {
        Gdx.app.log("onEnemyCollision", "PLAYER: " + this);
    }
}

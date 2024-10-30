package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Enemy {
    private static final float ATTACK_RANGE = 25f;
    private static final float MOVEMENT_SPEED = 15f;

    private World world;
    public Body body;
    private AnimationSetZombie animationSet;
    private AnimationSetZombie.ZombieAnimationType currentState;
    private AnimationSetZombie.ZombieAnimationType previousState;
    private float stateTime;
    private boolean isFacingLeft = true;
    private int health = 100;
    private boolean isDestroyed = false;
    private boolean isHit = false;
    private boolean readyToDispose = false;
    private Fixture attackCollider;
    private boolean attackColliderActive = false;

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
        } else if (isHit) {
            // Stop moving while hit (optional)
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        } else {
            if (isPlayerInRange(playerPosition)) {
                currentState = AnimationSetZombie.ZombieAnimationType.ATTACK;
                body.setLinearVelocity(0, body.getLinearVelocity().y);
            } else {
                moveToPlayer(playerPosition);
            }
        }
        // Adjust facing direction based on velocity
        adjustFacingDirection();
    }
    // No `flipped` flag needed anymore.
    private void adjustFacingDirection() {
        float velocityX = body.getLinearVelocity().x;

        if (velocityX < 0 && !isFacingLeft) {
            // Moving left and not facing left, flip frames to face left
            animationSet.flipFramesHorizontally();
            isFacingLeft = true;
        } else if (velocityX > 0 && isFacingLeft) {
            // Moving right and facing left, flip frames to face right
            animationSet.flipFramesHorizontally();
            isFacingLeft = false;
        }
    }


    private void moveToPlayer(Vector2 playerPosition) {
        Vector2 direction = playerPosition.cpy().sub(body.getPosition()).nor();
        direction.scl(MOVEMENT_SPEED);

        body.setLinearVelocity(direction.x, body.getLinearVelocity().y);

        if (direction.len() > 0) {
            currentState = AnimationSetZombie.ZombieAnimationType.WALK;
        } else {
            currentState = AnimationSetZombie.ZombieAnimationType.IDLE;
        }
    }

    private boolean deathAnimationCompleted() {
        return animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.DEATH).isAnimationFinished(stateTime);
    }


    private boolean isPlayerInRange(Vector2 playerPosition) {
        return body.getPosition().dst(playerPosition) < ATTACK_RANGE;
    }

    public void draw(Batch batch) {
        if (isDestroyed && deathAnimationCompleted()) return;

        boolean looping = currentState != AnimationSetZombie.ZombieAnimationType.DEATH;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        // Draw frame based on isFacingLeft; remove position adjustment, rely on `flipFramesHorizontally`
        batch.draw(
            currentFrame,
            body.getPosition().x - 32, // Adjust for position only
            body.getPosition().y - 20,
            64,                         // Width of frame
            64                          // Height of frame
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

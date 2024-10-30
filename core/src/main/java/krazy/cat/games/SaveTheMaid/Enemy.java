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

    private void createAttackCollider() {
        PolygonShape attackShape = new PolygonShape();
        attackShape.setAsBox(10f, 20f, new Vector2(isFacingLeft ? 20f : -20f, 0), 0); // Position in front of enemy

        FixtureDef attackFixtureDef = new FixtureDef();
        attackFixtureDef.shape = attackShape;
        attackFixtureDef.isSensor = true; // Set as sensor to detect collision without physical response

        attackCollider = body.createFixture(attackFixtureDef);
        attackCollider.setUserData("attack"); // Tag it for identification in collision handling
        attackShape.dispose();
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
            body.setLinearVelocity(0, body.getLinearVelocity().y); // Stop moving while hit
        } else {
            if (isPlayerInRange(playerPosition)) {
                currentState = AnimationSetZombie.ZombieAnimationType.ATTACK;
                body.setLinearVelocity(0, body.getLinearVelocity().y); // Stop moving while attacking

                if (!attackColliderActive) {
                    createAttackCollider();
                    attackColliderActive = true;
                }
            } else {
                moveToPlayer(playerPosition);
                if (attackColliderActive) {
                    body.destroyFixture(attackCollider); // Remove the collider when not attacking
                    attackColliderActive = false;
                }
            }
        }

        adjustFacingDirection();
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

    private void adjustFacingDirection() {
        boolean movingLeft = body.getLinearVelocity().x < 0;
        if (movingLeft != isFacingLeft) {
            // Update the facing direction
            isFacingLeft = movingLeft;
            // Flip frames only if the direction has actually changed
            if (animationSet.isFlipped() != movingLeft) {
                animationSet.flipFramesHorizontally();
            }
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
            isFacingLeft ? body.getPosition().x - 32 : body.getPosition().x + 32,
            body.getPosition().y - 20,
            isFacingLeft ? 64 : -64,
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

package krazy.cat.games.SaveTheMaid;

import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_GROUND_ONLY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Enemy {
    private static final float ATTACK_RANGE = 25f;
    private static final float MOVEMENT_SPEED = 15f;
    private static final float ATTACK_COOLDOWN = 1.5f; // Time to reset attack collider
    private static final float ATTACK_COLLIDER_UPDATE_DELAY = .4f; // Delay in seconds for updating the collider position

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
    private Fixture attackCollider;
    private boolean attackColliderActive = false;
    private float attackAnimationDuration;

    private float attackCooldownTimer = 0f;
    private float attackColliderUpdateTimer = 0f; // Timer to keep track of elapsed time

    public Enemy(World world, Vector2 position) {
        this.world = world;
        this.stateTime = 0f;
        this.currentState = AnimationSetZombie.ZombieAnimationType.IDLE;

        Texture spriteSheet = new Texture("Characters/Zombie/Colors/Grey.png");
        this.animationSet = new AnimationSetZombie(spriteSheet);
        attackAnimationDuration = animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.ATTACK).getAnimationDuration();

        defineEnemy(position);
        deactivateAttackCollider();
    }

    public void update(float dt, Vector2 playerPosition) {
        if (isDestroyed && !deathAnimationCompleted()) {
            stateTime += dt;
            disableCollision();
            return;
        } else if (isDestroyed && deathAnimationCompleted()) {
            dispose();
            return;
        }
        stateTime += dt;

        if (isHit && animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.HIT).isAnimationFinished(stateTime)) {
            currentState = previousState;
            isHit = false;
            stateTime = 0f;
        } else if (isHit) {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        } else {
            if (isPlayerInRange(playerPosition)) {
                attackColliderUpdateTimer += dt;

                if (attackCooldownTimer <= 0) {
                    currentState = AnimationSetZombie.ZombieAnimationType.ATTACK;
                    body.setLinearVelocity(0, body.getLinearVelocity().y);

                    // Enable attack collider while attacking
                    if (!attackColliderActive) {
                        attackColliderUpdateTimer = 0f;
                        activateAttackCollider();
                        stateTime = 0;
                    }

                    // Once attack animation finishes, retreat attack collider and start cooldown
                    if (animationSet.getAnimation(AnimationSetZombie.ZombieAnimationType.ATTACK).isAnimationFinished(stateTime)) {
                        startCooldown();
                        currentState = AnimationSetZombie.ZombieAnimationType.IDLE;
                        deactivateAttackCollider(); // <-- Ensure collider is off after attack ends
                    }
                }
            } else {
                moveToPlayer(playerPosition);

                // Deactivate attack collider immediately when switching to movement
                if (attackColliderActive) {
                    deactivateAttackCollider(); // <-- Ensure collider is off when moving
                }
            }


        }
        // Deactivate attack collider if the enemy is no longer attacking
        if (currentState != AnimationSetZombie.ZombieAnimationType.ATTACK && attackColliderActive) {
            deactivateAttackCollider();
        }
        // Handle attack cooldown
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= dt;
        }
        adjustFacingDirection();

        if (attackColliderUpdateTimer >= ATTACK_COLLIDER_UPDATE_DELAY) {
            updateAttackColliderPosition();
            attackColliderUpdateTimer = 0f; // Reset the timer after updating
        }
    }

    private void disableCollision() {
        body.getFixtureList().forEach(fixture -> {
           Filter filter =  fixture.getFilterData();
            filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
            fixture.setFilterData(filter);
        });
    }

    private void startCooldown() {
        attackCooldownTimer = ATTACK_COOLDOWN;
        deactivateAttackCollider(); // Ensure the collider is off during cooldown
    }

    public void draw(Batch batch) {
        if (isDestroyed && deathAnimationCompleted()) return;

        boolean looping = currentState != AnimationSetZombie.ZombieAnimationType.DEATH;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        batch.draw(
            currentFrame,
            body.getPosition().x - 32,
            body.getPosition().y - 20,
            64,
            64
        );
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

        fixtureDef.filter.categoryBits = CATEGORY_ENEMY;
        fixtureDef.filter.maskBits = MASK_ENEMY;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        // Initialize the attack collider as a sensor initially
        createAttackCollider();
    }

    private void createAttackCollider() {
        PolygonShape attackShape = new PolygonShape();
        float xOffset = isFacingLeft ? -12f : 12f;
        attackShape.setAsBox(6f, 6f, new Vector2(xOffset, 0), 0);

        FixtureDef attackFixtureDef = new FixtureDef();
        attackFixtureDef.shape = attackShape;
        attackFixtureDef.isSensor = true; // Ensure it’s always a sensor

        attackFixtureDef.filter.categoryBits = CATEGORY_PROJECTILE;
        attackFixtureDef.filter.maskBits = MASK_PROJECTILE;

        attackCollider = body.createFixture(attackFixtureDef);
        attackCollider.setUserData(this);
        attackShape.dispose();

        attackColliderActive = false;
    }

    private void updateAttackColliderPosition() {
        if (attackCollider == null) return;
        if (attackColliderActive) {
            // Calculate the offset based on facing direction
            float xOffset = isFacingLeft ? -12f : 12f;
            float yOffset = 0f; // Adjust Y offset if necessary

            // Move the collider to the attack position relative to the enemy's body
            PolygonShape attackShape = (PolygonShape) attackCollider.getShape();
            attackShape.setAsBox(6f, 6f, new Vector2(xOffset, yOffset), 0);
        } else {
            // this is a hack for "disabling" the collider
            PolygonShape attackShape = (PolygonShape) attackCollider.getShape();
            attackShape.setAsBox(0, 0, new Vector2(0, 0), 0);
        }
    }

    private void adjustFacingDirection() {
        float velocityX = body.getLinearVelocity().x;

        if (velocityX < 0 && !isFacingLeft) {
            animationSet.flipFramesHorizontally();
            isFacingLeft = true;
        } else if (velocityX > 0 && isFacingLeft) {
            animationSet.flipFramesHorizontally();
            isFacingLeft = false;
        }
    }

    private void activateAttackCollider() {
        attackColliderActive = true;
    }

    private void deactivateAttackCollider() {
        attackColliderActive = false;
        PolygonShape attackShape = (PolygonShape) attackCollider.getShape();
        attackShape.setAsBox(0, 0, new Vector2(0, 0), 0);
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

    public void dispose() {
        if (world != null && body != null) {
            if (attackCollider != null) body.destroyFixture(attackCollider);
            world.destroyBody(body);
            body = null;
        }
    }

    public void onPlayerCollision() {
        Gdx.app.log("onEnemyCollision", "PLAYER: " + this);
    }
}

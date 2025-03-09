package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class ProjectileBase {
    protected World world;
    protected Body body;
    protected float stateTime;
    protected boolean isDestroyed = false;
    protected boolean setToDestroy = false;
    protected boolean isExploding = false;

    protected Animation<TextureRegion> projectileAnimation;
    protected Animation<TextureRegion> explosionAnimation;

    protected boolean isFacingRight;
    protected boolean isShootingUp;
    protected boolean isFlipped = false;

    // New field to control explosion functionality
    protected boolean explosionEnabled;

    // Sprite dimensions (in pixels)
    protected float projectileSpriteWidth;
    protected float projectileSpriteHeight;
    protected float explosionSpriteWidth;
    protected float explosionSpriteHeight;
    protected int explosionFrameCount;

    // Collider dimensions (in pixels, or units that will be divided by PPM)
    protected float projectileColliderWidth;
    protected float projectileColliderHeight;

    // Offset used when defining the projectile’s fixture
    protected final float X_OFFSET_BULLET = 2 / PPM;

    /**
     * Constructs a new projectile.
     *
     * @param world                     the Box2D world
     * @param position                  the spawn position
     * @param velocity                  the initial velocity
     * @param projectileTexture         the texture for the projectile animation
     * @param projectileAnimRow         the row index to use when splitting the projectile texture
     * @param projectileSpriteWidth     the width (in pixels) of a projectile frame
     * @param projectileSpriteHeight    the height (in pixels) of a projectile frame
     * @param projectileColliderWidth   the half-width (in pixels) for the projectile collider shape
     * @param projectileColliderHeight  the half-height (in pixels) for the projectile collider shape
     * @param explosionEnabled          flag to enable explosion animation and collision
     * @param explosionTexture          the texture for the explosion animation
     * @param explosionAnimRow          the row index to use when splitting the explosion texture
     * @param explosionFrameCount       the number of frames for the explosion animation
     * @param explosionSpriteWidth      the width (in pixels) of an explosion frame
     * @param explosionSpriteHeight     the height (in pixels) of an explosion frame
     */
    public ProjectileBase(World world, Vector2 position, Vector2 velocity,
                          Texture projectileTexture, int projectileAnimRow,
                          float projectileSpriteWidth, float projectileSpriteHeight,
                          float projectileColliderWidth, float projectileColliderHeight,
                          boolean explosionEnabled,
                          Texture explosionTexture, int explosionAnimRow, int explosionFrameCount,
                          float explosionSpriteWidth, float explosionSpriteHeight) {
        this.world = world;
        stateTime = 0;
        this.projectileSpriteWidth = projectileSpriteWidth;
        this.projectileSpriteHeight = projectileSpriteHeight;
        this.projectileColliderWidth = projectileColliderWidth;
        this.projectileColliderHeight = projectileColliderHeight;
        this.explosionEnabled = explosionEnabled;
        this.explosionSpriteWidth = explosionSpriteWidth;
        this.explosionSpriteHeight = explosionSpriteHeight;
        this.explosionFrameCount = explosionFrameCount;

        // Build projectile animation
        TextureRegion[][] projFrames = TextureRegion.split(projectileTexture,
            (int) projectileSpriteWidth, (int) projectileSpriteHeight);
        projectileAnimation = new Animation<>(0.025f, projFrames[projectileAnimRow]);
        projectileAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Build explosion animation only if enabled
        if (explosionEnabled) {
            TextureRegion[][] expFrames = TextureRegion.split(explosionTexture,
                (int) explosionSpriteWidth, (int) explosionSpriteHeight);
            TextureRegion[] explosionFrames = new TextureRegion[explosionFrameCount];
            for (int i = 0; i < explosionFrameCount; i++) {
                explosionFrames[i] = expFrames[explosionAnimRow][i];
            }
            explosionAnimation = new Animation<>(0.1f, explosionFrames);
            explosionAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        }

        // Determine shooting direction
        isFacingRight = velocity.x > 0;
        isShootingUp = velocity.y > 0;

        // If facing right, flip the projectile frames horizontally
        if (isFacingRight) {
            for (TextureRegion frame : projFrames[projectileAnimRow]) {
                frame.flip(true, false);
            }
        }
        defineProjectile(position, velocity);
    }

    /**
     * Defines the projectile body and its initial fixture.
     */
    protected void defineProjectile(Vector2 position, Vector2 velocity) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        // Note: setAsBox expects half-width and half-height.
        shape.setAsBox((projectileColliderWidth) / PPM, (projectileColliderHeight) / PPM,
            new Vector2(X_OFFSET_BULLET, 0), 0);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.isSensor = true;
        fdef.filter.categoryBits = CATEGORY_PROJECTILE;
        fdef.filter.maskBits = MASK_PROJECTILE;
        body.createFixture(fdef).setUserData(this);
        body.setLinearVelocity(velocity);

        shape.dispose();
    }

    /**
     * Call this method when a collision occurs.
     */
    public void onCollision() {
        setToDestroy = true;
    }

    /**
     * Triggers the explosion: resets the animation timer, stops movement,
     * removes the original collider and creates a larger explosion collider.
     */
    protected void triggerExplosion() {
        isExploding = true;
        stateTime = 0;
        // Stop movement
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setGravityScale(0);
        // Remove existing fixtures
        for (Fixture f : body.getFixtureList()) {
            body.destroyFixture(f);
        }

        // Create a new collider based on the explosion sprite size.
        PolygonShape explosionShape = new PolygonShape();
        float explosionColliderWidth = explosionSpriteWidth / PPM;
        float explosionColliderHeight = explosionSpriteHeight / PPM;
        explosionShape.setAsBox(explosionColliderWidth / 2, explosionColliderHeight / 2);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = explosionShape;
        fdef.isSensor = true;
        fdef.filter.categoryBits = CATEGORY_PROJECTILE;
        fdef.filter.maskBits = MASK_PROJECTILE;
        body.createFixture(fdef).setUserData(this);
        explosionShape.dispose();
    }

    /**
     * Updates the projectile state.
     */
    public void update(float dt) {
        stateTime += dt;

        if (!isDestroyed && !isExploding) {
            // Example: flip vertically when shooting upward and the projectile starts falling
            if (!isFlipped && isShootingUp && body.getLinearVelocity().y < 0) {
                for (TextureRegion frame : projectileAnimation.getKeyFrames()) {
                    frame.flip(false, true);
                }
                isFlipped = true;
            }

            // Rotate the projectile if shooting upward
            if (isShootingUp) {
                body.setTransform(body.getPosition(), (float) Math.toRadians(-90));
            } else {
                body.setTransform(body.getPosition(), 0);
            }
        }

        if (setToDestroy && !isExploding) {
            if (explosionEnabled) {
                triggerExplosion();
            } else {
                destroy();
            }
        }

        // Once exploding, wait for the explosion animation to finish before destroying the body.
        if (isExploding && explosionAnimation != null && explosionAnimation.isAnimationFinished(stateTime)) {
            destroy();
        }
    }

    /**
     * Draws either the projectile or its explosion animation.
     */
    public void draw(Batch batch) {
        if (!isDestroyed) {
            TextureRegion frame;
            if (isExploding && explosionEnabled && explosionAnimation != null) {
                frame = explosionAnimation.getKeyFrame(stateTime);
                float width = explosionSpriteWidth / PPM;
                float height = explosionSpriteHeight / PPM;
                batch.draw(frame, body.getPosition().x - width / 2, body.getPosition().y - height / 2, width, height);
            } else {
                frame = projectileAnimation.getKeyFrame(stateTime);
                float width = projectileSpriteWidth / PPM;
                float height = projectileSpriteHeight / PPM;
                if (isShootingUp) {
                    batch.draw(frame, body.getPosition().x - width / 2, body.getPosition().y - height / 2,
                        width / 2, height / 2, width, height, 1, 1, -90);
                } else {
                    batch.draw(frame, body.getPosition().x - width / 2, body.getPosition().y - height / 2, width, height);
                }
            }
        }
    }

    /**
     * Destroys the projectile body.
     */
    public void destroy() {
        isDestroyed = true;
        world.destroyBody(body);
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}

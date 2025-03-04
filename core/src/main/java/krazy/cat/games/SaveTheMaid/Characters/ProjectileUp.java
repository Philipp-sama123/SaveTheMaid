package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class ProjectileUp {
    private final World world;
    private Body body;
    private Fixture projectileFixture;

    // Two separate animations: one for the projectile and one for the explosion.
    private final Animation<TextureRegion> projectileAnimation;
    private final Animation<TextureRegion> explosionAnimation;
    private float stateTime;
    private boolean isDestroyed = false;
    private boolean setToDestroy = false;

    private final boolean isFacingRight;
    private final boolean isShootingUp;
    private boolean isFlipped = false;

    private static final float X_OFFSET_BULLET = 2 / PPM;
    // Projectile sprite dimensions
    private static final int PROJECTILE_SPRITE_WIDTH = 24;
    private static final int PROJECTILE_SPRITE_HEIGHT = 24;

    // Explosion sprite dimensions and frame count
    private static final int EXPLOSION_SPRITE_WIDTH = 32;
    private static final int EXPLOSION_SPRITE_HEIGHT = 32;
    private static final int EXPLOSION_FRAME_COUNT = 6;

    // Explosion state
    private boolean isExploding = false;

    public ProjectileUp(World world, Vector2 position, Vector2 velocity, Texture projectileTexture, int projectileRow, Texture explosionTexture) {
        this.world = world;
        stateTime = 0;

        // Create projectile animation from projectileTexture (using the specified row)
        TextureRegion[][] projectileFrames = TextureRegion.split(projectileTexture, PROJECTILE_SPRITE_WIDTH, PROJECTILE_SPRITE_HEIGHT);
        TextureRegion[] projFrames = projectileFrames[projectileRow];
        projectileAnimation = new Animation<>(0.1f, projFrames);
        projectileAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Create explosion animation from explosionTexture (first row, 6 frames)
        TextureRegion[][] explosionFrames = TextureRegion.split(explosionTexture, EXPLOSION_SPRITE_WIDTH, EXPLOSION_SPRITE_HEIGHT);
        TextureRegion[] expFrames = new TextureRegion[EXPLOSION_FRAME_COUNT];
        for (int i = 0; i < EXPLOSION_FRAME_COUNT; i++) {
            expFrames[i] = explosionFrames[29][i];
        }
        explosionAnimation = new Animation<>(0.1f, expFrames);
        explosionAnimation.setPlayMode(Animation.PlayMode.NORMAL); // Play once

        isFacingRight = velocity.x > 0;
        isShootingUp = velocity.y > 0;

        // Flip projectile frames horizontally if needed.
        if (isFacingRight) {
            for (TextureRegion frame : projFrames) {
                frame.flip(true, false);
            }
        }
        defineProjectile(position, velocity);
    }

    public void destroy() {
        isDestroyed = true;
        world.destroyBody(body);
    }

    private void defineProjectile(Vector2 position, Vector2 velocity) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        // Original (smaller) collider for the projectile.
        shape.setAsBox(8 / PPM, 4 / PPM, new Vector2(X_OFFSET_BULLET, 0), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = CATEGORY_PROJECTILE;
        fixtureDef.filter.maskBits = MASK_PROJECTILE;

        projectileFixture = body.createFixture(fixtureDef);
        projectileFixture.setUserData(this);
        body.setLinearVelocity(velocity);

        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;

        if (!isDestroyed && !isExploding) {
            // Regular projectile behavior.
            if (!isFlipped && isShootingUp && body.getLinearVelocity().y < 0) {
                // Flip vertically when falling.
                for (TextureRegion frame : projectileAnimation.getKeyFrames()) {
                    frame.flip(false, true);
                }
                isFlipped = true;
            }
            if (isShootingUp) {
                body.setTransform(body.getPosition(), (float) Math.toRadians(-90));
            } else {
                body.setTransform(body.getPosition(), 0);
            }
        }

        // If collision has occurred and explosion hasn't started, trigger explosion.
        if (setToDestroy && !isExploding) {
            triggerExplosion();
        }

        // When exploding, check if the explosion animation has finished.
        if (isExploding) {
            if (explosionAnimation.isAnimationFinished(stateTime)) {
                destroy();
            }
        }
    }

    /**
     * Triggers the explosion effect by switching animations and replacing the collider.
     */
    private void triggerExplosion() {
        isExploding = true;
        stateTime = 0; // Reset the animation timer for the explosion

        // Stop any movement when exploding.
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);

        // Remove the original projectile collider.
        body.destroyFixture(projectileFixture);

        // Create a new, larger damage collider for the explosion.
        PolygonShape explosionShape = new PolygonShape();
        // Set the collider size based on the explosion sprite dimensions.
        float explosionColliderWidth = EXPLOSION_SPRITE_WIDTH / PPM;
        float explosionColliderHeight = EXPLOSION_SPRITE_HEIGHT / PPM;
        // setAsBox expects half-width and half-height:
        explosionShape.setAsBox(explosionColliderWidth / 2, explosionColliderHeight / 2);

        FixtureDef explosionFixtureDef = new FixtureDef();
        explosionFixtureDef.shape = explosionShape;
        explosionFixtureDef.isSensor = true;
        explosionFixtureDef.filter.categoryBits = CATEGORY_PROJECTILE;
        explosionFixtureDef.filter.maskBits = MASK_PROJECTILE;

        body.createFixture(explosionFixtureDef).setUserData(this);
        explosionShape.dispose();
    }

    public void draw(Batch batch) {
        if (!isDestroyed) {
            TextureRegion frame;
            if (isExploding) {
                frame = explosionAnimation.getKeyFrame(stateTime);
                float explosionWidth = EXPLOSION_SPRITE_WIDTH / PPM;
                float explosionHeight = EXPLOSION_SPRITE_HEIGHT / PPM;
                batch.draw(frame, body.getPosition().x - explosionWidth / 2, body.getPosition().y - explosionHeight / 2, explosionWidth, explosionHeight);
            } else {
                frame = projectileAnimation.getKeyFrame(stateTime);
                float width = PROJECTILE_SPRITE_WIDTH / PPM;
                float height = PROJECTILE_SPRITE_HEIGHT / PPM;
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
     * Call this method when the projectile collides with something.
     */
    public void onCollision() {
        // Instead of immediate destruction, mark for explosion.
        setToDestroy = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}

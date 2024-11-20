package krazy.cat.games.SaveTheMaid;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Projectile {
    private final World world;
    private Body body;
    private final Animation<TextureRegion> animation;
    private float stateTime;
    private boolean isDestroyed = false;
    private boolean setToDestroy = false;

    private boolean isFacingRight;
    private boolean isShootingUp; // New flag to indicate upward shooting
    private boolean isFlipped = false;

    private final float X_OFFSET_BULLET = 2 / PPM;

    public Projectile(World world, Vector2 position, Vector2 velocity, Texture texture) {
        this.world = world;
        stateTime = 0;

        TextureRegion[][] frames = TextureRegion.split(texture, 16, 12);
        animation = new Animation<>(0.1f, frames[0]);
        animation.setPlayMode(Animation.PlayMode.LOOP);

        // Set direction based on velocity
        isFacingRight = velocity.x > 0;
        isShootingUp = velocity.y > 0;  // Determine if shooting upwards

        if (isFacingRight) {
            for (TextureRegion frame : frames[0]) {
                frame.flip(true, false); // Flip horizontally if facing left
            }
        }
        defineProjectile(position, velocity);
    }

    public void destroy() {
        // Flag the projectile for removal
        isDestroyed = true;
        world.destroyBody(body);
    }

    private void defineProjectile(Vector2 position, Vector2 velocity) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(6 / PPM, 3 / PPM, new Vector2(X_OFFSET_BULLET,0 ), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        fixtureDef.filter.categoryBits = CATEGORY_PROJECTILE;
        fixtureDef.filter.maskBits = MASK_PROJECTILE;

        body.createFixture(fixtureDef).setUserData(this);

        body.setLinearVelocity(velocity);

        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;
        if (!isDestroyed) {
            // body.applyForceToCenter(new Vector2(0, 100), true);
            if (!isFlipped && isShootingUp && body.getLinearVelocity().y < 0) {
                for (TextureRegion frame : animation.getKeyFrames()) {
                    frame.flip(true, true);
                }
                isFlipped = true;
            }
        }

        // Rotate the body if shooting upwards
        if (isShootingUp) {
            float rotation = (float) Math.toRadians(-90); // Convert degrees to radians
            body.setTransform(body.getPosition(), rotation);
        } else {
            body.setTransform(body.getPosition(), 0); // No rotation for horizontal shooting
        }

        if (setToDestroy) { //ToDo: maximum range for bullets
            destroy();
        }
    }

    public void draw(Batch batch) {
        if (!isDestroyed) {
            TextureRegion frame = animation.getKeyFrame(stateTime);

            // Draw rotated frame if shooting upwards
            if (isShootingUp) {
                batch.draw(frame, body.getPosition().x - 8 / PPM, body.getPosition().y - 6 / PPM, 8 / PPM, 6 / PPM, 16 / PPM, 12 / PPM, 1, 1, -90);
            } else {
                batch.draw(frame, body.getPosition().x - 8 / PPM, body.getPosition().y - 6 / PPM, 16 / PPM, 12 / PPM);
            }
        }
    }

    public void onCollision() {
        setToDestroy = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}

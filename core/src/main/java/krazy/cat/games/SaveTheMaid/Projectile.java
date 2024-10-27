package krazy.cat.games.SaveTheMaid;

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

    public Projectile(World world, Vector2 position, Vector2 velocity, Texture texture) {
        this.world = world;
        stateTime = 0;

        TextureRegion[][] frames = TextureRegion.split(texture, 16, 12);
        animation = new Animation<>(0.1f, frames[0]);
        animation.setPlayMode(Animation.PlayMode.LOOP);

        // Set direction based on velocity
        isFacingRight = velocity.x > 0;
        if (isFacingRight) {
            for (TextureRegion frame : frames[0]) {
                frame.flip(true, false); // Flip horizontally for left-facing
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
        shape.setAsBox(8, 6);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef).setUserData(this);

        body.setLinearVelocity(velocity);

        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;
        if (!isDestroyed) {
            body.applyForceToCenter(new Vector2(0, 100), true);  // Adjust the force value as needed
        }

        if (setToDestroy) { //ToDo: maximum range for bullets
            destroy();
        }
    }

    public void draw(Batch batch) {
        if (!isDestroyed) {
            batch.draw(animation.getKeyFrame(stateTime), body.getPosition().x - 8, body.getPosition().y - 6, 16, 12);
        }
    }

    public void onCollision() {
        setToDestroy = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}

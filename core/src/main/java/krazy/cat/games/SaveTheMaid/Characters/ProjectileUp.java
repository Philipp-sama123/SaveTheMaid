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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class ProjectileUp {
    private final World world;
    private Body body;
    private final Animation<TextureRegion> animation;
    private float stateTime;
    private boolean isDestroyed = false;
    private boolean setToDestroy = false;

    private final boolean isFacingRight;
    private final boolean isShootingUp;
    private boolean isFlipped = false;

    private static final float X_OFFSET_BULLET = 2 / PPM;
    private static final int SPRITE_WIDTH = 24;
    private static final int SPRITE_HEIGHT = 24;

    public ProjectileUp(World world, Vector2 position, Vector2 velocity, Texture texture, int row) {
        this.world = world;
        stateTime = 0;

        // Extract the correct row of sprites (row parameter determines which projectile)
        TextureRegion[][] frames = TextureRegion.split(texture, SPRITE_WIDTH, SPRITE_HEIGHT);
        TextureRegion[] projectileFrames = frames[row]; // Select a specific row

        animation = new Animation<>(0.1f, projectileFrames);
        animation.setPlayMode(Animation.PlayMode.LOOP);

        isFacingRight = velocity.x > 0;
        isShootingUp = velocity.y > 0;

        if (isFacingRight) {
            for (TextureRegion frame : projectileFrames) {
                frame.flip(true, false); // Flip horizontally if necessary
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
        shape.setAsBox(8 / PPM, 4 / PPM, new Vector2(X_OFFSET_BULLET, 0), 0);

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
            if (!isFlipped && isShootingUp && body.getLinearVelocity().y < 0) {
                for (TextureRegion frame : animation.getKeyFrames()) {
                    frame.flip(false, true); // Flip vertically when falling
                }
                isFlipped = true;
            }
        }

        if (isShootingUp) {
            body.setTransform(body.getPosition(), (float) Math.toRadians(-90));
        } else {
            body.setTransform(body.getPosition(), 0);
        }

        if (setToDestroy) {
            destroy();
        }
    }

    public void draw(Batch batch) {
        if (!isDestroyed) {
            TextureRegion frame = animation.getKeyFrame(stateTime);
            float width = SPRITE_WIDTH / PPM;
            float height = SPRITE_HEIGHT / PPM;

            if (isShootingUp) {
                batch.draw(frame, body.getPosition().x - width / 2, body.getPosition().y - height / 2, width / 2, height / 2, width, height, 1, 1, -90);
            } else {
                batch.draw(frame, body.getPosition().x - width / 2, body.getPosition().y - height / 2, width, height);
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

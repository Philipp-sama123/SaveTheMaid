package krazy.cat.games.SaveTheMaid.Sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_CAT;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;

public class WaterEffect {
    private World world;
    private Body body;
    private Animation<TextureRegion> waterAnimation;
    private float stateTime;
    private boolean destroyed;
    private float riseSpeed = 0.5f / PPM; // Speed of upward movement

    public WaterEffect(World world, Vector2 position, Texture spriteSheet) {
        this.world = world;

        // Create a Box2D body for the water
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Static, as it shouldn't physically move
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define shape and fixture
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(80 / PPM, 32 / PPM); // Half of the sprite dimensions, scaled for Box2D
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // Water should not have physical collision
        fixtureDef.filter.categoryBits = CATEGORY_PROJECTILE;
        fixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE | CATEGORY_CAT;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        // Load frames from the sprite sheet
        TextureRegion[][] regions = TextureRegion.split(spriteSheet, 160, 64); // Assuming each frame is 160x64
        Array<TextureRegion> frames = new Array<>();
        for (TextureRegion[] row : regions) {
            for (TextureRegion frame : row) {
                frames.add(frame);
            }
        }
        if (frames.size == 0) {
            throw new IllegalArgumentException("Sprite sheet does not contain enough frames!");
        }
        waterAnimation = new Animation<>(0.25f, frames, Animation.PlayMode.LOOP); // Adjust frame duration as needed

        stateTime = 0;
        destroyed = false;
    }

    public void update(float delta) {
        stateTime += delta;

        // Gradually move the water effect upwards
        float newY = body.getPosition().y + riseSpeed * delta;

        // Update the body's position manually (Box2D Static Body workaround)
        body.setTransform(body.getPosition().x, newY, body.getAngle());
    }

    public void render(SpriteBatch batch) {
        if (!destroyed) {
            TextureRegion currentFrame = waterAnimation.getKeyFrame(stateTime);
            batch.draw(currentFrame,
                body.getPosition().x - 80 / PPM,
                body.getPosition().y - 32 / PPM,
                160 / PPM, 64 / PPM); // Scale to sprite dimensions
        }
    }

    public void destroy() {
        if (!destroyed) {
            world.destroyBody(body);
            destroyed = true;
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}

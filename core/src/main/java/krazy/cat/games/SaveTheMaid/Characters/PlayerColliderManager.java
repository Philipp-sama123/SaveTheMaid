package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PLAYER;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class PlayerColliderManager {

    private final Player player;
    private final World world;
    private final Body body;

    // Slide collider constant offset (from your original code)
    private static final float SLIDE_COLLIDER_VERTICAL_OFFSET = -32 / PPM;

    public PlayerColliderManager(Player player, World world, Body body) {
        this.player = player;
        this.world = world;
        this.body = body;
        createDefaultCollider();
    }

    /**
     * Creates the default capsule-like collider for the player.
     */
    public void createDefaultCollider() {
        // Clear existing fixtures
        Array<Fixture> fixtures = new Array<>(body.getFixtureList());
        for (Fixture fixture : fixtures) {
            body.destroyFixture(fixture);
        }

        // --- Central rectangle fixture ---
        PolygonShape rectShape = new PolygonShape();
        rectShape.setAsBox(8f / PPM, 16f / PPM);
        FixtureDef rectFixtureDef = new FixtureDef();
        rectFixtureDef.shape = rectShape;
        rectFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        rectFixtureDef.filter.maskBits = MASK_PLAYER;
        body.createFixture(rectFixtureDef).setUserData(player);
        rectShape.dispose();

        // --- Top circle fixture ---
        CircleShape topCircle = new CircleShape();
        topCircle.setRadius(8f / PPM);
        topCircle.setPosition(new Vector2(0, 16f / PPM));
        FixtureDef topCircleFixtureDef = new FixtureDef();
        topCircleFixtureDef.shape = topCircle;
        topCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        topCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        body.createFixture(topCircleFixtureDef).setUserData(player);
        topCircle.dispose();

        // --- Bottom circle fixture ---
        CircleShape bottomCircle = new CircleShape();
        bottomCircle.setRadius(8f / PPM);
        bottomCircle.setPosition(new Vector2(0, -16f / PPM));
        FixtureDef bottomCircleFixtureDef = new FixtureDef();
        bottomCircleFixtureDef.shape = bottomCircle;
        bottomCircleFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        bottomCircleFixtureDef.filter.maskBits = MASK_PLAYER;
        body.createFixture(bottomCircleFixtureDef).setUserData(player);
        bottomCircle.dispose();
    }

    /**
     * Rotates the collider into a horizontal (slide) configuration.
     */
    public void rotateColliderForSlide() {
        // Remove all existing fixtures
        Array<Fixture> fixtures = new Array<>(body.getFixtureList());
        for (Fixture fixture : fixtures) {
            body.destroyFixture(fixture);
        }
        // Create a slide collider: a wider, shorter rectangle
        PolygonShape rotatedShape = new PolygonShape();
        rotatedShape.setAsBox(24f / PPM, 8f / PPM, new Vector2(0, SLIDE_COLLIDER_VERTICAL_OFFSET / 2), 0);
        FixtureDef slideFixtureDef = new FixtureDef();
        slideFixtureDef.shape = rotatedShape;
        slideFixtureDef.filter.categoryBits = CATEGORY_PLAYER;
        slideFixtureDef.filter.maskBits = MASK_PLAYER;
        body.createFixture(slideFixtureDef).setUserData(player);
        rotatedShape.dispose();
    }

    /**
     * Restores the default collider configuration.
     */
    public void restoreCollider() {
        createDefaultCollider();
    }
}

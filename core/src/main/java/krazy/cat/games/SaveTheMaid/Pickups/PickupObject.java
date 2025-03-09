package krazy.cat.games.SaveTheMaid.Pickups;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PICK_UP;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;
// the physic updates are deffered for spawning and destroying
public class PickupObject {

    public enum PickupType {
        LIFE,
        AMMO
    }

    private PickupType type;
    private World world;
    private Body body;
    private Texture texture;
    private boolean collected;

    public PickupObject(World world, Vector2 position, PickupType type) {
        this.world = world;
        this.type = type;
        this.collected = false;
        // Load a texture based on the pickup type.
        switch (type) {
            case LIFE:
                texture = GameAssetManager.getInstance().get(AssetPaths.LIFE_PICKUP_TEXTURE, Texture.class);
                break;
            case AMMO:
                texture = GameAssetManager.getInstance().get(AssetPaths.AMMO_PICKUP_TEXTURE, Texture.class);
                break;
        }

        definePickup(position);
    }

    private void definePickup(Vector2 position) {
        Gdx.app.log("PICKUP", "POSITION!!" + position);
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.StaticBody;
        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(10f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // So it only detects overlaps
        // Set collision filtering so only the player can pick it up.
        fixtureDef.filter.categoryBits = CATEGORY_PICK_UP;
        fixtureDef.filter.maskBits = CATEGORY_PLAYER;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();
    }

    public void update(float dt) {
        // (Optional) You could add some rotation or pulsing animation here.
    }

    public void draw(Batch batch) {
        if (collected) return;
        float width = texture.getWidth() / PPM;
        float height = texture.getHeight() / PPM;
        Vector2 pos = body.getPosition();
        // Draw the texture centered on the pickup's position.
        batch.draw(texture, pos.x - width / 2, pos.y - height / 2, width, height);
    }

    public void collect() {
        collected = true;
    }

    public PickupType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public Body getBody() {
        return body;
    }
}

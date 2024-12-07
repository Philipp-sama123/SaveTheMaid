package krazy.cat.games.SaveTheMaid.Sprites;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import krazy.cat.games.SaveTheMaid.WorldContactListener;

public class Goal {
    private final World world;
    private Body body;
    private final Rectangle bounds;

    public Goal(World world, TiledMap map, Rectangle bounds) {
        this.world = world;
        this.bounds = bounds;
        defineGoal();
    }

    private void defineGoal() {
        // Define the Body and BodyDef
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Goal is stationary
        bodyDef.position.set(
            (bounds.getX() + bounds.getWidth() / 2) / PPM,
            (bounds.getY() + bounds.getHeight() / 2) / PPM
        );

        body = world.createBody(bodyDef);

        // Define the Fixture and make it a sensor
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(bounds.getWidth() / 2 / PPM,
            bounds.getHeight() / 2 / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // Make this fixture a sensor

        body.createFixture(fixtureDef).setUserData(this); // Attach this Goal object to the fixture for identification
        shape.dispose();
    }

    // Custom behavior when the cat collides with the goal
    public void onCatCollision() {
        System.out.println("Goal reached! Cat interaction triggered.");
        // Implement any additional logic such as level completion, animations, etc.
    }

    public void dispose() {
        world.destroyBody(body); // Clean up when no longer needed
    }
}

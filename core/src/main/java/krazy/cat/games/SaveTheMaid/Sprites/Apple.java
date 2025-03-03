package krazy.cat.games.SaveTheMaid.Sprites;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import krazy.cat.games.SaveTheMaid.Characters.Player.Player;

public class Apple {
    private final Body body;
    private final Fixture fixture;

    public Apple(World world, Rectangle bounds) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(
            (bounds.getX() + bounds.getWidth() / 2) / PPM,
            (bounds.getY() + bounds.getHeight() / 2) / PPM
        );

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            (bounds.getWidth() / 2) / PPM,
            (bounds.getHeight() / 2) / PPM
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // Make it a sensor
        fixtureDef.filter.categoryBits = CATEGORY_PROJECTILE;
        fixtureDef.filter.maskBits = MASK_PROJECTILE;
        fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this); // Set user data for collision detection

        shape.dispose();
    }

    public void onPlayerCollision(Player player) {
        player.appleHeal(); // Heal the player by 10 health points
        // ToDo: figure out why it crashes when a 2nd apple gets touched    body.getWorld().destroyBody(body); // Remove the apple after collision
        // maybe not destroying it -- rather making a countdown to disable (!)
    }

}

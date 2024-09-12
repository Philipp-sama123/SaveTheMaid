package krazy.cat.games.SaveTheMaid.Sprites;


import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Sprite {
    public World world;
    public Body body;

    public Player(World world) {
        this.world = world;
        definePlayer();
    }

    private void definePlayer() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(100, 100);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(16);
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
    }

    public void jump() {
        body.applyLinearImpulse(new Vector2(0, 250f), body.getWorldCenter(), true);
    }

    public void moveLeft() {

        if (body.getLinearVelocity().x > -500.f)
            body.applyLinearImpulse(new Vector2(-5f, 0), body.getWorldCenter(), true);   // Move left
    }

    public void moveRight() {
        if (body.getLinearVelocity().x < 500.f)
            body.applyLinearImpulse(new Vector2(5f, 0), body.getWorldCenter(), true);    // Move right

    }
}

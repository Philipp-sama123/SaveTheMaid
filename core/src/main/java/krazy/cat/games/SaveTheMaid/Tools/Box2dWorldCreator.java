package krazy.cat.games.SaveTheMaid.Tools;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_GROUND;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.BatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.RatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.ZombieAICharacter;
import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Sprites.Brick;
import krazy.cat.games.SaveTheMaid.Sprites.Coin;

public class Box2dWorldCreator {
    public Box2dWorldCreator(World world, TiledMap map, GameScreen gameScreen) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        // Create bodies for "Ground" layer
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rectangle.getX() + rectangle.getWidth() / 2) / PPM,
                (rectangle.getY() + rectangle.getHeight() / 2) / PPM); // Adjusted for PPM
            body = world.createBody(bodyDef);

            shape.setAsBox((rectangle.getWidth() / 2) / PPM,
                (rectangle.getHeight() / 2) / PPM); // Adjusted for PPM
            fixtureDef.shape = shape;
            fixtureDef.filter.categoryBits = CATEGORY_GROUND;
            fixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE;

            body.createFixture(fixtureDef).setUserData("environment");
        }

        // Create bodies for "Pipes" layer
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rectangle.getX() + rectangle.getWidth() / 2) / PPM,
                (rectangle.getY() + rectangle.getHeight() / 2) / PPM); // Adjusted for PPM
            body = world.createBody(bodyDef);

            shape.setAsBox((rectangle.getWidth() / 2) / PPM,
                (rectangle.getHeight() / 2) / PPM); // Adjusted for PPM
            fixtureDef.shape = shape;
            fixtureDef.filter.categoryBits = CATEGORY_GROUND;
            fixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE;

            body.createFixture(fixtureDef).setUserData("environment");
        }

        // Create Coin objects for "Coins" layer
        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            new Coin(world, map, rectangle); // Coins are using raw LibGDX coordinates, adjust internally if needed
        }

        // Create Brick objects for "Bricks" layer
        for (MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            new Brick(world, map, rectangle); // Bricks are using raw LibGDX coordinates, adjust internally if needed
        }

        // Create enemies for "SpawnPoints" layer
        for (MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

            gameScreen.addEnemy(new BatAICharacter(world, new Vector2((rectangle.x + 100) / PPM,
                (rectangle.y + 100) / PPM))); // Adjusted for PPM
            gameScreen.addEnemy(new ZombieAICharacter(world, new Vector2(rectangle.x / PPM,
                rectangle.y / PPM))); // Adjusted for PPM
            gameScreen.addEnemy(new RatAICharacter(world, new Vector2((rectangle.x + 100) / PPM,
                rectangle.y / PPM))); // Adjusted for PPM
        }

        // Create Maid objects for "SpawnPointsMaid" layer
        for (MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
//            gameScreen.addMaid(new MaidAICharacter(world, new Vector2(rectangle.x / PPM,
//                rectangle.y / PPM))); // Adjusted for PPM
        }

        shape.dispose();
    }
}

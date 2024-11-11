package krazy.cat.games.SaveTheMaid.Tools;

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

import krazy.cat.games.SaveTheMaid.Characters.ZombieEnemy;
import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Sprites.Brick;
import krazy.cat.games.SaveTheMaid.Sprites.Coin;

public class Box2dWorldCreator {
    public Box2dWorldCreator(World world, TiledMap map, GameScreen gameScreen) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        // goes through "Ground" layers to get bodies and create them
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rectangle.getX() + rectangle.getWidth() / 2), (rectangle.getY() + rectangle.getHeight() / 2));
            body = world.createBody(bodyDef);
            shape.setAsBox(rectangle.getWidth() / 2, rectangle.getHeight() / 2);
            fixtureDef.shape = shape;
            fixtureDef.filter.categoryBits = CATEGORY_GROUND;
            fixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE; // Collides with player, enemies, and projectiles

            body.createFixture(fixtureDef).setUserData("environment");
        }
        // goes through "Pipes" layers to get bodies and create them
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rectangle.getX() + rectangle.getWidth() / 2), (rectangle.getY() + rectangle.getHeight() / 2));
            body = world.createBody(bodyDef);
            shape.setAsBox(rectangle.getWidth() / 2, rectangle.getHeight() / 2);
            fixtureDef.shape = shape;
            fixtureDef.filter.categoryBits = CATEGORY_GROUND;
            fixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE; // Collides with player, enemies, and projectiles

            body.createFixture(fixtureDef).setUserData("environment");
        }
        // goes through "Coins" layers to get bodies and create them
        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            new Coin(world, map, rectangle); // ToDo: add to a list
        }
        // goes through "Bricks" layers to get bodies and create them
        for (MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            new Brick(world, map, rectangle);
        }     // goes through "Bricks" layers to get bodies and create them
        for (MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            gameScreen.addEnemy(new ZombieEnemy(world, new Vector2(rectangle.x, rectangle.y)));
        }
    }

}

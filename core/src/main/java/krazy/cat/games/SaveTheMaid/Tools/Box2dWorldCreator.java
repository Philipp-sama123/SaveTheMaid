package krazy.cat.games.SaveTheMaid.Tools;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_DESTROY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_GROUND;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.EnemyType;
import krazy.cat.games.SaveTheMaid.Characters.AI.EnemySpawnPoint;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.CatCharacter;
import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Sprites.Goal;

public class Box2dWorldCreator {
    public Box2dWorldCreator(World world, TiledMap map, BaseLevel baseLevel) {

        for (MapObject object : map.getLayers().get(2).getObjects()) {
            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                BodyDef bodyDef = new BodyDef();
                FixtureDef fixtureDef = new FixtureDef();
                Body body;
                float[] vertices = polygon.getTransformedVertices();

                // Convert vertices to Box2D's coordinate system
                Vector2[] worldVertices = new Vector2[vertices.length / 2];
                for (int i = 0; i < vertices.length / 2; i++) {
                    worldVertices[i] = new Vector2(
                        vertices[i * 2] / PPM,
                        vertices[i * 2 + 1] / PPM
                    );
                }

                // Validate polygon convexity and vertex order if needed here

                bodyDef.type = BodyDef.BodyType.StaticBody; // Explicitly set type
                body = world.createBody(bodyDef);

                PolygonShape polygonShape = new PolygonShape();
                polygonShape.set(worldVertices); // Ensure vertices form a convex polygon

                fixtureDef.shape = polygonShape;
                fixtureDef.filter.categoryBits = CATEGORY_GROUND;
                fixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE;

                body.createFixture(fixtureDef).setUserData("environment");

                polygonShape.dispose(); // Properly dispose of the shape
            }
            if (object instanceof RectangleMapObject) {
                Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                PolygonShape shape = new PolygonShape();
                BodyDef bodyDef = new BodyDef();
                FixtureDef fixtureDef = new FixtureDef();
                Body body;
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
        }

        // Create bodies for "Goal" layer
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            new Goal(world, map, rectangle); // Coins are using raw LibGDX coordinates, adjust internally if needed
        }
        // Create Goal objects for "Apple" layer
        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            // ToDo: Replace with another layer
        }
        // Create bodies for "Destroy" layer
        for (MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            // Center the body in the rectangle
            bodyDef.position.set((rectangle.getX() + rectangle.getWidth() / 2) / PPM,
                (rectangle.getY() + rectangle.getHeight() / 2) / PPM);

            Body body = world.createBody(bodyDef);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox((rectangle.getWidth() / 2) / PPM, (rectangle.getHeight() / 2) / PPM);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            // Set the collision category for the destroy zone
            fixtureDef.filter.categoryBits = CATEGORY_DESTROY;
            // Only allow collisions with the player and enemies
            fixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY;

            body.createFixture(fixtureDef).setUserData("destroy");

            shape.dispose(); // Clean up shape resources
        }

        // Create enemies for "SpawnPoints" layer
        for (MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            Vector2 spawnPosition = new Vector2(
                (rectangle.x + rectangle.width / 2) / PPM,  // Center X
                (rectangle.y + rectangle.height / 2) / PPM  // Center Y
            );

            Random rand = new Random();
            int random = rand.nextInt(4); // 0-3 inclusive
            EnemyType enemyType = EnemyType.ZOMBIE;
            switch (random) {
                case 0:
                    enemyType = EnemyType.ZOMBIE;
                    break;
                case 1:
                    enemyType = EnemyType.BAT;
                    break;
                case 2:
                    enemyType = EnemyType.DAMNED;
                    break;
                case 3:
                    enemyType = EnemyType.RAT;
                    break;
            }
            EnemySpawnPoint spawnPoint = new EnemySpawnPoint(
                world,
                spawnPosition,
                baseLevel,
                5f,  // 5 meter trigger range
                5f,  // 5 second spawn interval
                5,   // Max 5 enemies
                false,// Don't spawn all at once,
                enemyType
            );

            baseLevel.addSpawnPoint(spawnPoint);
        }

        // Create Friend objects for "SpawnPointsFriend" layer
        for (MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            baseLevel.addCat(new CatCharacter(world, new Vector2(rectangle.x / PPM,
                rectangle.y / PPM), baseLevel)); // Adjusted for PPM
        }
    }
}

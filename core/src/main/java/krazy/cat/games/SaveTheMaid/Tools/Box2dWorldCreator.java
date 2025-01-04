package krazy.cat.games.SaveTheMaid.Tools;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_GROUND;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
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

import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.BatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.RatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.ZombieAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.CatCharacter;
import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Sprites.Apple;
import krazy.cat.games.SaveTheMaid.Sprites.Brick;
import krazy.cat.games.SaveTheMaid.Sprites.Goal;
import krazy.cat.games.SaveTheMaid.Sprites.WaterEffect;

public class Box2dWorldCreator {
    public Box2dWorldCreator(World world, TiledMap map, GameScreen gameScreen) {


        // Create bodies for "Ground" layer
// Create bodies for "Ground" layer
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
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            new Apple(world, rectangle);
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
                (rectangle.y + 100) / PPM), gameScreen)); // Adjusted for PPM
            gameScreen.addEnemy(new ZombieAICharacter(world, new Vector2(rectangle.x / PPM,
                rectangle.y / PPM), gameScreen)); // Adjusted for PPM
            gameScreen.addEnemy(new RatAICharacter(world, new Vector2((rectangle.x + 100) / PPM,
                rectangle.y / PPM), gameScreen)); // Adjusted for PPM
        }

        // Create Friend objects for "SpawnPointsFriend" layer
        for (MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            gameScreen.addCat(new CatCharacter(world, new Vector2(rectangle.x / PPM,
                rectangle.y / PPM), gameScreen)); // Adjusted for PPM
        }
      //  createMultipleWaterEffects(world, map, gameScreen);

    }
    private void createMultipleWaterEffects(World world, TiledMap map, GameScreen gameScreen) {
        // Texture for the water sprite sheet (load globally if reused)
        Texture waterTexture = gameScreen.getWaterTexture();

        // Starting position for the water effects
        float startX = 80; // Starting X position of the first water effect
        float startY = 0; // Y position (same for all)

        // Number of water effects and spacing
        int numEffects = 10; // Number of water effects to create
        float spacing = 160; // Horizontal spacing between effects (adjust as needed)

        // Loop to create multiple water effects
        for (int i = 0; i < numEffects; i++) {
            float worldX = (startX + i * spacing) / PPM; // Calculate X position for each effect
            float worldY = startY / PPM; // Y position remains constant

            // Create a new water effect at the calculated position
            WaterEffect waterEffect = new WaterEffect(world, new Vector2(worldX, worldY), waterTexture);

            // Add the water effect to the game screen for rendering
            gameScreen.addWaterEffect(waterEffect);

            System.out.println("Water effect " + (i + 1) + " created at position: (" + worldX + ", " + worldY + ")");
        }
    }

    private void createSingleWaterEffect(World world, TiledMap map, GameScreen gameScreen) {
        // Texture for the water sprite sheet (load globally if reused)
        Texture waterTexture = gameScreen.getWaterTexture();

        // Determine the start position of the map
        float startX = 80; // Starting X position (adjust as needed)
        float startY = 10; // Starting Y position (adjust as needed)

        // Convert to world coordinates
        float worldX = startX / PPM;
        float worldY = startY / PPM;

        // Create a new water effect at the start of the map
        WaterEffect waterEffect = new WaterEffect(world, new Vector2(worldX, worldY), waterTexture);

        // Add the water effect to the game screen for rendering
        gameScreen.addWaterEffect(waterEffect);

        System.out.println("Single water effect created at the start of the map.");
    }


}

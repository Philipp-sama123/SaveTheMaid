package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.Scenes.Hud;
import krazy.cat.games.SaveTheMaid.Sprites.Player;
import krazy.cat.games.SaveTheMaid.Tools.Box2dWorldCreator;

public class GameScreen implements Screen {

    private final SaveTheMaidGame game;
    private final Player player;

    private Viewport gameViewport;
    private OrthographicCamera gameCamera;
    private Hud hud;

    // Tiled Map Variables
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    // Box2D Variables
    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;

    public GameScreen(SaveTheMaidGame game) {
        this.game = game;
        this.hud = new Hud(game.batch);

        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("Tiled/Level_1.tmx");
        this.renderer = new OrthogonalTiledMapRenderer(map, 1);

        this.gameCamera = new OrthographicCamera();
        this.gameViewport = new FitViewport((float) GAME_WIDTH, (float) GAME_HEIGHT, gameCamera);
        this.gameViewport.apply();
        this.gameCamera.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0.f);

        this.world = new World(new Vector2(0, -100), true);
        this.box2DDebugRenderer = new Box2DDebugRenderer();

        new Box2dWorldCreator(world, map);

        player = new Player(world);
    }

    @Override
    public void show() {

    }

    public void update(float dt) {
        handleInput(dt);

        world.step(1 / 60f, 6, 2);

        gameCamera.position.x = player.body.getPosition().x;
        gameCamera.position.y = player.body.getPosition().y;

        gameCamera.update();
        // just render what the camera can see
        renderer.setView(gameCamera);
    }

    private void handleInput(float dt) {
        // Get the x and y coordinates of the touch
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.input.getY();

            // Get the width and height of the screen
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();

            // Define boundaries for the middle row (between 33% and 66% height)
            float rowHeight = screenHeight / 3;
            float colWidth = screenWidth / 3;

            // Identify the regions
            boolean topLeft = touchX < colWidth && touchY < rowHeight;
            boolean topCenter = touchX >= colWidth && touchX < 2 * colWidth && touchY < rowHeight;
            boolean topRight = touchX >= 2 * colWidth && touchY < rowHeight;

            boolean middleLeft = touchX < colWidth && touchY >= rowHeight && touchY < 2 * rowHeight;
            boolean middleCenter = touchX >= colWidth && touchX < 2 * colWidth && touchY >= rowHeight && touchY < 2 * rowHeight;
            boolean middleRight = touchX >= 2 * colWidth && touchY >= rowHeight && touchY < 2 * rowHeight;

            boolean bottomLeft = touchX < colWidth && touchY >= 2 * rowHeight;
            boolean bottomCenter = touchX >= colWidth && touchX < 2 * colWidth && touchY >= 2 * rowHeight;
            boolean bottomRight = touchX >= 2 * colWidth && touchY >= 2 * rowHeight;


            // Apply different actions based on which region is touched
            if (topLeft) {
                //  player.body.applyLinearImpulse(new Vector2(-1f, 4f), player.body.getWorldCenter(), true);  // Move left and jump
            } else if (topCenter) {
                player.body.applyLinearImpulse(new Vector2(0, 250f), player.body.getWorldCenter(), true);
                //    player.body.applyLinearImpulse(new Vector2(0, 4f), player.body.getWorldCenter(), true);    // Jump straight up
            } else if (topRight) {
                //   player.body.applyLinearImpulse(new Vector2(1f, 4f), player.body.getWorldCenter(), true);   // Move right and jump
            } else if (middleLeft) {
                if (player.body.getLinearVelocity().x > -500.f)
                    player.body.applyLinearImpulse(new Vector2(-5f, 0), player.body.getWorldCenter(), true);   // Move left
            } else if (middleCenter) {
                //   player.body.applyLinearImpulse(new Vector2(0, 0), player.body.getWorldCenter(), true);     // Idle or stop
            } else if (middleRight) {
                if (player.body.getLinearVelocity().x < 500.f)
                    player.body.applyLinearImpulse(new Vector2(5f, 0), player.body.getWorldCenter(), true);    // Move right
            } else if (bottomLeft) {
                //       player.body.applyLinearImpulse(new Vector2(-1f, -1f), player.body.getWorldCenter(), true); // Move left and crouch (or stop)
            } else if (bottomCenter) {
                //       player.body.applyLinearImpulse(new Vector2(0, -1f), player.body.getWorldCenter(), true);   // Crouch
            } else if (bottomRight) {
                //       player.body.applyLinearImpulse(new Vector2(1f, -1f), player.body.getWorldCenter(), true);  // Move right and crouch
            }
        }
    }

    @Override
    public void render(float dt) {
        update(dt);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // render game map
        renderer.render();
        // render Box2D Debug
        box2DDebugRenderer.render(world, gameCamera.combined);
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
        hud.dispose();
    }
}

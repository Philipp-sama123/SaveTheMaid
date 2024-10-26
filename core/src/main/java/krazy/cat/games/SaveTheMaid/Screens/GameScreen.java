package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.Scenes.Hud;
import krazy.cat.games.SaveTheMaid.Player;
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

        this.world = new World(new Vector2(0, -125), false);
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
        gameCamera.position.y = player.body.getPosition().y + 32;

        gameCamera.update();
        // just render what the camera can see
        renderer.setView(gameCamera);
    }

    private void handleInput(float dt) {
        // Get the x and y coordinates of the touch
        float joystickPercentX = hud.getMovementJoystick().getKnobPercentX(); // Knob percentage movement on the X-axis
        player.move(joystickPercentX);
        if (hud.getJumpButton().isPressed()) {
            player.jump();
        }
        if (hud.getShootButton().isPressed()) {
            player.shoot();
        }
    }

    @Override
    public void render(float dt) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update(dt);
        player.update(dt);
        // render game map
        renderer.render();
        // render Box2D Debug
        box2DDebugRenderer.render(world, gameCamera.combined);
        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);
        //  player.updateAnimationState(game.batch);
        game.batch.end();

        hud.stage.act();
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

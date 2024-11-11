package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.Characters.ZombieEnemy;
import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.Scenes.Hud;
import krazy.cat.games.SaveTheMaid.Characters.Player;
import krazy.cat.games.SaveTheMaid.Tools.Box2dWorldCreator;
import krazy.cat.games.SaveTheMaid.WorldContactListener;

public class GameScreen implements Screen {
    private boolean jumpPressed = false;
    private boolean debugPressed = false;

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

    private Array<ZombieEnemy> enemies = new Array<>();
    public boolean isShowBox2dDebug;

    public GameScreen(SaveTheMaidGame game) {
        this.game = game;
        this.hud = new Hud(game.batch);


        this.mapLoader = new TmxMapLoader();
        // fix for map background artifacts
        this.map = mapLoader.load("Tiled/Level_1.tmx");
        for (TiledMapTileSet tileSet : map.getTileSets()) {
            for (TiledMapTile tile : tileSet) {
                if (tile.getTextureRegion() != null) {
                    tile.getTextureRegion().getTexture().setFilter(
                        Texture.TextureFilter.Linear, Texture.TextureFilter.Linear
                    );
                }
            }
        }
        this.renderer = new OrthogonalTiledMapRenderer(map, 1);

        this.gameCamera = new OrthographicCamera();
        this.gameViewport = new FitViewport(GAME_WIDTH, GAME_HEIGHT, gameCamera);
        this.gameViewport.apply();
        this.gameCamera.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0.f);

        this.world = new World(new Vector2(0, -125), false);
        this.box2DDebugRenderer = new Box2DDebugRenderer();
        // Initializes also the enemies
        new Box2dWorldCreator(world, map, this);
        world.setContactListener(new WorldContactListener());

        player = new Player(world);
    }

    @Override
    public void show() {

    }

    public void update(float dt) {
        handleInput(dt);

        world.step(1 / 60f, 6, 2);

        for (ZombieEnemy zombieEnemy : enemies) {
            zombieEnemy.update(dt, player.body.getPosition());
        }

        gameCamera.position.x = player.body.getPosition().x;
        gameCamera.position.y = player.body.getPosition().y + 32;

        gameCamera.update();
        // just render what the camera can see
        renderer.setView(gameCamera);
        hud.update(dt);
    }

    private void handleInput(float dt) {
        // Get the x and y coordinates of the touch
        float joystickPercentX = hud.getMovementJoystick().getKnobPercentX(); // Knob percentage movement on the X-axis
        player.move(joystickPercentX);
        // Jump button handling
        if (hud.getJumpButton().isPressed()) {
            if (!jumpPressed) { // If the jump button is pressed for the first time
                player.jump();
                jumpPressed = true; // Mark jump as pressed
            }
        } else {
            jumpPressed = false; // Reset when button is released
        }
        if (hud.getShootButton().isPressed()) {
            player.shoot();
        }
        if (hud.getShootUpButton().isPressed()) {
            player.shootUp();
        }
        if (hud.getDebugButton().isPressed()) {
            if (!debugPressed) {
                isShowBox2dDebug = !isShowBox2dDebug;
                debugPressed = true; // Mark debug as pressed
            }
        } else {
            debugPressed = false; // Reset when button is released
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
        if (isShowBox2dDebug)
            box2DDebugRenderer.render(world, gameCamera.combined);
        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);
        //  player.updateAnimationState(game.batch);
        for (ZombieEnemy zombieEnemy : enemies) {
            zombieEnemy.draw(game.batch);
        }
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

    public void addEnemy(ZombieEnemy zombieEnemy) {
        enemies.add(zombieEnemy);
    }
}

package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;

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
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.MaidAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.Player;
import krazy.cat.games.SaveTheMaid.FirebaseCallback;
import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.UI.Hud;
import krazy.cat.games.SaveTheMaid.Tools.Box2dWorldCreator;
import krazy.cat.games.SaveTheMaid.WorldContactListener;

public class GameScreen implements Screen {
    private final SaveTheMaidGame game;
    private final Player player;

    private Viewport gameViewport, uiViewport;
    private OrthographicCamera gameCamera, uiCamera;
    private Hud hud;

    // Tiled Map Variables
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    // Box2D Variables
    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;

    private Array<BaseAICharacter> enemies = new Array<>();
    private Array<BaseAICharacter> maids = new Array<>();
    public boolean isShowBox2dDebug;

    private float accumulator = 0;
    private final float fixedTimeStep = 1 / 60f;

    // Map dimensions in pixels
    public int mapWidthInPixels;
    public int mapHeightInPixels;

    public Player getPlayer() {
        return player;
    }

    public GameScreen(SaveTheMaidGame game) {
        this.game = game;

        // Initialize HUD with a separate camera and viewport
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new ExtendViewport(GAME_WIDTH, GAME_HEIGHT, uiCamera);
        this.hud = new Hud(game, uiViewport);

        // Load map and setup tiled renderer
        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("Tiled/Level_1.tmx");

        // Fix for map background artifacts
        for (TiledMapTileSet tileSet : map.getTileSets()) {
            for (TiledMapTile tile : tileSet) {
                if (tile.getTextureRegion() != null) {
                    tile.getTextureRegion().getTexture().setFilter(
                        Texture.TextureFilter.Linear, Texture.TextureFilter.Linear
                    );
                }
            }
        }

        this.renderer = new OrthogonalTiledMapRenderer(map, 1 / PPM); // Adjust for PPM

        // Game camera and viewport
        this.gameCamera = new OrthographicCamera();
        float aspectRatio = (float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        if (aspectRatio > 1) { // Portrait mode
            this.gameViewport = new ExtendViewport(GAME_WIDTH / PPM, (GAME_HEIGHT * 1.2f) / PPM, gameCamera);
        } else { // Landscape or square
            this.gameViewport = new ExtendViewport(GAME_WIDTH / PPM, GAME_HEIGHT / PPM, gameCamera);
        }
        this.gameViewport.apply();
        this.gameCamera.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0.f);

        // Box2D world setup
        this.world = new World(new Vector2(0, -125 / PPM), false); // Adjust gravity for PPM
        this.box2DDebugRenderer = new Box2DDebugRenderer();

        // Initialize map and entities
        new Box2dWorldCreator(world, map, this);
        world.setContactListener(new WorldContactListener());

        player = new Player(world);

        // Calculate map size
        int mapWidthInTiles = map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        int tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        int tilePixelHeight = map.getProperties().get("tileheight", Integer.class);

        mapWidthInPixels = mapWidthInTiles * tilePixelWidth;
        mapHeightInPixels = mapHeightInTiles * tilePixelHeight;
    }

    @Override
    public void show() {
        hud.enableInput();
    }

    public void update(float deltaTime) {
        accumulator += deltaTime;

        while (accumulator >= fixedTimeStep) {
            world.step(fixedTimeStep, 6, 2);
            accumulator -= fixedTimeStep;
        }

        for (var enemy : enemies) {
            enemy.update(deltaTime, player.body.getPosition());
        }
        for (var maid : maids) {
            maid.update(deltaTime, player.body.getPosition());
        }

        // Center camera on player while clamping within map bounds
        gameCamera.position.x = Math.max(
            gameCamera.viewportWidth / 2,
            Math.min(player.body.getPosition().x, mapWidthInPixels / PPM - gameCamera.viewportWidth / 2)
        );

        gameCamera.position.y = Math.max(
            gameCamera.viewportHeight / 2,
            Math.min(player.body.getPosition().y, mapHeightInPixels / PPM - gameCamera.viewportHeight / 2)
        );
        gameCamera.update();

        // Update renderer view and HUD
        renderer.setView(gameCamera);
        hud.update(deltaTime);
    }

    @Override
    public void render(float dt) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(dt);

        player.update(dt);

        // Render map
        renderer.render();

        // Render Box2D Debug
        if (isShowBox2dDebug)
            box2DDebugRenderer.render(world, gameCamera.combined);

        drawEntities();

        // Draw HUD/UI
        hud.healthLabel.setText(player.currentHealth + "/" + player.maxHealth);
        hud.stage.act();
        hud.stage.draw();
    }

    private void drawEntities() {
        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);
        for (BaseAICharacter enemy : enemies) enemy.draw(game.batch);
        for (BaseAICharacter maid : maids) maid.draw(game.batch);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float) height / width;

        // Recreate the game viewport for portrait and landscape adjustments
        if (aspectRatio > 1) { // Portrait mode
            gameViewport = new ExtendViewport((GAME_WIDTH-250) / PPM, (GAME_HEIGHT -100) / PPM, gameCamera);
        } else { // Landscape or square
            gameViewport = new ExtendViewport(GAME_WIDTH / PPM, GAME_HEIGHT / PPM, gameCamera);
        }

        // Apply and update the new viewport
        gameViewport.update(width, height, true);

        // Update the UI viewport
        uiViewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        hud.disableInput();
        // Example Firebase usage
        game.firebaseInterface.writeData("/ollaa/aja", "Hello Firebase!", new FirebaseCallback() {
            @Override
            public void onSuccess() {
                Gdx.app.log("WRITE DATA !!", "Data written successfully to Firebase!");
            }

            @Override
            public void onFailure(String errorMessage) {
                Gdx.app.log("WRITE DATA !!", "Failed to write data: " + errorMessage);
            }
        });
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
        hud.dispose();
    }

    public void addEnemy(BaseAICharacter enemy) {
        enemies.add(enemy);
    }

    public void addMaid(MaidAICharacter maidAICharacter) {
        maids.add(maidAICharacter);
    }
}

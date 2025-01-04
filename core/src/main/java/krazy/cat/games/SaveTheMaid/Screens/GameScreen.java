package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;

import krazy.cat.games.SaveTheMaid.*;
import krazy.cat.games.SaveTheMaid.Characters.AI.*;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.*;
import krazy.cat.games.SaveTheMaid.Characters.Player;
import krazy.cat.games.SaveTheMaid.Sprites.WaterEffect;
import krazy.cat.games.SaveTheMaid.Tools.Box2dWorldCreator;
import krazy.cat.games.SaveTheMaid.Tools.ScoreSystemManager;
import krazy.cat.games.SaveTheMaid.UI.Hud;
import krazy.cat.games.SaveTheMaid.WorldContactListener;

public class GameScreen implements Screen {
    // Core game and player references
    private final SaveTheMaidGame game;
    private final Player player;

    // Cameras and Viewports
    private final OrthographicCamera gameCamera;
    private final Viewport gameViewport;
    private final OrthographicCamera uiCamera;
    private final Viewport uiViewport;

    // HUD/UI
    private final Hud hud;

    // Tiled Map Variables
    private final TmxMapLoader mapLoader;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;

    // Box2D Physics
    private final World world;
    private final Box2DDebugRenderer box2DDebugRenderer;

    // Game Entities
    private final Array<BaseAICharacter> enemies = new Array<>();
    private final Array<BaseFriendAICharacter> friends = new Array<>();

    // Debug and Gameplay State
    public boolean isShowBox2dDebug;
    private float accumulator = 0f;
    private final float fixedTimeStep = 1 / 60f;

    // Map Dimensions
    public final int mapWidthInPixels;
    public final int mapHeightInPixels;
    private int enemiesKilled;
    private int catsSaved;


    private Array<WaterEffect> waterEffects = new Array<>();
    private Texture waterTexture = new Texture("PlatformerAssets/AnimatedSprites/WaterTiles.png");

    public GameScreen(SaveTheMaidGame game) {
        this.game = game;
        this.enemiesKilled = 0;
        this.catsSaved = 0;

        // Initialize Cameras and Viewports
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new StretchViewport(GAME_WIDTH, GAME_HEIGHT, uiCamera);
        this.hud = new Hud(game, uiViewport);

        this.gameCamera = new OrthographicCamera();
        this.gameViewport = new StretchViewport(GAME_WIDTH / PPM, GAME_HEIGHT / PPM, gameCamera);
        gameViewport.apply();

        // Load and Configure Map
        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("Tiled/Level_2.tmx");
        configureMapTextures();

        this.renderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);

        // Initialize Box2D World and Debug Renderer
        this.world = new World(new Vector2(0, -125 / PPM), false);
        this.box2DDebugRenderer = new Box2DDebugRenderer();

        // Initialize World Entities
        new Box2dWorldCreator(world, map, this);
        world.setContactListener(new WorldContactListener());
        this.player = new Player(world, this);

        // Calculate Map Dimensions
        this.mapWidthInPixels = calculateMapDimension("width") * calculateMapDimension("tilewidth");
        this.mapHeightInPixels = calculateMapDimension("height") * calculateMapDimension("tileheight");

        // Set Initial Camera Position
        gameCamera.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0);
    }

    // === Screen Lifecycle Methods ===

    @Override
    public void show() {
        hud.enableInput();
    }

    @Override
    public void render(float deltaTime) {
        clearScreen();

        update(deltaTime);
        renderMap();
        renderEntities();
        renderUI();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
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
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        box2DDebugRenderer.dispose();
        hud.dispose();
    }

    // === Core Update Logic ===

    private void update(float deltaTime) {
        updatePhysics(deltaTime);
        updateEntities(deltaTime);
        updateCamera();
        hud.update(deltaTime);
        waterEffects.forEach(waterEffect -> waterEffect.update(deltaTime));
    }

    private void updatePhysics(float deltaTime) {
        accumulator += deltaTime;
        while (accumulator >= fixedTimeStep) {
            world.step(fixedTimeStep, 6, 2);
            accumulator -= fixedTimeStep;
        }
    }

    private void updateEntities(float deltaTime) {
        player.update(deltaTime);

        for (BaseAICharacter enemy : enemies) {
            enemy.update(deltaTime, player.getBody().getPosition());
        }

        for (BaseFriendAICharacter friend : friends) {
            friend.update(deltaTime, player.getBody().getPosition());
        }
    }

    private void updateCamera() {
        gameCamera.position.x = Math.max(
            gameCamera.viewportWidth / 2,
            Math.min(player.getBody().getPosition().x, mapWidthInPixels / PPM - gameCamera.viewportWidth / 2)
        );

        gameCamera.position.y = Math.max(
            gameCamera.viewportHeight / 2,
            Math.min(player.getBody().getPosition().y, mapHeightInPixels / PPM - gameCamera.viewportHeight / 2)
        );

        gameCamera.update();
        renderer.setView(gameCamera);
    }

    // === Render Helpers ===

    private void renderMap() {
        renderer.render();

        if (isShowBox2dDebug) {
            box2DDebugRenderer.render(world, gameCamera.combined);
        }
    }

    private void renderEntities() {
        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);

        for (BaseAICharacter enemy : enemies) {
            enemy.draw(game.batch);
        }

        for (BaseFriendAICharacter friend : friends) {
            friend.draw(game.batch);
        }

        waterEffects.forEach(waterEffect -> waterEffect.render(game.batch));
        game.batch.end();
    }

    private void renderUI() {
        hud.updateHealth(player.getCurrentHealth(), player.getMaxHealth());
        hud.stage.act();
        hud.stage.draw();
    }

    // === Utility Methods ===

    private void configureMapTextures() {
        for (TiledMapTileSet tileSet : map.getTileSets()) {
            for (TiledMapTile tile : tileSet) {
                if (tile.getTextureRegion() != null) {
                    tile.getTextureRegion().getTexture().setFilter(
                        Texture.TextureFilter.Linear, Texture.TextureFilter.Linear
                    );
                }
            }
        }
    }

    private int calculateMapDimension(String property) {
        return map.getProperties().get(property, Integer.class);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    // === Public Accessors ===

    public void addEnemy(BaseAICharacter enemy) {
        enemies.add(enemy);
    }

    public void addCat(BaseFriendAICharacter cat) {
        friends.add(cat);
    }

    public Player getPlayer() {
        return player;
    }

    public void addEnemyKill() {
        enemiesKilled++;
        ScoreSystemManager.getInstance().addScore(10);
        hud.updateScore();
    }

    public void addCatSaved() {
        catsSaved++;
        ScoreSystemManager.getInstance().addScore(100);
        hud.updateScore();
    }

    public void showGameOverScreen() {
        game.setScreen(new GameOverScreen(game, hud.getWorldTimer(), enemiesKilled, catsSaved));
    }

    public Texture getWaterTexture() {
        return waterTexture;
    }

    public void addWaterEffect(WaterEffect waterEffect) {
        waterEffects.add(waterEffect);
    }
}

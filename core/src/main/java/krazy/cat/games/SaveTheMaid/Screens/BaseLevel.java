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
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.RatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.BaseFriendAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.CatCharacter;
import krazy.cat.games.SaveTheMaid.Characters.Player;
import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.Tools.Box2dWorldCreator;
import krazy.cat.games.SaveTheMaid.Tools.ScoreSystemManager;
import krazy.cat.games.SaveTheMaid.UI.Hud;
import krazy.cat.games.SaveTheMaid.WorldContactListener;

public abstract class BaseLevel implements Screen {
    protected final SaveTheMaidGame game;
    protected final OrthographicCamera gameCamera;
    protected final Viewport gameViewport;
    protected final OrthographicCamera uiCamera;
    protected final Viewport uiViewport;
    protected final Hud hud;
    protected final TmxMapLoader mapLoader;
    protected final TiledMap map;
    protected final OrthogonalTiledMapRenderer renderer;
    protected final World world;
    protected final Box2DDebugRenderer box2DDebugRenderer;

    protected final Array<BaseAICharacter> enemies = new Array<>();
    protected final Array<BaseFriendAICharacter> friends = new Array<>();

    protected Player player;

    public boolean isShowBox2dDebug;
    private float accumulator = 0f;
    private final float fixedTimeStep = 1 / 60f;
    protected final int mapWidthInPixels;
    protected final int mapHeightInPixels;
    protected int enemiesKilled;
    protected int catsSaved;

    public BaseLevel(SaveTheMaidGame game, String mapPath) {
        this.game = game;
        this.enemiesKilled = 0;
        this.catsSaved = 0;
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new StretchViewport(GAME_WIDTH, GAME_HEIGHT, uiCamera);
        this.hud = new Hud(game, uiViewport);
        this.gameCamera = new OrthographicCamera();
        this.gameViewport = new StretchViewport(GAME_WIDTH / PPM, GAME_HEIGHT / PPM, gameCamera);
        gameViewport.apply();
        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load(mapPath);
        configureMapTextures();
        this.renderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);
        this.world = new World(new Vector2(0, -125 / PPM), false);
        this.box2DDebugRenderer = new Box2DDebugRenderer();
        new Box2dWorldCreator(world, map, this);
        world.setContactListener(new WorldContactListener());
        this.player = new Player(world, this);
        this.mapWidthInPixels = calculateMapDimension("width") * calculateMapDimension("tilewidth");
        this.mapHeightInPixels = calculateMapDimension("height") * calculateMapDimension("tileheight");
        gameCamera.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0);
    }

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

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    private void update(float deltaTime) {
        updatePhysics(deltaTime);
        updateEntities(deltaTime);
        updateCamera();
        hud.update(deltaTime);
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
        for (BaseAICharacter enemy : enemies)
            enemy.update(deltaTime, player.getBody().getPosition());
        for (BaseFriendAICharacter friend : friends)
            friend.update(deltaTime, player.getBody().getPosition());
    }

    private void updateCamera() {
        gameCamera.position.x = Math.max(gameCamera.viewportWidth / 2, Math.min(player.getBody().getPosition().x, mapWidthInPixels / PPM - gameCamera.viewportWidth / 2));
        gameCamera.position.y = Math.max(gameCamera.viewportHeight / 2, Math.min(player.getBody().getPosition().y, mapHeightInPixels / PPM - gameCamera.viewportHeight / 2));
        gameCamera.update();
        renderer.setView(gameCamera);
    }

    private void renderMap() {
        renderer.render();
        if (isShowBox2dDebug) box2DDebugRenderer.render(world, gameCamera.combined);
    }

    private void renderEntities() {
        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);
        for (BaseAICharacter enemy : enemies) enemy.draw(game.batch);
        for (BaseFriendAICharacter friend : friends) friend.draw(game.batch);
        game.batch.end();
    }

    private void renderUI() {
        hud.updateHealth(player.getCurrentHealth(), player.getMaxHealth());
        hud.stage.act();
        hud.stage.draw();
    }

    private void configureMapTextures() {
        for (TiledMapTileSet tileSet : map.getTileSets()) {
            for (TiledMapTile tile : tileSet) {
                if (tile.getTextureRegion() != null) {
                    tile.getTextureRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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

    public void addCat(BaseFriendAICharacter cat) {
        friends.add(cat);
    }

    public void addEnemy(BaseAICharacter enemy) {
        enemies.add(enemy);
    }


    public Player getPlayer() {
        return player;
    }
}

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

import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.MaidAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.Player;
import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.UI.Hud;
import krazy.cat.games.SaveTheMaid.Tools.Box2dWorldCreator;
import krazy.cat.games.SaveTheMaid.WorldContactListener;

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

    // private Array<ZombieEnemy> enemies = new Array<>();
    private Array<BaseAICharacter> enemies = new Array<>();
    private Array<BaseAICharacter> maids = new Array<>();
    public boolean isShowBox2dDebug;

    // map dimensions in pixels
    public int mapWidthInPixels;
    public int mapHeightInPixels;

    public Player getPlayer() {
        return player;
    }

    public GameScreen(SaveTheMaidGame game) {
        this.game = game;

        this.hud = new Hud(game);

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

        // Calculate map size
        int mapWidthInTiles = map.getProperties().get("width", Integer.class);       // Number of tiles in width
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);     // Number of tiles in height
        int tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);    // Tile width in pixels
        int tilePixelHeight = map.getProperties().get("tileheight", Integer.class);  // Tile height in pixels

        mapWidthInPixels = mapWidthInTiles * tilePixelWidth;
        mapHeightInPixels = mapHeightInTiles * tilePixelHeight;
    }

    @Override
    public void show() {
        hud.enableInput();
    }

    public void update(float dt) {
        world.step(1 / 60f, 6, 2);

        for (var enemy : enemies) {
            enemy.update(dt, player.body.getPosition());
        }
        for (var maid : maids) {
            maid.update(dt, player.body.getPosition());
        }

        // Update Camera
        gameCamera.position.x = Math.max(player.body.getPosition().x, (float) GAME_WIDTH / 2);
        gameCamera.position.y = Math.max(player.body.getPosition().y + 32, (float) GAME_HEIGHT / 2);

        gameCamera.position.x = Math.min(gameCamera.position.x, mapWidthInPixels);
        gameCamera.position.y = Math.min(gameCamera.position.y, mapHeightInPixels);
        gameCamera.update();

        // just render what the camera can see
        renderer.setView(gameCamera);
        hud.update(dt);
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
        //  player.updateAnimationState(game.batch);
        player.draw(game.batch);
        for (BaseAICharacter enemy : enemies) {
            enemy.draw(game.batch);
        }
        for (BaseAICharacter maid : maids) {
            maid.draw(game.batch);
        }
        hud.healthLabel.setText(player.currentHealth + "/" + player.maxHealth);
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

    public void addEnemy(BaseAICharacter enemy) {
        enemies.add(enemy);
    }

    public void addMaid(MaidAICharacter maidAICharacter) {
        maids.add(maidAICharacter);
    }
}

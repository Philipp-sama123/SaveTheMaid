package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Screens.HellLevel;
import krazy.cat.games.SaveTheMaid.Screens.PauseScreen;
import krazy.cat.games.SaveTheMaid.Screens.StartupScreen;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;
import krazy.cat.games.SaveTheMaid.Tools.ScoreSystemManager;

public class SaveTheMaidGame extends Game {
    public static final float PPM = 100; // 100 pixels = 1 meter
    public static final int GAME_WIDTH = 384;
    public static final int GAME_HEIGHT = 192;

    public SpriteBatch batch;

    private GameScreen gameScreen;
    private HellLevel hellLevel;
    private PauseScreen pauseScreen;
    private StartupScreen startupScreen;
    private BaseLevel activeLevel;

    public void setActiveLevel(BaseLevel level) {
        activeLevel = level;
    }

    public BaseLevel getActiveLevel() {
        return activeLevel;
    }

    public SaveTheMaidGame() {
    }

    @Override
    public void create() {
        GameAssetManager.getInstance(); // Initializes and loads assets
        GameAssetManager.getInstance().getAssetManager().finishLoading();
        ScoreSystemManager.getInstance();

        batch = new SpriteBatch();
        startupScreen = new StartupScreen(this);
        gameScreen = new GameScreen(this);
        hellLevel = new HellLevel(this);
        pauseScreen = new PauseScreen(this);

        setScreen(getStartupScreen());
    }

    public void reinitializeGameScreen() {
        gameScreen = null;
        gameScreen = new GameScreen(this);
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public HellLevel getHellLevel() {
        return hellLevel;
    }

    public StartupScreen getStartupScreen() {
        return startupScreen;
    }

    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }

    @Override
    public void dispose() {
        GameAssetManager.getInstance().dispose(); // Dispose assets when the game ends
    }
}

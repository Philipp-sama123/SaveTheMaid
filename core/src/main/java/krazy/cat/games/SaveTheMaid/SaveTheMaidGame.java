package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Screens.VillageLevel;
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

    private VillageLevel villageLevel;
    private HellLevel hellLevel;
    private PauseScreen pauseScreen;
    private StartupScreen startupScreen;
    private BaseLevel currentLevel;


    public SaveTheMaidGame() {
    }

    @Override
    public void create() {
        GameAssetManager.getInstance(); // Initializes and loads assets
        GameAssetManager.getInstance().getAssetManager().finishLoading();
        ScoreSystemManager.getInstance();

        batch = new SpriteBatch();

        startupScreen = new StartupScreen(this);

        villageLevel = new VillageLevel(this);
        hellLevel = new HellLevel(this);

        pauseScreen = new PauseScreen(this);

        setScreen(getStartupScreen());
    }

    public void startHellLevel() {
        setCurrentLevel(hellLevel);
        setScreen(hellLevel);
    }

    public void startVillageLevel() {
        setCurrentLevel(villageLevel);
        setScreen(villageLevel);
    }

    public void reloadCurrentLevel() {
        if (currentLevel instanceof VillageLevel) {
            villageLevel = null;
            villageLevel = new VillageLevel(this);
        } else if (currentLevel instanceof HellLevel) {
            hellLevel = null;
            hellLevel = new HellLevel(this);
        }

    }

    public VillageLevel getVillageLevel() {
        return villageLevel;
    }

    public HellLevel getHellLevel() {
        return hellLevel;
    }

    public void setCurrentLevel(BaseLevel level) {
        currentLevel = level;
    }

    public BaseLevel getCurrentLevel() {
        return currentLevel;
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

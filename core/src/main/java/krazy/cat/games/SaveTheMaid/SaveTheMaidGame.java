package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Screens.PauseScreen;
import krazy.cat.games.SaveTheMaid.Screens.StartupScreen;

public class SaveTheMaidGame extends Game {
    public SpriteBatch batch;

    public static final int GAME_WIDTH = 384;
    public static final int GAME_HEIGHT = 192;
    private GameScreen gameScreen;
    private PauseScreen pauseScreen;
    private StartupScreen startupScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        startupScreen = new StartupScreen(this);
        gameScreen = new GameScreen(this);
        pauseScreen = new PauseScreen(this);

        setScreen(getStartupScreen());
    }


    public GameScreen getGameScreen() {
        return gameScreen;
    }
    public StartupScreen getStartupScreen() {
        return startupScreen;
    }
    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }
}

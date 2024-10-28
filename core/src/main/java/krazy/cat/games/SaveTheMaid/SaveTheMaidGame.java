package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Screens.StartupScreen;

public class SaveTheMaidGame extends Game {
    public SpriteBatch batch;

    public static final int GAME_WIDTH =  384;
    public static final int GAME_HEIGHT = 192;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new StartupScreen(this));
    }

}

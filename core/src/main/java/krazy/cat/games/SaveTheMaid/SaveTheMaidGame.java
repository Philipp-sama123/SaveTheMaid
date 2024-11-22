package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import krazy.cat.games.SaveTheMaid.Screens.GameScreen;
import krazy.cat.games.SaveTheMaid.Screens.PauseScreen;
import krazy.cat.games.SaveTheMaid.Screens.StartupScreen;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class SaveTheMaidGame extends Game {
    public static final float PPM = 100; // 100 pixels = 1 meter
    public static final int GAME_WIDTH = 384;
    public static final int GAME_HEIGHT = 192;

    public SpriteBatch batch;
    private AssetManager assetManager;

    private GameScreen gameScreen;
    private PauseScreen pauseScreen;
    private StartupScreen startupScreen;

    public FirebaseInterface firebaseInterface;

    public SaveTheMaidGame(FirebaseInterface firebaseInterface) {
        this.firebaseInterface = firebaseInterface;
    }

    @Override
    public void create() {
        GameAssetManager.getInstance(); // Initializes and loads assets
        batch = new SpriteBatch();
        assetManager = new AssetManager();

        assetManager.finishLoading(); // Wait until all assets are loaded

        startupScreen = new StartupScreen(this);
        gameScreen = new GameScreen(this);
        pauseScreen = new PauseScreen(this);

        setScreen(getStartupScreen());

        // Example Firebase usage
        firebaseInterface.writeData("examplePath", "Hello Firebase!");
        Gdx.app.log("firebaseInterface", firebaseInterface + " ");
    }

    public void reinitializeGameScreen() {
        gameScreen = null;
        gameScreen = new GameScreen(this);
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

    public AssetManager getAssetManager() {
        return assetManager;
    }

//    public boolean isUserSignedIn() {
//        FirebaseUser user = firebaseInterface.getCurrentUser(); // Assuming getCurrentUser() returns the current Firebase user if signed in
//        Gdx.app.log("USER", "firebaseInterface.getUserEmail()" + firebaseInterface.getUserEmail());
//        Gdx.app.log("USER", "firebaseInterface.getUserDisplayName()" + firebaseInterface.getUserDisplayName());
//        return user != null;
//    }

    @Override
    public void dispose() {
        GameAssetManager.getInstance().dispose(); // Dispose assets when the game ends
    }
}

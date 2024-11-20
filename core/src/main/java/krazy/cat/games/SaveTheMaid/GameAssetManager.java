package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class GameAssetManager {
    private static GameAssetManager instance;
    private final AssetManager assetManager;

    private GameAssetManager() {
        assetManager = new AssetManager();
        loadAssets();
        assetManager.finishLoading(); // Ensure all assets are loaded
    }

    public static synchronized GameAssetManager getInstance() {
        if (instance == null) {
            instance = new GameAssetManager();
        }
        return instance;
    }

    private void loadAssets() {
        // Enemy Assets
        assetManager.load("SFX/swipe.mp3", Sound.class);
        assetManager.load("SFX/PlayerHit.wav", Sound.class);
        assetManager.load("SFX/ZombieAttack.wav", Sound.class);
        
        assetManager.load("Characters/Rat/Rat_v3/Sprite Sheet/Rat_v3_Sheet.png", Texture.class);
        assetManager.load("Characters/Bat/Bat_v1/Sprite Sheet/Bat_v1_Sheet.png", Texture.class);
        assetManager.load("Characters/Zombie/Colors/Grey.png", Texture.class);

        // Player Assets
        assetManager.load("Characters/FemaleAgent/Body/Black.png", Texture.class);
        assetManager.load("Characters/FemaleAgent/Feet/Red.png", Texture.class);
        assetManager.load("JumpEffect.png", Texture.class);
        assetManager.load("PlayerBloodEffect.png", Texture.class);
        assetManager.load("Characters/FemaleAgent/PixelBullet16x16.png", Texture.class);
        assetManager.load("SFX/Jump.wav", Sound.class);
        assetManager.load("SFX/Shoot.wav", Sound.class);
        assetManager.load("SFX/PlayerHit.wav", Sound.class);
    }

    public <T> T get(String assetPath, Class<T> type) {
        return assetManager.get(assetPath, type);
    }

    public void dispose() {
        assetManager.dispose();
    }
}

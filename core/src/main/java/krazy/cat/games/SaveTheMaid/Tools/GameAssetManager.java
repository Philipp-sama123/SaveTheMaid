package krazy.cat.games.SaveTheMaid.Tools;

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
        //-Enemy Assets
        assetManager.load(AssetPaths.RAT_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.BAT_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.ZOMBIE_GREY_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.DAMNED_MALE, Texture.class);
        assetManager.load(AssetPaths.DAMNED_FEMALE, Texture.class);
        // --SFX
        assetManager.load(AssetPaths.SWIPE_SOUND, Sound.class);
        assetManager.load(AssetPaths.PLAYER_HIT_SOUND, Sound.class);
        assetManager.load(AssetPaths.ZOMBIE_ATTACK_SOUND, Sound.class);

        // Maid Character
        assetManager.load(AssetPaths.MAID_CHARACTER_BLACK, Texture.class);

        // Player Assets
        assetManager.load(AssetPaths.PLAYER_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.PLAYER_JUMP_EFFECT_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.PLAYER_SLIDE_EFFECT_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.PLAYER_DESTROY_EFFECT_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.PLAYER_BLOOD_EFFECT_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.AGENT_PIXEL_BULLET_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.AGENT_PIXEL_SHOOT_UP_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.AGENT_PIXEL_SHOOT_UP_EXPLOSION_TEXTURE, Texture.class);

        //SFX
        assetManager.load(AssetPaths.JUMP_SOUND, Sound.class);
        assetManager.load(AssetPaths.SHOOT_SOUND, Sound.class);
        assetManager.load(AssetPaths.PLAYER_FOOTSTEP_SOUND, Sound.class);
        assetManager.load(AssetPaths.PLAYER_SLIDE_SOUND, Sound.class);

        assetManager.load(AssetPaths.CAT_PICKUP_SOUND, Sound.class);
        assetManager.load(AssetPaths.CAT_GOAL_SOUND, Sound.class);

        assetManager.load(AssetPaths.HEALTH_BAR_CONTAINER, Texture.class);
        assetManager.load(AssetPaths.HEALTH_BAR_ANIMATIONS, Texture.class);
        assetManager.load(AssetPaths.HEALTH_BAR_SIMPLE, Texture.class);

        assetManager.load(AssetPaths.CAT_TEXTURE_2, Texture.class);

        assetManager.load(AssetPaths.LIFE_PICKUP_TEXTURE, Texture.class);
        assetManager.load(AssetPaths.AMMO_PICKUP_TEXTURE, Texture.class);

    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public <T> T get(String assetPath, Class<T> type) {
        return assetManager.get(assetPath, type);
    }

    public void dispose() {
        assetManager.dispose();
    }
}

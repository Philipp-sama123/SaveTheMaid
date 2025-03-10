package krazy.cat.games.SaveTheMaid.Characters.Player;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.Tools.Utils.createAnimation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class PlayerEffectManager {
    private final Player player;

    private static final float BLOOD_EFFECT_SCALE = 0.5f; // 50% of original size
    private static final int JUMP_EFFECT_Y_OFFSET = 45;
    private static final int JUMP_EFFECT_X_OFFSET = 10;


    public PlayerEffectManager(Player player) {
        this.player = player;
    }

    // Effect animations and timers
    private Animation<TextureRegion> jumpEffectAnimation;
    private Animation<TextureRegion> bloodEffectAnimation;
    private Animation<TextureRegion> slideEffectAnimation;
    private Animation<TextureRegion> destroyEffectAnimation;

    private boolean showJumpEffect;
    private boolean showSlideEffect;
    private boolean showBloodEffect;
    private boolean showDestroyEffect;

    private float jumpEffectTime;
    private float bloodEffectTime;
    private float slideEffectTime;
    private float destroyEffectTime;


    protected void drawEffects(Batch batch) {
        // Draw slide effect
        if (showSlideEffect) {
            TextureRegion slideEffectFrame = slideEffectAnimation.getKeyFrame(slideEffectTime);
            // Ensure correct horizontal orientation
            if ((player.isFacingRight && slideEffectFrame.isFlipX()) || (!player.isFacingRight && !slideEffectFrame.isFlipX())) {
                slideEffectFrame.flip(true, false);
            }
            float effectPosX = (player.getBody().getPosition().x) - (slideEffectFrame.getRegionWidth() / 2f / PPM)
                - (player.isFacingRight ? 20 / PPM : -20 / PPM);
            float effectPosY = (player.getBody().getPosition().y) - 25 / PPM;
            batch.draw(slideEffectFrame, effectPosX, effectPosY,
                slideEffectFrame.getRegionWidth() / PPM, slideEffectFrame.getRegionHeight() / PPM);
        }
        // Draw jump effect
        if (showJumpEffect) {
            TextureRegion jumpEffectFrame = jumpEffectAnimation.getKeyFrame(jumpEffectTime);
            float effectPosX = player.getBody().getPosition().x - ((jumpEffectFrame.getRegionWidth() + JUMP_EFFECT_X_OFFSET) / 2f / PPM);
            float effectPosY = player.getBody().getPosition().y - (JUMP_EFFECT_Y_OFFSET / PPM);
            batch.draw(jumpEffectFrame, effectPosX, effectPosY,
                jumpEffectFrame.getRegionWidth() / PPM,
                jumpEffectFrame.getRegionHeight() / PPM);
        }
        // Draw blood effect
        if (showBloodEffect) {
            TextureRegion bloodEffectFrame = bloodEffectAnimation.getKeyFrame(bloodEffectTime);
            float effectPosX = player.getBody().getPosition().x - ((bloodEffectFrame.getRegionWidth() / PPM) * BLOOD_EFFECT_SCALE / 2);
            float effectPosY = player.getBody().getPosition().y;
            batch.draw(bloodEffectFrame, effectPosX, effectPosY,
                (bloodEffectFrame.getRegionWidth() / PPM) * BLOOD_EFFECT_SCALE,
                (bloodEffectFrame.getRegionHeight() / PPM) * BLOOD_EFFECT_SCALE);
        }
        // Draw blood effect
        if (showDestroyEffect) {
            TextureRegion destroyEffectFrame = destroyEffectAnimation.getKeyFrame(destroyEffectTime);
            float effectPosX = player.getBody().getPosition().x - (destroyEffectFrame.getRegionWidth() / 2f / PPM);
            float effectPosY = player.getBody().getPosition().y - (destroyEffectFrame.getRegionHeight() / 2f / PPM);
            batch.draw(destroyEffectFrame, effectPosX, effectPosY,
                (destroyEffectFrame.getRegionWidth() / PPM),
                (destroyEffectFrame.getRegionHeight() / PPM));
        }
    }

    /**
     * Update the timers for the jump, slide, and blood effects.
     */
    protected void updateEffects(float delta) {
        if (showJumpEffect) {
            jumpEffectTime += delta;
            if (jumpEffectAnimation.isAnimationFinished(jumpEffectTime)) {
                showJumpEffect = false;
            }
        }
        if (showSlideEffect) {
            slideEffectTime += delta;
            if (slideEffectAnimation.isAnimationFinished(slideEffectTime)) {
                showSlideEffect = false;
            }
        }
        if (showBloodEffect) {
            bloodEffectTime += delta;
            if (bloodEffectAnimation.isAnimationFinished(bloodEffectTime)) {
                showBloodEffect = false;
            }
        }
        if (showDestroyEffect) {
            destroyEffectTime += delta;
            if (destroyEffectAnimation.isAnimationFinished(destroyEffectTime)) {
                showDestroyEffect = false;
            }
        }
    }

    protected void loadEffects() {
        Texture jumpSpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_JUMP_EFFECT_TEXTURE, Texture.class);
        Texture bloodSpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_BLOOD_EFFECT_TEXTURE, Texture.class);
        Texture slideSmokeSpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_SLIDE_EFFECT_TEXTURE, Texture.class);
        Texture destroySpriteSheet = GameAssetManager.getInstance().get(AssetPaths.PLAYER_DESTROY_EFFECT_TEXTURE, Texture.class);

        jumpEffectAnimation = createAnimation(jumpSpriteSheet, 64, 64, 6, 0.1f);
        bloodEffectAnimation = createAnimation(bloodSpriteSheet, 110, 86, 7, 0.1f);
        slideEffectAnimation = createAnimation(slideSmokeSpriteSheet, 48, 32, 9, 0.05f);
        destroyEffectAnimation = createAnimation(destroySpriteSheet, 64, 64, 13, 0.1f);
    }

    protected void triggerDestroyEffect() {
        showDestroyEffect = true;
        destroyEffectTime = 0;
    }

    protected void triggerBloodEffect() {
        showBloodEffect = true;
        bloodEffectTime = 0;
    }

    protected void triggerJumpEffect() {
        showJumpEffect = true;
        jumpEffectTime = 0;
    }

    protected void triggerSlideEffect() {
        showSlideEffect = true;
        slideEffectTime = 0;
    }

    public boolean isShowBloodEffect() {
        return showBloodEffect;
    }

    public boolean isShowJumpEffect() {
        return showJumpEffect;
    }

    public boolean isShowDestroyEffect() {
        return showDestroyEffect;
    }

    public boolean isShowSlideEffect() {
        return showSlideEffect;
    }
}

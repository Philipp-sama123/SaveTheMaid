package krazy.cat.games.SaveTheMaid.Characters.Player;

import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.JUMP_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.PLAYER_FOOTSTEP_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.PLAYER_HIT_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.PLAYER_SLIDE_SOUND;
import static krazy.cat.games.SaveTheMaid.Tools.AssetPaths.SHOOT_SOUND;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetFemaleAgent.AnimationType;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class PlayerSoundManager {
    private final Player player;

    private Sound jumpSound;
    private Sound shootSound;
    private Sound hitSound;
    private Sound footstepSound;
    private Sound slideSound;

    // Used to prevent duplicate footstep sounds
    private int lastFootstepFrame = -1;

    public PlayerSoundManager(Player player) {
        this.player= player;
        initializeSounds();
    }

    private void initializeSounds() {
        jumpSound = GameAssetManager.getInstance().getAssetManager().get(JUMP_SOUND);
        shootSound = GameAssetManager.getInstance().getAssetManager().get(SHOOT_SOUND);
        hitSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_HIT_SOUND);
        footstepSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_FOOTSTEP_SOUND);
        slideSound = GameAssetManager.getInstance().getAssetManager().get(PLAYER_SLIDE_SOUND);
    }

    public void playJumpSound() {
        if (jumpSound != null) {
            jumpSound.play(0.25f);
        }
    }

    public void playShootSound() {
        if (shootSound != null) {
            shootSound.play();
        }
    }

    public void playHitSound() {
        if (hitSound != null) {
            hitSound.play();
        }
    }

    public void playSlideSound() {
        if (slideSound != null) {
            slideSound.play(0.25f);
        }
    }

    /**
     * Handles the logic for playing footstep sounds.
     *
     * @param currentAnimationState The current animation state (WALK or RUN).
     * @param currentAnimation      The current animation being played.
     * @param stateTime             The state time for the animation.
     */
    public void handleFootstepSound(AnimationType currentAnimationState, Animation<TextureRegion> currentAnimation, float stateTime) {
        // Define a helper to map shooting states to their non-shooting counterparts.
        if (currentAnimationState == AnimationType.WALK_SHOOT) {
            currentAnimationState = AnimationType.WALK;
        } else if (currentAnimationState == AnimationType.RUN_SHOOT) {
            currentAnimationState = AnimationType.RUN;
        }

        // Only trigger footsteps for walking or running
        if (currentAnimationState == AnimationType.WALK || currentAnimationState == AnimationType.RUN) {
            // Compute continuous animation time using modulo to keep it within the animation duration.
            float animationDuration = currentAnimation.getAnimationDuration();
            float animTime = stateTime % animationDuration;
            int currentFrameIndex = currentAnimation.getKeyFrameIndex(animTime);

            // Log frame index for debugging (optional)
            // Gdx.app.log("Footstep", "Frame Index: " + currentFrameIndex);

            // Check for specific frame triggers (e.g., 0 or 4) and avoid duplicate triggers.
            if ((currentFrameIndex == 0 || currentFrameIndex == 4) && currentFrameIndex != lastFootstepFrame) {
                if (footstepSound != null) {
                    footstepSound.stop();
                    float pitch = currentAnimationState == AnimationType.WALK ? 0.0f : 0.5f;
                    footstepSound.play(0.25f, pitch, 0.0f);
                }
                lastFootstepFrame = currentFrameIndex;
            }

            // Reset the last frame when the loop completes
            if (animTime < 0.01f) {  // Near start of a new loop
                lastFootstepFrame = -1;
            }
        } else {
            // When not walking or running, reset so footsteps can trigger when movement resumes.
            lastFootstepFrame = -1;
        }
    }

    // Optionally, you can add a reset method for footstep sound state if needed.
    public void resetFootstep() {
        lastFootstepFrame = -1;
    }
}

package krazy.cat.games.SaveTheMaid.Characters.AnimationSets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSetRat {
    private static final float FRAME_DURATION = 0.1f;
    public static final int FRAME_WIDTH = 62;
    public static final int FRAME_HEIGHT = 44;
    private static final int SPRITE_SHEET_COLUMNS = 9; // 10 frames per row

    public enum RatAnimationType {
        IDLE, MOVE_1, MOVE_2, TURN, ATTACK_1, ATTACK_2, HIT, DEATH_1, DEATH_2
    }

    private final Map<RatAnimationType, Animation<TextureRegion>> animations;
    private boolean flipped = false;

    public AnimationSetRat(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        animations = new EnumMap<>(RatAnimationType.class);
        animations.put(RatAnimationType.IDLE, createAnimation(textureRegions, 0, 0, 8));             // Row 0, Frames 0-7
        animations.put(RatAnimationType.MOVE_1, createAnimation(textureRegions, 0, 8, 8));          // Row 0, Frame 8 + Row 1, Frames 0-6
        animations.put(RatAnimationType.MOVE_2, createAnimation(textureRegions, 1, 7, 8));          // Row 1, Frames 7-8 + Row 2, Frames 0-5
        animations.put(RatAnimationType.TURN, createAnimation(textureRegions, 2, 6, 4));            // Row 2, Frames 6-8 + Row 3, Frame 0
        animations.put(RatAnimationType.ATTACK_1, createAnimation(textureRegions, 3, 1, 8));        // Row 3, Frames 1-8
        animations.put(RatAnimationType.ATTACK_2, createAnimation(textureRegions, 4, 0, 9));        // Row 4, Frames 0-8
        animations.put(RatAnimationType.HIT, createAnimation(textureRegions, 5, 0, 3));             // Row 5, Frames 0-3
        animations.put(RatAnimationType.DEATH_1, createAnimation(textureRegions, 5, 3, 18));        // Row 5, Frames 4-8 + Row 6, Frames 0-8 + Row 7, Frames 0-2
        animations.put(RatAnimationType.DEATH_2, createAnimation(textureRegions, 7, 3, 7));         // Row 7, Frames 3-8 + Row 8, Frame 0
        // Debug: Check that all animations are created
        for (RatAnimationType type : RatAnimationType.values()) {
            if (animations.get(type) == null) {
                System.err.println("Animation " + type + " is null");
            } else {
                System.out.println("Animation " + type + " created successfully");
            }
        }
    }

    private Animation<TextureRegion> createAnimation(TextureRegion[][] textureRegions, int startRow, int startCol, int count) {
        TextureRegion[] frames = new TextureRegion[count];
        int frameIndex = 0;

        for (int row = startRow; row < textureRegions.length && frameIndex < count; row++) {
            for (int col = (row == startRow ? startCol : 0); col < SPRITE_SHEET_COLUMNS && frameIndex < count; col++) {
                frames[frameIndex++] = textureRegions[row][col];
            }
        }

        if (frameIndex < count) {
            System.err.println("Error: Not enough frames extracted. Requested: " + count + ", Extracted: " + frameIndex);
            return null;
        }

        return new Animation<>(FRAME_DURATION, frames);
    }

    public TextureRegion getFrame(RatAnimationType type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
    }

    public Animation<TextureRegion> getAnimation(RatAnimationType type) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation;
    }

    public void flipFramesHorizontally() {
        for (Animation<TextureRegion> animation : animations.values()) {
            for (TextureRegion frame : animation.getKeyFrames()) {
                frame.flip(true, false);  // Always flip when this is called
            }
        }
        flipped = !flipped;
    }

    public void dispose() {
        animations.clear();
    }
}

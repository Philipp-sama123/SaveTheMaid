package krazy.cat.games.SaveTheMaid.Characters.AnimationSets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSetBat {
    private static final float FRAME_DURATION = 0.1f;
    private static final int FRAME_WIDTH = 40;
    private static final int FRAME_HEIGHT = 42;
    private static final int SPRITE_SHEET_COLUMNS = 10; // 10 frames per row

    public enum BatAnimationType {
        IDLE1, IDLE2, APPEARANCE, MOVE1, MOVE2, TURN_AROUND, DASH, GRAB, GRAB2, HIT, DEATH1, DEATH2
    }

    private final Map<BatAnimationType, Animation<TextureRegion>> animations;
    private boolean flipped = false;

    public AnimationSetBat(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        animations = new EnumMap<>(BatAnimationType.class);
        animations.put(BatAnimationType.IDLE1, createAnimation(textureRegions, 0, 0, 16));
        animations.put(BatAnimationType.APPEARANCE, createAnimation(textureRegions, 1, 6, 4));
        animations.put(BatAnimationType.MOVE1, createAnimation(textureRegions, 2, 0, 12));
        animations.put(BatAnimationType.TURN_AROUND, createAnimation(textureRegions, 3, 2, 3));
        animations.put(BatAnimationType.MOVE2, createAnimation(textureRegions, 3, 5, 7));
        animations.put(BatAnimationType.DASH, createAnimation(textureRegions, 4, 2, 15));
        animations.put(BatAnimationType.GRAB, createAnimation(textureRegions, 5, 7, 9));
        animations.put(BatAnimationType.GRAB2, createAnimation(textureRegions, 6, 6, 3));
        animations.put(BatAnimationType.HIT, createAnimation(textureRegions, 6, 9, 3));
        animations.put(BatAnimationType.DEATH1, createAnimation(textureRegions, 7, 2, 18));
        animations.put(BatAnimationType.DEATH2, createAnimation(textureRegions, 9, 0, 6));


        // Debug: Check that all animations are created
        for (BatAnimationType type : BatAnimationType.values()) {
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

    public TextureRegion getFrame(BatAnimationType type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
    }

    public Animation<TextureRegion> getAnimation(BatAnimationType type) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation;
    }

    public void flipFramesHorizontally() {
        for (Animation<TextureRegion> animation : animations.values()) {
            for (TextureRegion frame : animation.getKeyFrames()) {
                frame.flip(true, false);
            }
        }
        flipped = !flipped;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void dispose() {
        // Dispose resources if necessary
    }
}

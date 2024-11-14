package krazy.cat.games.SaveTheMaid.Characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSetMaid implements AnimationSet {
    private static final float FRAME_DURATION = 0.1f;
    public static final int FRAME_WIDTH = 64;
    public static final int FRAME_HEIGHT = 64;
    private static final int SPRITE_SHEET_COLUMNS = 9; // 10 frames per row

    public enum MaidAnimationType {
        IDLE, WALK, CARRY, CARRY_FULL, RUN, CARRY_IDLE, SIT, JUMP, LAY, DIE,
    }

    private final Map<MaidAnimationType, Animation<TextureRegion>> animations;
    private boolean flipped = false;

    public AnimationSetMaid(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        animations = new EnumMap<>(MaidAnimationType.class);
        animations.put(MaidAnimationType.WALK, createAnimation(textureRegions, 0, 0, 8));             // Row 0, Frames 0-7
        animations.put(MaidAnimationType.CARRY, createAnimation(textureRegions, 0, 8, 8));          // Row 0, Frame 8 + Row 1, Frames 0-6
        animations.put(MaidAnimationType.CARRY_FULL, createAnimation(textureRegions, 1, 6, 8));          // Row 1, Frames 7-8 + Row 2, Frames 0-5
        animations.put(MaidAnimationType.RUN, createAnimation(textureRegions, 2, 4, 7));            // Row 2, Frames 6-8 + Row 3, Frame 0
        animations.put(MaidAnimationType.IDLE, createAnimation(textureRegions, 3, 2, 5));        // Row 3, Frames 1-8
        animations.put(MaidAnimationType.CARRY_IDLE, createAnimation(textureRegions, 3, 7, 5));        // Row 4, Frames 0-8
        animations.put(MaidAnimationType.SIT, createAnimation(textureRegions, 4, 7, 6));             // Row 5, Frames 0-3
        animations.put(MaidAnimationType.JUMP, createAnimation(textureRegions, 5, 3, 6));        // Row 5, Frames 4-8 + Row 6, Frames 0-8 + Row 7, Frames 0-2
        animations.put(MaidAnimationType.LAY, createAnimation(textureRegions, 5, 9, 13));         // Row 7, Frames 3-8 + Row 8, Frame 0
        animations.put(MaidAnimationType.DIE, createAnimation(textureRegions, 7, 3, 11));         // Row 7, Frames 3-8 + Row 8, Frame 0
        // Debug: Check that all animations are created
        for (MaidAnimationType type : MaidAnimationType.values()) {
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

    @Override
    public TextureRegion getFrame(Enum type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
    }

    @Override
    public Animation<TextureRegion> getAnimation(Enum type) {
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

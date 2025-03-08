package krazy.cat.games.SaveTheMaid.Characters.AnimationSets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSetDamned implements AnimationSet<AnimationSetDamned.DamnedAnimationType> {
    private static final float FRAME_DURATION = 0.1f;
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int SPRITE_SHEET_COLUMNS = 10; // 10 frames per row

    public enum DamnedAnimationType {
        IDLE, WALK, ATTACK, EAT_PREY, WALK_ATTACK, HIT, DEATH, CRAWL_IDLE, CRAWL, CRAWL_EAT_PREY, CRAWL_DEATH
    }

    private final Map<DamnedAnimationType, Animation<TextureRegion>> animations;
    private boolean flipped = false;

    public AnimationSetDamned(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        animations = new EnumMap<>(DamnedAnimationType.class);

        animations.put(DamnedAnimationType.IDLE, createAnimation(textureRegions, 0, 0, 5));
        animations.put(DamnedAnimationType.WALK, createAnimation(textureRegions, 1, 0, 8));
        animations.put(DamnedAnimationType.ATTACK, createAnimation(textureRegions, 2, 0, 6));
        animations.put(DamnedAnimationType.DEATH, createAnimation(textureRegions, 3, 0, 6));

        // Debug: Check that all animations are created
        for (DamnedAnimationType type : DamnedAnimationType.values()) {
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

    public TextureRegion getFrame(DamnedAnimationType type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
    }

    public Animation<TextureRegion> getAnimation(DamnedAnimationType type) {
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

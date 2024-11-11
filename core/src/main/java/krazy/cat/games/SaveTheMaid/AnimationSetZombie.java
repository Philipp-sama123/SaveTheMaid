package krazy.cat.games.SaveTheMaid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSetZombie {
    private static final float FRAME_DURATION = 0.1f;
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int SPRITE_SHEET_COLUMNS = 10; // 10 frames per row

    public enum ZombieAnimationType {
        IDLE, WALK, ATTACK, EAT_PREY, WALK_ATTACK, HIT, DEATH, CRAWL_IDLE, CRAWL, CRAWL_EAT_PREY, CRAWL_DEATH
    }

    private final Map<ZombieAnimationType, Animation<TextureRegion>> animations;
    private boolean flipped = false;

    public AnimationSetZombie(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        animations = new EnumMap<>(ZombieAnimationType.class);

        animations.put(ZombieAnimationType.IDLE, createAnimation(textureRegions, 0, 0, 7));
        animations.put(ZombieAnimationType.WALK, createAnimation(textureRegions, 0, 7, 8));
        animations.put(ZombieAnimationType.ATTACK, createAnimation(textureRegions, 1, 5, 6));
        animations.put(ZombieAnimationType.EAT_PREY, createAnimation(textureRegions, 2, 1, 5));
        animations.put(ZombieAnimationType.WALK_ATTACK, createAnimation(textureRegions, 2, 6, 8));
        animations.put(ZombieAnimationType.HIT, createAnimation(textureRegions, 3, 6, 3)); // actually it starts at 4 but the effect should start immediately and its 5 frames long actually
        animations.put(ZombieAnimationType.DEATH, createAnimation(textureRegions, 4, 0, 14));
        animations.put(ZombieAnimationType.CRAWL_IDLE, createAnimation(textureRegions, 5, 4, 6));
        animations.put(ZombieAnimationType.CRAWL, createAnimation(textureRegions, 6, 0, 6));
        animations.put(ZombieAnimationType.CRAWL_EAT_PREY, createAnimation(textureRegions, 6, 6, 5));
        animations.put(ZombieAnimationType.CRAWL_DEATH, createAnimation(textureRegions, 7, 1, 7));

        // Debug: Check that all animations are created
        for (ZombieAnimationType type : ZombieAnimationType.values()) {
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

    public TextureRegion getFrame(ZombieAnimationType type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
    }

    public Animation<TextureRegion> getAnimation(ZombieAnimationType type) {
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

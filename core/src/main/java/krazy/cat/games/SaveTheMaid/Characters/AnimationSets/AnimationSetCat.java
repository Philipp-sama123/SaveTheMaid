package krazy.cat.games.SaveTheMaid.Characters.AnimationSets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSetCat implements AnimationSet<AnimationSetCat.CatAnimationType> {
    private static final float FRAME_DURATION = .25f;
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;
    private static final int SPRITE_SHEET_COLUMNS = 4;

    @Override
    public TextureRegion getFrame(CatAnimationType type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
    }

    private final Map<AnimationSetCat.CatAnimationType, Animation<TextureRegion>> animations;

    public AnimationSetCat(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        animations = new EnumMap<>(CatAnimationType.class);
        animations.put(CatAnimationType.APPEAR, createAnimation(textureRegions, 0, 0, 3));
        animations.put(CatAnimationType.WALK_LEFT, createAnimation(textureRegions, 1, 0, 3));
        animations.put(CatAnimationType.WALK_RIGHT, createAnimation(textureRegions, 2, 0, 3));
        animations.put(CatAnimationType.DISAPPEAR, createAnimation(textureRegions, 3, 0, 3));

        // Debug: Check that all animations are created
        for (AnimationSetCat.CatAnimationType type : AnimationSetCat.CatAnimationType.values()) {
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
    public Animation<TextureRegion> getAnimation(CatAnimationType type) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation;
    }


    @Override
    public void flipFramesHorizontally() {

    }

    @Override
    public void dispose() {

    }

    public enum CatAnimationType {
        APPEAR, WALK_LEFT, WALK_RIGHT, DISAPPEAR
    }

}

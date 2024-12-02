package krazy.cat.games.SaveTheMaid.Characters.AnimationSets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSetFemaleAgent {
    private static final float FRAME_DURATION = 0.1f;
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;

    public enum AnimationType {
        IDLE, WALK, RUN, FALL_AIM, IDLE_RELOAD, STAND_AIM, STAND_SHOOT, STAND_AIM_UP, STAND_SHOOT_UP, CROUCH_IDLE, CROUCH_AIM, CROUCH_SHOOT, CROUCH_SHOOT_UP, CROUCH_AIM_UP, WALK_AIM, WALK_SHOOT, WALK_AIM_UP, WALK_SHOOT_UP, RUN_SHOOT_UP, RUN_AIM_UP, RUN_SHOOT, RUN_AIM, JUMP, JUMP_SHOOT, FALL, FALL_SHOOT, SLIDE, SLIDE_SHOOT, IDLE_CHARISMATIC, IDLE_BACK_VIEW, DEATH, JUMP_AIM
    }

    private final Map<AnimationType, Animation<TextureRegion>> animations;
    private boolean isFlipped = false;

    public AnimationSetFemaleAgent(Texture upperBodySpriteSheet) {
        animations = createAnimations(upperBodySpriteSheet);
    }

    private Map<AnimationType, Animation<TextureRegion>> createAnimations(Texture spriteSheet) {
        final Map<AnimationType, Animation<TextureRegion>> upperBodyAnimations;
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        upperBodyAnimations = new EnumMap<>(AnimationType.class);

        upperBodyAnimations.put(AnimationType.IDLE, createAnimation(textureRegions, 0, 5));
        upperBodyAnimations.put(AnimationType.IDLE_RELOAD, createAnimation(textureRegions, 1, 10));
        upperBodyAnimations.put(AnimationType.STAND_AIM, createAnimation(textureRegions, 2, 5));
        upperBodyAnimations.put(AnimationType.STAND_SHOOT, createAnimation(textureRegions, 3, 5));
        upperBodyAnimations.put(AnimationType.STAND_AIM_UP, createAnimation(textureRegions, 4, 5));
        upperBodyAnimations.put(AnimationType.STAND_SHOOT_UP, createAnimation(textureRegions, 5, 5));
        upperBodyAnimations.put(AnimationType.CROUCH_IDLE, createAnimation(textureRegions, 6, 5));
        upperBodyAnimations.put(AnimationType.CROUCH_AIM, createAnimation(textureRegions, 7, 5));
        upperBodyAnimations.put(AnimationType.CROUCH_SHOOT, createAnimation(textureRegions, 8, 5));
        upperBodyAnimations.put(AnimationType.CROUCH_AIM_UP, createAnimation(textureRegions, 9, 5));
        upperBodyAnimations.put(AnimationType.CROUCH_SHOOT_UP, createAnimation(textureRegions, 10, 5));
        upperBodyAnimations.put(AnimationType.WALK, createAnimation(textureRegions, 11, 8));
        upperBodyAnimations.put(AnimationType.WALK_AIM, createAnimation(textureRegions, 12, 8));
        upperBodyAnimations.put(AnimationType.WALK_SHOOT, createAnimation(textureRegions, 13, 8));
        upperBodyAnimations.put(AnimationType.WALK_AIM_UP, createAnimation(textureRegions, 14, 8));
        upperBodyAnimations.put(AnimationType.WALK_SHOOT_UP, createAnimation(textureRegions, 15, 8));
        upperBodyAnimations.put(AnimationType.RUN, createAnimation(textureRegions, 16, 8));
        upperBodyAnimations.put(AnimationType.RUN_AIM, createAnimation(textureRegions, 17, 8));
        upperBodyAnimations.put(AnimationType.RUN_SHOOT, createAnimation(textureRegions, 18, 8));
        upperBodyAnimations.put(AnimationType.RUN_AIM_UP, createAnimation(textureRegions, 19, 8));
        upperBodyAnimations.put(AnimationType.RUN_SHOOT_UP, createAnimation(textureRegions, 20, 8));
        upperBodyAnimations.put(AnimationType.JUMP, createAnimation(textureRegions, 21, 5));
        upperBodyAnimations.put(AnimationType.JUMP_AIM, createAnimation(textureRegions, 22, 5));
        upperBodyAnimations.put(AnimationType.JUMP_SHOOT, createAnimation(textureRegions, 23, 5));
        upperBodyAnimations.put(AnimationType.FALL, createAnimation(textureRegions, 24, 5));
        upperBodyAnimations.put(AnimationType.FALL_AIM, createAnimation(textureRegions, 25, 5));
        upperBodyAnimations.put(AnimationType.FALL_SHOOT, createAnimation(textureRegions, 26, 5));
        upperBodyAnimations.put(AnimationType.SLIDE, createAnimation(textureRegions, 27, 5));
        upperBodyAnimations.put(AnimationType.SLIDE_SHOOT, createAnimation(textureRegions, 28, 5));
        upperBodyAnimations.put(AnimationType.IDLE_CHARISMATIC, createAnimation(textureRegions, 29, 5));
        upperBodyAnimations.put(AnimationType.IDLE_BACK_VIEW, createAnimation(textureRegions, 30, 5));
        upperBodyAnimations.put(AnimationType.DEATH, createAnimation(textureRegions, 31, 12));

        // Debug: Check that all animations are created
        for (AnimationType type : AnimationType.values()) {
            if (upperBodyAnimations.get(type) == null) {
                System.err.println("Animation " + type + " is null");
            } else {
                System.out.println("Animation " + type + " created successfully");
            }
        }
        return upperBodyAnimations;
    }

    private Animation<TextureRegion> createAnimation(TextureRegion[][] textureRegions, int row, int count) {
        if (row >= textureRegions.length || count > textureRegions[row].length) {
            System.err.println("Error: Frame extraction out of bounds. Row: " + row + ", Count: " + count);
            return null;
        }
        TextureRegion[] frames = new TextureRegion[count];
        System.arraycopy(textureRegions[row], 0, frames, 0, count);
        return new Animation<>(FRAME_DURATION, frames);
    }

    public TextureRegion getCurrentFrame(AnimationType type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
    }

    public Animation<TextureRegion> getCurrentFrame(AnimationType type) {
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
        isFlipped = !isFlipped;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void dispose() {
        // Dispose resources if necessary
    }
}

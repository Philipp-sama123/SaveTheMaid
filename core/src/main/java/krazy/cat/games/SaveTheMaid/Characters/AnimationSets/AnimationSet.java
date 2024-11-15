package krazy.cat.games.SaveTheMaid.Characters.AnimationSets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface AnimationSet<T extends Enum<T>> {
    TextureRegion getFrame(T animationType, float stateTime, boolean looping);

    Animation<TextureRegion> getAnimation(T animationType);

    void flipFramesHorizontally();

    void dispose();
}

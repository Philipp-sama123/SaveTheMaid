package krazy.cat.games.SaveTheMaid.Tools;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Utils {

    public static Animation<TextureRegion> createAnimation(Texture spriteSheet, int frameWidth, int frameHeight, int frameCount, float frameDuration) {
        TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet, frameWidth, frameHeight);
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            frames.add(tmpFrames[0][i]);  // Assuming single row
        }
        return new Animation<>(frameDuration, frames, Animation.PlayMode.NORMAL);
    }
}

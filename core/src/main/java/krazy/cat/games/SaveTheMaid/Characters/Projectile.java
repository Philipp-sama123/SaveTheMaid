package krazy.cat.games.SaveTheMaid.Characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Projectile extends ProjectileBase {
    /**
     * Projectile uses 16x12 frames for the projectile.
     * In this example, we assume an explosion animation with 6 frames from row 0,
     * and we use a collider size of 6x3 pixels for the projectile.
     * Adjust these numbers as needed.
     */
    public Projectile(World world, Vector2 position, Vector2 velocity,
                      Texture texture, Texture explosionTexture) {
        super(
            world, position,
            velocity, texture,
            0,
            16,
            12,
            6,
            3,
            false,
            explosionTexture,
            8,
            6,
            32,
            32
        );
    }
}

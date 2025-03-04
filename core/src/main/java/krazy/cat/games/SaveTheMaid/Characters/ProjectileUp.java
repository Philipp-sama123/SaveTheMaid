package krazy.cat.games.SaveTheMaid.Characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class ProjectileUp extends ProjectileBase {
    /**
     * ProjectileUp uses 24x24 frames for the projectile and 32x32 frames for the explosion.
     * The projectile collider is defined as 8 (width) x 4 (height) pixels.
     * The explosion animation uses 6 frames from row index 29.
     */
    public ProjectileUp(World world, Vector2 position, Vector2 velocity,
                        Texture projectileTexture, int projectileRow,
                        Texture explosionTexture) {
        super(
            world, position, velocity,
            projectileTexture, projectileRow,
            24,
            24,
            8,
            4,
            explosionTexture,
            29,
            6,
            32,
            32
        );
    }
}

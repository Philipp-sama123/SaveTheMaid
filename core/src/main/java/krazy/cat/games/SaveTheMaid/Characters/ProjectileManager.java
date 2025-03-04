package krazy.cat.games.SaveTheMaid.Characters;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class ProjectileManager {
    private final World world;

    private static final float PROJECTILE_VELOCITY_X = 2.0f;
    private static final float PROJECTILE_VELOCITY_Y = 1.5f;

    private Array<Projectile> projectiles;
    private Array<ProjectileUp> projectilesUp;
    private Texture projectileTexture;
    private Texture projectileUpTexture;
    private Texture explosionProjectileUpTexture;

    public ProjectileManager(World world) {
        projectileTexture = GameAssetManager.getInstance().get(AssetPaths.AGENT_PIXEL_BULLET_TEXTURE, Texture.class);
        projectileUpTexture = GameAssetManager.getInstance().get(AssetPaths.AGENT_PIXEL_SHOOT_UP_TEXTURE, Texture.class);
        explosionProjectileUpTexture = GameAssetManager.getInstance().get(AssetPaths.AGENT_PIXEL_SHOOT_UP_EXPLOSION_TEXTURE, Texture.class);
        projectiles = new Array<>();
        projectilesUp = new Array<>();
        this.world = world;
    }


    private void addProjectile(Vector2 position, Vector2 velocity) {
        projectiles.add(new Projectile(world, position, velocity, projectileTexture));
    }

    private void addProjectileUp(Vector2 position, Vector2 velocity) {
        projectilesUp.add(new ProjectileUp(world, position, velocity, projectileUpTexture, 0,explosionProjectileUpTexture ));
    }

    public void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);
            if (projectile.isDestroyed()) {
                projectiles.removeIndex(i);
            }
        }
        for (int i = projectilesUp.size - 1; i >= 0; i--) {
            ProjectileUp projectileUp = projectilesUp.get(i);
            projectileUp.update(delta);
            if (projectileUp.isDestroyed()) {
                projectilesUp.removeIndex(i);
            }
        }
    }

    public void addShootUpProjectile(Body body) {
        Vector2 position = body.getPosition().cpy().add(0, 40 / PPM);
        Vector2 velocity = new Vector2(0, PROJECTILE_VELOCITY_Y);
        addProjectileUp(position, velocity);
    }

    public void addShootProjectile(Body body, boolean isFacingRight, boolean isCrouching, boolean isSliding) {
        Vector2 position = body.getPosition().cpy().add(
            isFacingRight ? 20 / PPM : -20 / PPM,
            isCrouching ? 2 / PPM : 10 / PPM);
        if (isSliding) {
            position.y -= 16 / PPM;
            position.x += isFacingRight ? 16 / PPM : -16 / PPM;
        }
        Vector2 velocity = new Vector2(isFacingRight ? PROJECTILE_VELOCITY_X : -PROJECTILE_VELOCITY_X, 0);
        addProjectile(position, velocity);
    }


    public void drawProjectiles(Batch batch) {
        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }
        for (ProjectileUp projectileUp : projectilesUp) {
            projectileUp.draw(batch);
        }
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }
}

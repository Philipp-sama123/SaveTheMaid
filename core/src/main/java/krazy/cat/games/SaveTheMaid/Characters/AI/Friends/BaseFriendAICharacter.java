package krazy.cat.games.SaveTheMaid.Characters.AI.Friends;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_GROUND_ONLY;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Characters.Player;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;


public abstract class BaseFriendAICharacter<T extends Enum<T>> {
    protected static final float MOVEMENT_SPEED = 15f / PPM;

    public Sound ATTACK_SOUND = GameAssetManager.getInstance().get(AssetPaths.SWIPE_SOUND, Sound.class);
    public Sound HIT_SOUND = GameAssetManager.getInstance().get(AssetPaths.PLAYER_HIT_SOUND, Sound.class);
    public Sound DEATH_SOUND = GameAssetManager.getInstance().get(AssetPaths.ZOMBIE_ATTACK_SOUND, Sound.class);

    public int health = 100;
    public int currentDamage = 20;

    protected World world;
    protected float stateTime;

    protected Body body;

    protected boolean isFacingLeft = false;
    public boolean isDestroyed = false;
    protected boolean isActive = false;
    protected Player playerReference;

    public BaseFriendAICharacter(World world, Vector2 position) {
        this.world = world;
        this.stateTime = 0f;

        defineFriend(position);
    }

    public void disableCollision() {
        body.getFixtureList().forEach(fixture -> {
            Filter filter = fixture.getFilterData();
            filter.maskBits = MASK_GROUND_ONLY; // Set mask to none
            fixture.setFilterData(filter);
        });
    }

    public boolean isPlayerInRange(Vector2 playerPosition) {
        return body.getPosition().dst(playerPosition) < 500;
    }

    protected abstract void defineFriend(Vector2 position);

    public abstract void setAnimation(T animationType);

    public abstract void jump();

    public abstract void slide();

    public abstract void moveToPlayer(Vector2 playerPosition);

    public abstract boolean isDeathAnimationComplete();

    public abstract void update(float dt, Vector2 position);

    public abstract void draw(Batch batch);

    public void dispose() {
        if (world != null && body != null) {
            world.destroyBody(body);
            body = null;
        }
    }

    public void activate(Player player) {
        isActive = true;
        playerReference = player;
    }

}

package krazy.cat.games.SaveTheMaid.Characters.AI.Friends;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_GROUND_ONLY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.compression.lzma.Base;

import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetCat;
import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetCat;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class CatCharacter extends BaseFriendAICharacter<AnimationSetCat.CatAnimationType> {
    private final AnimationSetCat animationSet;

    private AnimationSetCat.CatAnimationType currentState;
    private AnimationSetCat.CatAnimationType previousState;

    private static final float MOVEMENT_SPEED = .25f; // Adjust speed as necessary

    public CatCharacter(World world, Vector2 position) {
        super(world, position);

        this.currentState = AnimationSetCat.CatAnimationType.WALK_LEFT;

        Texture spriteSheet = GameAssetManager.getInstance().get(AssetPaths.CAT_TEXTURE_2, Texture.class);
        this.animationSet = new AnimationSetCat(spriteSheet);
    }

    public void update(float dt, Vector2 playerPosition) {
        if (isDestroyed && !isDeathAnimationComplete()) {
            disableCollision();
            return;
        }
        if (isDestroyed && isDeathAnimationComplete()) {
            dispose();
            return;
        }

        stateTime += dt;

        // Follow the player
        moveToPlayer(playerPosition);
    }

    @Override
    public void draw(Batch batch) {
        if (isDestroyed && isDeathAnimationComplete()) return;

        boolean looping = currentState != AnimationSetCat.CatAnimationType.DISAPPEAR;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        batch.draw(currentFrame,
            body.getPosition().x - (float) currentFrame.getRegionWidth() / 2 / PPM,
            body.getPosition().y - (float) currentFrame.getRegionHeight() / 2 / PPM,
            currentFrame.getRegionWidth() / PPM,
            currentFrame.getRegionHeight() / PPM);
    }

    private void adjustFacingDirection() {
    }

    @Override
    protected void defineFriend(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8 / PPM, 10f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        fixtureDef.filter.categoryBits = CATEGORY_ENEMY;
        fixtureDef.filter.maskBits = MASK_ENEMY;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void setAnimation(AnimationSetCat.CatAnimationType type) {
        if (currentState != type) {
            currentState = type;
            stateTime = 0; // Reset state time when changing animation
        }
    }

    @Override
    public void moveToPlayer(Vector2 playerPosition) {
        // Calculate the direction vector to the player
        Vector2 direction = playerPosition.cpy().sub(body.getPosition());

        // If the cat is close to the player, stop movement and pause the animation
        if (direction.len() < 0.1f) { // Adjust the threshold as needed
            body.setLinearVelocity(0, 0);
            setAnimation(AnimationSetCat.CatAnimationType.APPEAR); // Idle or appear animation
            return;
        }

        // Normalize the direction and apply movement speed
        direction.nor().scl(MOVEMENT_SPEED);

        body.setLinearVelocity(direction.x, body.getLinearVelocity().y);

        // Adjust animation and facing direction based on movement
        if (direction.x > 0) {
            // Moving right
            setAnimation(AnimationSetCat.CatAnimationType.WALK_RIGHT);
        } else {
            // Moving left
            setAnimation(AnimationSetCat.CatAnimationType.WALK_LEFT);
        }
    }

    @Override
    public boolean isDeathAnimationComplete() {
        return animationSet.getAnimation(AnimationSetCat.CatAnimationType.DISAPPEAR).isAnimationFinished(stateTime);
    }
}

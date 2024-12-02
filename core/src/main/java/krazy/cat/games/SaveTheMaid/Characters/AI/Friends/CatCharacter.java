package krazy.cat.games.SaveTheMaid.Characters.AI.Friends;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_CAT;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_CAT;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_PROJECTILE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSet;
import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetCat;
import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetFemaleAgent;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class CatCharacter extends BaseFriendAICharacter<AnimationSetCat.CatAnimationType> {
    private final AnimationSetCat animationSet;
    private AnimationSetCat.CatAnimationType currentState;
    private AnimationSetCat.CatAnimationType previousState;
    private float slidingTimer = 0f;
    private static final float SLIDING_DURATION = 1.5f; // Sliding duration in seconds

    private float SLIDE_IMPULSE = 1.5f;
    private boolean isSliding = false;
    private boolean markedForDisposal = false;

    private static final float MOVEMENT_SPEED = .25f; // Adjust speed as necessary

    public CatCharacter(World world, Vector2 position) {
        super(world, position);

        this.currentState = AnimationSetCat.CatAnimationType.APPEAR;

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

        if (markedForDisposal && isDeathAnimationComplete()) {
            dispose();
            return;
        }

        stateTime += dt;

        if (isSliding) {
            slidingTimer -= dt; // Decrease the timer by delta time
            if (slidingTimer <= 0) {
                isSliding = false; // Exit sliding state
            }
        }
        if (!markedForDisposal) {
            // Follow the player
            moveToPlayer(playerPosition);
        }
    }

    public void disappear() {
        if (!markedForDisposal) {
            setAnimation(AnimationSetCat.CatAnimationType.DISAPPEAR);
            markedForDisposal = true;
            disableCollision();
        }
        playerReference.removeFriend();
    }

    @Override
    public void draw(Batch batch) {
        if ((isDestroyed || markedForDisposal) && isDeathAnimationComplete()) return;

        boolean looping = currentState != AnimationSetCat.CatAnimationType.DISAPPEAR;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        batch.draw(currentFrame,
            body.getPosition().x - (float) currentFrame.getRegionWidth() / 2 / PPM,
            body.getPosition().y - (float) currentFrame.getRegionHeight() / 2 / PPM,
            currentFrame.getRegionWidth() / PPM,
            currentFrame.getRegionHeight() / PPM);
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

        // Sensor fixture for interactions
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(8 / PPM, 10f / PPM);

        FixtureDef sensorFixtureDef = new FixtureDef();
        sensorFixtureDef.shape = sensorShape;
        sensorFixtureDef.isSensor = true;
        sensorFixtureDef.filter.categoryBits = CATEGORY_CAT;
        sensorFixtureDef.filter.maskBits = MASK_CAT;
        body.createFixture(sensorFixtureDef).setUserData(this);
        sensorShape.dispose();
    }

    @Override
    public void setAnimation(AnimationSetCat.CatAnimationType type) {
        if (currentState != type) {
            currentState = type;
            stateTime = 0; // Reset state time when changing animation
        }
    }

    @Override
    public void jump() {
        body.setLinearVelocity(body.getLinearVelocity().x, 1.5f); // Scaled jump velocity
        stateTime = 0;
    }

    @Override
    public void slide() {
        if (!isSliding) {
            isSliding = true; // Enter sliding state
            float currentVelocityX = body.getLinearVelocity().x;
            float slideImpulseX;

            if (currentVelocityX < 0 || currentState == AnimationSetCat.CatAnimationType.WALK_LEFT) {
                slideImpulseX = -SLIDE_IMPULSE; // Slide to the left
            } else {
                slideImpulseX = SLIDE_IMPULSE; // Slide to the right
            }

            // Apply the sliding impulse
            body.applyLinearImpulse(new Vector2(slideImpulseX, 0), body.getWorldCenter(), true);
            stateTime = 0;
            slidingTimer = SLIDING_DURATION;
        }
    }

    @Override
    public void moveToPlayer(Vector2 playerPosition) {
        if (isActive && !isSliding) {
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

            if (direction.x > 0) {
                setAnimation(AnimationSetCat.CatAnimationType.WALK_RIGHT);
            } else {
                setAnimation(AnimationSetCat.CatAnimationType.WALK_LEFT);
            }
        }
    }

    @Override
    public boolean isDeathAnimationComplete() {
        return animationSet.getAnimation(AnimationSetCat.CatAnimationType.DISAPPEAR).isAnimationFinished(stateTime);
    }
}

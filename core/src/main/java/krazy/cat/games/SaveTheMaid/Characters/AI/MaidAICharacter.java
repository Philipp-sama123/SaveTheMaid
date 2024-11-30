package krazy.cat.games.SaveTheMaid.Characters.AI;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.MASK_ENEMY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import krazy.cat.games.SaveTheMaid.Characters.AnimationSets.AnimationSetMaid;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class MaidAICharacter extends BaseAICharacter<AnimationSetMaid.MaidAnimationType> {
    private final AnimationSetMaid animationSet;
    private static final float WALK_SPEED = 5f;

    private AnimationSetMaid.MaidAnimationType currentState;
    private AnimationSetMaid.MaidAnimationType previousState;

    private Texture attackBarTexture;
    private float saveProgress = 0f; // Ranges from 0 to 1
    private final float attackBarYOffset = 30f; // Adjust to position the bar above the character
    private final float attackCooldownTime = 3f; // Time to refill attack bar (seconds)
    private float attackCooldownTimer = 0f;

    public MaidAICharacter(World world, Vector2 position) {
        super(world, position);
        this.currentState = AnimationSetMaid.MaidAnimationType.IDLE;

        // Load Maid sprite and animations
        Texture spriteSheet = GameAssetManager.getInstance().get(AssetPaths.MAID_CHARACTER_BLACK, Texture.class);
        this.animationSet = new AnimationSetMaid(spriteSheet);

        // Load the attack bar texture
        attackBarTexture = GameAssetManager.getInstance().get(AssetPaths.HEALTH_BAR_SIMPLE, Texture.class);

        isFacingLeft = true;
    }

    @Override
    public boolean canAttack() {
        return true;// attackProgress >= 1f; // Maid can attack when progress is full
    }

    @Override
    public void update(float dt, Vector2 playerPosition) {
        if (isDestroyed) {
            disableCollision();
            return;
        }

        stateTime += dt;
        stateMachine.update(dt, playerPosition);
        adjustFacingDirection();
    }

    @Override
    public void draw(Batch batch) {
        if (isDestroyed && isDeathAnimationComplete()) return;

        boolean looping = currentState != AnimationSetMaid.MaidAnimationType.DIE;
        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, looping);

        drawHealthBar(batch);
        drawSaveBar(batch); // Draw the attack progress bar

        batch.draw(
            currentFrame,
            body.getPosition().x - 32 / PPM,
            body.getPosition().y - 20 / PPM,
            64 / PPM,
            64 / PPM
        );
    }

    @Override
    protected void defineEnemy(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f / PPM, 20f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        fixtureDef.filter.categoryBits = CATEGORY_ENEMY;
        fixtureDef.filter.maskBits = MASK_ENEMY;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    private void drawSaveBar(Batch batch) {
        float barWidth = attackBarTexture.getWidth() / PPM * saveProgress; // Scale width by progress
        batch.setColor(1, 1, 1, 0.75f); // Add transparency to the bar
        batch.draw(
            attackBarTexture,
            body.getPosition().x - (attackBarTexture.getWidth() / (2 * PPM)),
            body.getPosition().y + attackBarYOffset / PPM,
            barWidth,
            attackBarTexture.getHeight() / PPM
        );
        batch.setColor(1, 1, 1, 1); // Reset color
    }


    @Override
    public void moveToPlayer(Vector2 playerPosition) {
        Vector2 direction = playerPosition.cpy().sub(body.getPosition()).nor();
        direction.scl(MOVEMENT_SPEED);

        body.setLinearVelocity(direction.x, body.getLinearVelocity().y);
        currentState = AnimationSetMaid.MaidAnimationType.RUN;
    }

    @Override
    public void attack() {
        if (!canAttack()) return;

        setAnimation(AnimationSetMaid.MaidAnimationType.LAY);
        body.setLinearVelocity(0, 0);

        // Reset attack progress and start cooldown
        saveProgress += .10f;
    }

    @Override
    public void chase() {
        setAnimation(AnimationSetMaid.MaidAnimationType.RUN);
    }

    @Override
    public void die() {
        setAnimation(AnimationSetMaid.MaidAnimationType.DIE);
        disableCollision();
    }

    @Override
    public void hit() {
        Gdx.app.log("MAID", "TODO Implement hit behavior");
    }

    @Override
    public void idle() {
        setAnimation(AnimationSetMaid.MaidAnimationType.IDLE);
        body.setLinearVelocity(0, 0);
    }

    private void adjustFacingDirection() {
        float velocityX = body.getLinearVelocity().x;

        if (velocityX < 0 && !isFacingLeft) {
            animationSet.flipFramesHorizontally();
            isFacingLeft = true;
        } else if (velocityX > 0 && isFacingLeft) {
            animationSet.flipFramesHorizontally();
            isFacingLeft = false;
        }
    }

    @Override
    public void updateAttackColliderPosition() {
        Gdx.app.log("MAID", "NOT USED! updateAttackColliderPosition");
    }

    @Override
    public void setAnimation(AnimationSetMaid.MaidAnimationType animationType) {
        currentState = animationType;
        stateTime = 0;
    }

    @Override
    public boolean isDeathAnimationComplete() {
        return animationSet.getAnimation(AnimationSetMaid.MaidAnimationType.DIE).isAnimationFinished(stateTime);
    }

    @Override
    public boolean isAttackAnimationFinished() {
        return animationSet.getAnimation(AnimationSetMaid.MaidAnimationType.LAY).isAnimationFinished(stateTime);
    }

    @Override
    public boolean isHitAnimationFinished() {
        return true;
    }
}

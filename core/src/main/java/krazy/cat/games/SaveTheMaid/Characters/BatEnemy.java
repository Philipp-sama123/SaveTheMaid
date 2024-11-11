package krazy.cat.games.SaveTheMaid.Characters;

import krazy.cat.games.SaveTheMaid.AnimationSetBat;

public class BatEnemy {
    private AnimationSetBat animationSet;
    private AnimationSetBat.BatAnimationType currentState;
//
//    public EnemyBat(World world, Vector2 position) {
//        super(world);
//        this.currentState = AnimationSetBat.BatAnimationType.IDLE1;
//        this.animationSet = new AnimationSetBat(new Texture("Characters/Bat/Bat_v1/Sprite Sheet/Bat_v1_Sheet.png"));
//        defineEnemy(position);
//        stateMachine.changeState(new IdleState());
//    }
//
//    private void defineEnemy(Vector2 position) {
//        BodyDef bodyDef = new BodyDef();
//        bodyDef.position.set(position);
//        bodyDef.type = BodyDef.BodyType.DynamicBody;
//        body = world.createBody(bodyDef);
//
//        PolygonShape shape = new PolygonShape();
//        shape.setAsBox(8f, 20f);
//
//        FixtureDef fixtureDef = new FixtureDef();
//        fixtureDef.shape = shape;
//        fixtureDef.filter.categoryBits = CATEGORY_ENEMY;
//        fixtureDef.filter.maskBits = MASK_ENEMY;
//        body.createFixture(fixtureDef).setUserData(this);
//        shape.dispose();
//    }
//
//    @Override
//    protected void flipDirection() {
//        animationSet.flipFramesHorizontally();
//    }
//
//    @Override
//    public void draw(Batch batch) {
//        if (isDestroyed && isDeathAnimationComplete()) return;
//
//        TextureRegion currentFrame = animationSet.getFrame(currentState, stateTime, currentState != AnimationSetBat.BatAnimationType.DEATH1);
//        batch.draw(currentFrame, body.getPosition().x - 32, body.getPosition().y - 20, 64, 64);
//    }
//
//    @Override
//    public boolean isDeathAnimationComplete() {
//        return animationSet.getAnimation(AnimationSetBat.BatAnimationType.DEATH1).isAnimationFinished(stateTime);
//    }
}

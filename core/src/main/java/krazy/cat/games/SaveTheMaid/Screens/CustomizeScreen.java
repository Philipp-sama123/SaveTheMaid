package krazy.cat.games.SaveTheMaid.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import krazy.cat.games.SaveTheMaid.Characters.Player; // Import Player class
import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class CustomizeScreen implements Screen {
    private final SaveTheMaidGame game;
    private final Stage stage;
    private final Player player; // Player instance
    private final SpriteBatch batch; // SpriteBatch for rendering
    private final World world; // World instance for physics
    private Body groundBody; // Ground body

    public CustomizeScreen(SaveTheMaidGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        batch = game.batch; // Use the game's batch
        world = new World(new Vector2(0, -9.8f), true); // Create a dummy world with gravity
        player = new Player(world); // Create player instance with the dummy world
        createGround(); // Create ground for the player to stand on
    }

    private void createGround() {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(400, 50); // Set position of the ground
        groundBodyDef.type = BodyDef.BodyType.StaticBody; // Set body type as static
        groundBody = world.createBody(groundBodyDef);

        // Create a box shape for the ground
        FixtureDef groundFixtureDef = new FixtureDef();
        groundFixtureDef.shape = new PolygonShape();
        ((PolygonShape) groundFixtureDef.shape).setAsBox(800, 10); // Width, Height
        groundBody.createFixture(groundFixtureDef);
        groundFixtureDef.shape.dispose(); // Dispose shape to free memory
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        setupUI();
    }

    private void setupUI() {
        // Back button with a visual style
        Texture backTexture = new Texture(Gdx.files.internal("UiSprites/Buttons/Shooting.png"));
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backTexture));
        backButton.getImage().setScaling(Scaling.fit);
        backButton.setSize(100, 100); // Set preferred size
        backButton.setPosition(50, 50);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new StartupScreen(game));
            }
        });
        stage.addActor(backButton);
    }

    @Override
    public void render(float delta) {
        world.step(delta, 6, 2); // Update the world
        stage.act(delta); // Update the stage
        stage.draw(); // Draw the stage

        // Render the player in idle animation
        batch.begin();
        player.update(delta); // Update player animations
        player.draw(batch); // Draw the player with original scale
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
       // player.dispose(); // Dispose player resources if necessary
        world.dispose(); // Dispose the dummy world
    }
}

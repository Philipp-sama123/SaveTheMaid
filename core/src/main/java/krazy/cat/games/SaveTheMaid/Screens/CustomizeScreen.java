package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.PPM;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_ENEMY;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_GROUND;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PLAYER;
import static krazy.cat.games.SaveTheMaid.WorldContactListener.CATEGORY_PROJECTILE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.utils.viewport.FitViewport;

import krazy.cat.games.SaveTheMaid.Characters.Player.Player;
import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class CustomizeScreen implements Screen {
    private final SaveTheMaidGame game;
    private final Player player;
    private final SpriteBatch batch;
    private final Stage stage;
    private final World world;
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private Body groundBody;

    public CustomizeScreen(SaveTheMaidGame game) {
        this.game = game;
        this.batch = game.batch;

        // Set up the camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(
            (float) GAME_WIDTH / 2 / PPM,
            (float) GAME_HEIGHT / 2 / PPM, camera
        );
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();

        // Create stage
        stage = new Stage(viewport, batch);

        // Create the Box2D world
        world = new World(new Vector2(0, -9.8f), true);

        // Create player instance
        player = new Player(world);

        // Create ground for physics
        createGround();

        // Set up the UI
        setupUI();
    }

    private void createGround() {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(camera.viewportWidth / 2, 10 / PPM); // Adjusted for PPM
        groundBodyDef.type = BodyDef.BodyType.StaticBody;

        groundBody = world.createBody(groundBodyDef);

        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(camera.viewportWidth, 10 / PPM); // Adjusted width and height for PPM

        FixtureDef groundFixtureDef = new FixtureDef();
        groundFixtureDef.shape = groundShape;
        groundFixtureDef.filter.categoryBits = CATEGORY_GROUND;
        groundFixtureDef.filter.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE;
        groundBody.createFixture(groundFixtureDef);

        groundShape.dispose();
    }

    private void setupUI() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(new Texture(Gdx.files.internal("UiSprites/Buttons/Shooting.png"))); // Default button state
        style.down = new TextureRegionDrawable(new Texture(Gdx.files.internal("UiSprites/Buttons/ShootingPressed.png"))); // Pressed button state

        ImageButton backButton = new ImageButton(style);

        // Set button size (adjust based on your PPM)
        backButton.setSize(.25f, .25f); // 1 world unit, adjust as necessary

        // Position the button at the top-left corner
        float buttonX = 10 / PPM; // Small margin from the left
        float buttonY = viewport.getWorldHeight() - backButton.getHeight() - 10 / PPM; // Adjust for height and margin
        backButton.setPosition(buttonX, buttonY);

        // Add a click listener to the button
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new StartupScreen(game));
            }
        });

        // Add the button to the stage
        stage.addActor(backButton);
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update physics world
        world.step(delta, 6, 2);

        // Update camera and stage
        camera.update();
        stage.act(delta);

        // Render the player and the ground
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.update(delta);
        player.draw(batch);
        batch.end();

        // Render the UI
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        world.dispose();
    }
}

package krazy.cat.games.SaveTheMaid.UI;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import javax.swing.text.View;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.Screens.PauseScreen;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class Hud implements Disposable {
    private SaveTheMaidGame game;
    private InputMultiplexer inputMultiplexer;
    public Stage stage;
    public Viewport viewport;

    public Integer worldTimer;
    private float timeCount;
    private Integer health;
    private Image healthBar;
    private Image healthBarBackground;
    Label countdownLabel;

    Label timeLabel;
    Label levelLabel;
    Label worldLabel;
    Label playerLabel;

    private ImageButton jumpButton;
    private ImageButton shootButton;
    private ImageButton shootUpButton;
    private ImageButton debugButton;
    private ImageButton pauseButton;
    private ImageButton slideButton;
    private Touchpad movementJoystick;

    private boolean jumpPressed = false;

    public Hud(SaveTheMaidGame game, Viewport viewport) {
        worldTimer = 0;
        timeCount = 0;
        health = 0;
        this.game = game;
        // Use StretchViewport for the HUD
        this.viewport = viewport;
        stage = new Stage(viewport, game.batch);

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        BitmapFont font = new BitmapFont();
        font.getData().setScale(0.5f); // Adjust font scaling for StretchViewport

        countdownLabel = new Label(String.format("%03d", worldTimer), new Label.LabelStyle(font, Color.WHITE));

        timeLabel = new Label("TIME", new Label.LabelStyle(font, Color.WHITE));
        levelLabel = new Label("1/1", new Label.LabelStyle(font, Color.WHITE));
        worldLabel = new Label("WORLD", new Label.LabelStyle(font, Color.WHITE));
        playerLabel = new Label("HEALTH", new Label.LabelStyle(font, Color.WHITE));
        // Add the health bar to a new Table
        Texture healthBarTexture = GameAssetManager.getInstance().get(AssetPaths.HEALTH_BAR_SIMPLE, Texture.class);
        Texture healthBarBackgroundTexture = GameAssetManager.getInstance().get(AssetPaths.HEALTH_BAR_CONTAINER, Texture.class);

        healthBar = new Image(new TextureRegionDrawable(new TextureRegion(healthBarTexture)));
        healthBarBackground = new Image(new TextureRegionDrawable(new TextureRegion(healthBarBackgroundTexture)));

        Stack healthStack = new Stack();
        healthStack.add(healthBar); // Add the health bar first
        healthStack.add(healthBarBackground); // Add the health label over the health bar
        table.add(healthStack).expandX();
        table.add().expandX();
        table.add(timeLabel).expandX();
        table.row();
        table.add().expandX();
        table.add().expandX();
        table.add(countdownLabel).expandX();

        stage.addActor(table);

        // Initialize other UI components
        createMovementJoystick();
        createButtons();
    }

    public void enableInput() {
        // Initialize InputMultiplexer
        if (inputMultiplexer == null) {
            inputMultiplexer = new InputMultiplexer();
            inputMultiplexer.addProcessor(stage);
        }
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void disableInput() {
        Gdx.input.setInputProcessor(null);
    }


    private void createButtons() {
        Texture jumpTextureUp = new Texture(Gdx.files.internal("UiSprites/Buttons/Jump.png"));
        Texture jumpTextureDown = new Texture(Gdx.files.internal("UiSprites/Buttons/JumpPressed.png"));
        ImageButton.ImageButtonStyle buttonStyleJump = new ImageButton.ImageButtonStyle();

        buttonStyleJump.up = new TextureRegionDrawable(jumpTextureUp);
        buttonStyleJump.down = new TextureRegionDrawable(jumpTextureDown);

        jumpButton = new ImageButton(buttonStyleJump);

        Texture slideTextureUp = new Texture(Gdx.files.internal("UiSprites/Buttons/SlideButton.png"));
        Texture slideTextureDown = new Texture(Gdx.files.internal("UiSprites/Buttons/SlideButtonPressed.png"));
        ImageButton.ImageButtonStyle buttonStyleSlide = new ImageButton.ImageButtonStyle();

        buttonStyleSlide.up = new TextureRegionDrawable(slideTextureUp);
        buttonStyleSlide.down = new TextureRegionDrawable(slideTextureDown);

        slideButton = new ImageButton(buttonStyleSlide);

        Texture shootTextureUp = new Texture(Gdx.files.internal("UiSprites/Buttons/Shooting.png"));
        Texture shootTextureDown = new Texture(Gdx.files.internal("UiSprites/Buttons/ShootingPressed.png"));

        ImageButton.ImageButtonStyle buttonStyleShoot = new ImageButton.ImageButtonStyle();

        buttonStyleShoot.up = new TextureRegionDrawable(shootTextureUp);
        buttonStyleShoot.down = new TextureRegionDrawable(shootTextureDown);

        shootButton = new ImageButton(buttonStyleShoot);

        Texture shootUpTextureUp = new Texture(Gdx.files.internal("UiSprites/Buttons/ShootingUp.png"));
        Texture shootUpTextureDown = new Texture(Gdx.files.internal("UiSprites/Buttons/ShootingUpPressed.png"));

        ImageButton.ImageButtonStyle buttonStyleShootUp = new ImageButton.ImageButtonStyle();

        buttonStyleShootUp.up = new TextureRegionDrawable(shootUpTextureUp);
        buttonStyleShootUp.down = new TextureRegionDrawable(shootUpTextureDown);

        shootUpButton = new ImageButton(buttonStyleShootUp);

        Texture debugButtonTextureUp = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Notifications.png"));
        Texture debugButtonTextureDown = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Notifications.png"));

        ImageButton.ImageButtonStyle buttonStyleDebugButton = new ImageButton.ImageButtonStyle();

        buttonStyleDebugButton.up = new TextureRegionDrawable(debugButtonTextureUp);
        buttonStyleDebugButton.down = new TextureRegionDrawable(debugButtonTextureDown);

        debugButton = new ImageButton(buttonStyleDebugButton);

        Texture pausetTextureUp = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Pause.png"));
        Texture pauseTextureDown = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Pause.png"));
        ImageButton.ImageButtonStyle buttonStylePauseButton = new ImageButton.ImageButtonStyle();

        buttonStylePauseButton.up = new TextureRegionDrawable(pausetTextureUp);
        buttonStylePauseButton.down = new TextureRegionDrawable(pauseTextureDown);

        pauseButton = new ImageButton(buttonStylePauseButton);

        // Arrange main buttons in a right-aligned table
        Table buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.center().right();

        buttonTable.add(shootUpButton).size(25, 25).pad(5);
        buttonTable.add(jumpButton).size(25, 25).pad(5).row();
        buttonTable.add(shootButton).size(25, 25).pad(5);
        buttonTable.add(slideButton).size(25, 25).pad(5);
        stage.addActor(buttonTable);

        Table debugTable = new Table();
        debugTable.setFillParent(true);
        debugTable.bottom().left();
        debugTable.add(pauseButton).size(15, 15).pad(2.5f);
        debugTable.add(debugButton).size(15, 15).pad(2.5f);
        stage.addActor(debugTable);

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.getPauseScreen()); // Switch back to GameScreen
            }
        });

        debugButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getGameScreen().isShowBox2dDebug = !game.getGameScreen().isShowBox2dDebug; // Switch back to GameScreen
            }
        });
    }

    private void createMovementJoystick() {
        // Load textures for the joystick background and knob
        Texture joystickBackground = new Texture(Gdx.files.internal("UiSprites/Joystick/2dStick.png"));
        Texture joystickKnob = new Texture(Gdx.files.internal("UiSprites/Joystick/MoveStick.png"));

        // Check if textures are loaded
        if (!joystickBackground.getTextureData().isPrepared()) {
            joystickBackground.getTextureData().prepare();
        }
        if (!joystickKnob.getTextureData().isPrepared()) {
            joystickKnob.getTextureData().prepare();
        }

        // Create joystick style
        Touchpad.TouchpadStyle movementJoystickStyle = new Touchpad.TouchpadStyle();

        // Set background and knob using TextureRegionDrawable
        movementJoystickStyle.background = new TextureRegionDrawable(new TextureRegion(joystickBackground));
        movementJoystickStyle.knob = new TextureRegionDrawable(new TextureRegion(joystickKnob));

        // Adjust knob size relative to background
        TextureRegionDrawable knobDrawable = (TextureRegionDrawable) movementJoystickStyle.knob;
        float knobWidth = joystickKnob.getWidth() * .15f;  // Scale down the knob width
        float knobHeight = joystickKnob.getHeight() * .15f; // Scale down the knob height

        knobDrawable.setMinWidth(knobWidth);  // Adjust knob size
        knobDrawable.setMinHeight(knobHeight);

        // Create the Touchpad with a smaller size
        float joystickWidth = joystickBackground.getWidth() * .15f; // Scale down the joystick background width
        float joystickHeight = joystickBackground.getHeight() * .15f; // Scale down the joystick background height

        movementJoystick = new Touchpad(10, movementJoystickStyle);

        // Place the joystick in the bottom left corner
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left();

        // Use the scaled size for the joystick
        table.add(movementJoystick).size(joystickWidth, joystickHeight).pad(25);

        stage.addActor(table);
    }

    // Method to update the timer and display in the HUD
    public void update(float deltaTime) {
        timeCount += deltaTime;

        if (timeCount >= 1) {  // Decrement timer every second
            worldTimer++;
            countdownLabel.setText(String.format("%03d", worldTimer));  // Update label
            timeCount = 0;
        }

        if (shootUpButton.isPressed()) {
            game.getGameScreen().getPlayer().shootUp();
        }
        if (shootButton.isPressed()) {
            game.getGameScreen().getPlayer().shoot();
        }

        if (jumpButton.isPressed()) {
            if (!jumpPressed) {
                game.getGameScreen().getPlayer().jump();
                jumpPressed = true;
            }
        } else {
            jumpPressed = false; // Reset when button is released
        }
        if (slideButton.isPressed()) {
            game.getGameScreen().getPlayer().slide();
        }
        float joystickPercentX = movementJoystick.getKnobPercentX(); // Knob percentage movement on the X-axis
        game.getGameScreen().getPlayer().move(joystickPercentX);
        game.getGameScreen().getPlayer().crouch(movementJoystick.getKnobPercentY() < -0.75);
    }

    public void updateHealth(int currentHealth, int maxHealth) {
        if (currentHealth < 0) currentHealth = 0;
        if (currentHealth > maxHealth) currentHealth = maxHealth;

        // Calculate the new width of the health bar based on the health percentage
        float healthPercentage = (float) currentHealth / maxHealth;
        float originalWidth = healthBar.getPrefWidth(); // Assuming this gives the original texture width
        float newWidth = originalWidth * healthPercentage;

        // Update the width of the health bar
        healthBar.setWidth(newWidth);

        // Optionally reposition the health bar to keep it aligned correctly within the background
        float healthBarX = healthBarBackground.getX();
        healthBar.setPosition(healthBarX, healthBar.getY());

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public float getWorldTimer() {
        return worldTimer;
    }
}

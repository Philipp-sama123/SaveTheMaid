package krazy.cat.games.SaveTheMaid.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;
import krazy.cat.games.SaveTheMaid.Tools.ScoreSystemManager;

public class Hud implements Disposable {
    private final SaveTheMaidGame game;
    private InputMultiplexer inputMultiplexer;
    public Stage stage;
    public Viewport viewport;

    private Integer worldTimer;
    private float timeCount;
    private final Integer health;

    private final Label countdownLabel;
    private final Label timeLabel;
    private final Label levelLabel;
    private final Label worldLabel;
    private final Label playerLabel;
    private final Label scoreLabel; // NEW: Label to display the score
    private final Label highScoreLabel; // NEW: Label for the high score

    private final Image healthBar;
    private ImageButton jumpButton;
    private ImageButton shootButton;
    private ImageButton shootUpButton;
    private ImageButton debugButton;
    private ImageButton pauseButton;
    private ImageButton slideButton;
    private Touchpad movementJoystick;

    private boolean jumpPressed = false;


    public Hud(SaveTheMaidGame game, Viewport viewport) {
        this.game = game;
        this.viewport = viewport;
        this.worldTimer = 0;
        this.timeCount = 0;
        this.health = 100; // Default health value

        // Set up stage and viewport
        stage = new Stage(viewport, game.batch);

        // Initialize the table layout
        Table table = new Table();
        table.top();
        table.setFillParent(true);

        BitmapFont font = new BitmapFont();
        font.getData().setScale(0.5f);

        // Labels
        countdownLabel = new Label(String.format("%03d", worldTimer), new Label.LabelStyle(font, Color.WHITE));
        timeLabel = new Label("TIME", new Label.LabelStyle(font, Color.WHITE));
        levelLabel = new Label("1/1", new Label.LabelStyle(font, Color.WHITE));
        worldLabel = new Label("WORLD", new Label.LabelStyle(font, Color.WHITE));
        playerLabel = new Label("HEALTH", new Label.LabelStyle(font, Color.WHITE));
        scoreLabel = new Label("Score: " + ScoreSystemManager.getInstance().getScore(), new Label.LabelStyle(font, Color.YELLOW)); // Initial score display
        highScoreLabel = new Label(("High Score: " + ScoreSystemManager.getInstance().getHighScore()), new Label.LabelStyle(font, Color.GREEN)); // Initial high score

        // Health Bar
        Texture healthBarTexture = GameAssetManager.getInstance().get(AssetPaths.HEALTH_BAR_SIMPLE, Texture.class);
        Texture healthBarBackgroundTexture = GameAssetManager.getInstance().get(AssetPaths.HEALTH_BAR_CONTAINER, Texture.class);

        healthBar = new Image(new TextureRegionDrawable(new TextureRegion(healthBarTexture)));

        Stack healthStack = new Stack();
        healthStack.add(healthBar);

        // Arrange UI elements in the table
        table.add(healthStack).expandX();
        table.add().expandX();
        table.add(timeLabel).expandX();
        table.add(scoreLabel).expandX(); // Add score to the HUD
        table.row();
        table.add(playerLabel).expandX();
        table.add().expandX();
        table.add(countdownLabel).expandX();
        table.add(highScoreLabel).expandX(); // Add high score to the HUD

        stage.addActor(table);

        // Initialize controls
        createMovementJoystick();
        createButtons();
    }

    public void enableInput() {
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

        buttonTable.add(shootUpButton).size(25, 25).pad(2.5f);
        buttonTable.add(jumpButton).size(25, 25).pad(2.5f).row();
        buttonTable.add(shootButton).size(25, 25).pad(2.5f);
        buttonTable.add(slideButton).size(25, 25).pad(2.5f);
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
                game.getCurrentLevel().isShowBox2dDebug = !game.getCurrentLevel().isShowBox2dDebug; // Switch back to GameScreen
            }
        });
    }

    private void createMovementJoystick() {
        // Load textures for the joystick background and knob
        Texture joystickBackground = new Texture(Gdx.files.internal("UiSprites/Joystick/ShootPad.png"));
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
        float knobWidth = joystickKnob.getWidth() * .1f;  // Scale down the knob width
        float knobHeight = joystickKnob.getHeight() * .1f; // Scale down the knob height

        knobDrawable.setMinWidth(knobWidth);  // Adjust knob size
        knobDrawable.setMinHeight(knobHeight);

        // Create the Touchpad with a smaller size
        float joystickWidth = joystickBackground.getWidth() * .1f; // Scale down the joystick background width
        float joystickHeight = joystickBackground.getHeight() * .1f; // Scale down the joystick background height

        movementJoystick = new Touchpad(10, movementJoystickStyle);

        // Place the joystick in the bottom left corner
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left();

        // Use the scaled size for the joystick
        table.add(movementJoystick).size(joystickWidth, joystickHeight).pad(25);

        stage.addActor(table);
    }

    public void updateScore() {
        scoreLabel.setText("Score: " + ScoreSystemManager.getInstance().getScore());
        highScoreLabel.setText("High Score: " + ScoreSystemManager.getInstance().getHighScore());
    }

    public void update(float deltaTime) {
        timeCount += deltaTime;

        if (timeCount >= 1) {
            worldTimer++;
            countdownLabel.setText(String.format("%03d", worldTimer));
            timeCount = 0;
        }

        if (shootUpButton.isPressed()) {
            game.getCurrentLevel().getPlayer().shootUp();
        }
        if (shootButton.isPressed()) {
            game.getCurrentLevel().getPlayer().shoot();
        }

        if (jumpButton.isPressed()) {
            if (!jumpPressed) {
                game.getCurrentLevel().getPlayer().jump();
                jumpPressed = true;
            }
        } else {
            jumpPressed = false; // Reset when button is released
        }
        if (slideButton.isPressed()) {
            game.getCurrentLevel().getPlayer().slide();
        }
        float joystickPercentX = movementJoystick.getKnobPercentX(); // Knob percentage movement on the X-axis
        game.getCurrentLevel().getPlayer().move(joystickPercentX);
        game.getCurrentLevel().getPlayer().crouch(movementJoystick.getKnobPercentY() < -0.75);
    }

    public void updateHealth(int currentHealth, int maxHealth) {
        currentHealth = Math.max(0, Math.min(currentHealth, maxHealth));
        healthBar.setWidth(healthBar.getPrefWidth() * (float) currentHealth / maxHealth);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public float getWorldTimer() {
        return worldTimer;
    }
}

package krazy.cat.games.SaveTheMaid.Scenes;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import javax.swing.ButtonModel;

public class Hud implements Disposable {
    private final InputMultiplexer inputMultiplexer;
    public Stage stage;
    public Viewport viewport;

    private Integer worldTimer;
    private float timeCount;
    private Integer score;

    Label countdownLabel;
    Label scoreLabel;
    Label timeLabel;
    Label levelLabel;
    Label worldLabel;
    Label playerLabel;
    private ImageButton jumpButton;
    private ImageButton shootButton;
    private Touchpad movementJoystick;
    private Touchpad shootingJoystick;

    public Hud(SpriteBatch spriteBatch) {
        worldTimer = 300;
        timeCount = 0;
        score = 0;

        viewport = new FitViewport(GAME_WIDTH, GAME_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, spriteBatch);
        Table table = new Table();
        table.top();
        table.setFillParent(true);
        BitmapFont font = new BitmapFont();
        font.getData().setScale(0.5f);  // Scale down the font size to 50%
        countdownLabel = new Label(String.format("%03d", worldTimer), new Label.LabelStyle(font, Color.WHITE));
        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(font, Color.WHITE));
        timeLabel = new Label("TIME", new Label.LabelStyle(font, Color.WHITE));
        levelLabel = new Label("1/1", new Label.LabelStyle(font, Color.WHITE));
        worldLabel = new Label("WORLD", new Label.LabelStyle(font, Color.WHITE));
        playerLabel = new Label("PLAYER", new Label.LabelStyle(font, Color.WHITE));

        table.add(playerLabel).expandX();
        table.add(worldLabel).expandX();
        table.add(timeLabel).expandX();
        table.row();
        table.add(scoreLabel).expandX();
        table.add(levelLabel).expandX();
        table.add(countdownLabel).expandX();

        stage.addActor(table);

        createMovementJoystick();
        createButtons();

        // Initialize InputMultiplexer
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public Touchpad getMovementJoystick() {
        return movementJoystick;
    }


    public ImageButton getJumpButton() {
        return jumpButton;
    }

    public ImageButton getShootButton() {
        return shootButton;
    }

    private void createButtons() {
        // Load texture for pause button
        Texture jumpTextureUp = new Texture(Gdx.files.internal("UiSprites/Buttons/Jump.png"));
        Texture jumpTextureDown = new Texture(Gdx.files.internal("UiSprites/Buttons/JumpPressed.png"));
        ImageButton.ImageButtonStyle buttonStylePause = new ImageButton.ImageButtonStyle();

        buttonStylePause.up = new TextureRegionDrawable(jumpTextureUp);
        buttonStylePause.down = new TextureRegionDrawable(jumpTextureDown);

        jumpButton = new ImageButton(buttonStylePause);

        Texture shootTextureUp = new Texture(Gdx.files.internal("UiSprites/Buttons/Shooting.png"));
        Texture shootTextureDown = new Texture(Gdx.files.internal("UiSprites/Buttons/ShootingPressed.png"));

        ImageButton.ImageButtonStyle buttonStyleShoot = new ImageButton.ImageButtonStyle();

        buttonStyleShoot.up = new TextureRegionDrawable(shootTextureUp);
        buttonStyleShoot.down = new TextureRegionDrawable(shootTextureDown);

        shootButton = new ImageButton(buttonStyleShoot);

        // Arrange button in a table
        Table table = new Table();
        table.setFillParent(true);
        table.center().right();
        table.add(shootButton).size(25, 25).pad(10);
        table.add(jumpButton).size(25, 25).pad(10);

        stage.addActor(table);
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
        float knobWidth = joystickKnob.getWidth() * 0.125f;  // Scale down the knob width
        float knobHeight = joystickKnob.getHeight() * 0.125f; // Scale down the knob height

        knobDrawable.setMinWidth(knobWidth);  // Adjust knob size
        knobDrawable.setMinHeight(knobHeight);

        // Create the Touchpad with a smaller size
        float joystickWidth = joystickBackground.getWidth() * 0.125f; // Scale down the joystick background width
        float joystickHeight = joystickBackground.getHeight() * 0.125f; // Scale down the joystick background height

        movementJoystick = new Touchpad(10, movementJoystickStyle);

        // Place the joystick in the bottom left corner
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left();

        // Use the scaled size for the joystick
        table.add(movementJoystick).size(joystickWidth, joystickHeight).pad(25);

        stage.addActor(table);
    }


    @Override
    public void dispose() {
        stage.dispose();
    }

}

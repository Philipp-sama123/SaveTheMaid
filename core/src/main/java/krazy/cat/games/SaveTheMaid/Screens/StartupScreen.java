package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class StartupScreen implements Screen {

    private final SaveTheMaidGame game;
    private final Stage stage;
    private final Viewport viewport;
    Texture backgroundTexture;
    private String userEmail;
    private final Label greetingLabel;

    public StartupScreen(SaveTheMaidGame game) {
        this.game = game;
        backgroundTexture = new Texture("Characters/FemaleAgent/Portrait/Red.png");
        viewport = new FitViewport(SaveTheMaidGame.GAME_WIDTH, SaveTheMaidGame.GAME_HEIGHT);

        stage = new Stage(viewport, game.batch);

        // Load button textures
        Texture playTexture = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Simple right.png"));
        Texture playPressedTexture = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Simple right.png")); // Add pressed texture
        Texture customizeTexture = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Settings.png"));
        Texture customizePressedTexture = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Settings.png")); // Add pressed texture
        Texture logoutTexture = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Exit.png"));
        Texture logoutPressedTexture = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Exit.png")); // Add pressed texture
        // Set up Logout button
        ImageButton logoutButton = new ImageButton(new TextureRegionDrawable(logoutTexture), new TextureRegionDrawable(logoutPressedTexture));
        // Set up Play button
        ImageButton playButton = new ImageButton(new TextureRegionDrawable(playTexture), new TextureRegionDrawable(playPressedTexture));
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.startVillageLevel();
            }
        });      // Set up Play 2 button
        ImageButton playLevel2Button = new ImageButton(new TextureRegionDrawable(playTexture), new TextureRegionDrawable(playPressedTexture));
        playLevel2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.startHellLevel();
            }
        });


        // Set up Customize button
        ImageButton customizeButton = new ImageButton(new TextureRegionDrawable(customizeTexture), new TextureRegionDrawable(customizePressedTexture));
        customizeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CustomizeScreen(game));
            }
        });

        BitmapFont font = new BitmapFont(); // Use your own font if you have one
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;

        greetingLabel = new Label("", labelStyle);
        updateGreetingText();

        // Layout buttons in a table
        Table table = new Table();
        table.center();
        table.setFillParent(true);
        table.add(greetingLabel).padBottom(20).row();  // Add greeting label

        table.add(playButton).size(50).pad(5);
        table.add(playLevel2Button).size(50).pad(5);
        table.add(customizeButton).size(50).pad(5);
        table.add(logoutButton).size(50).pad(5);

        stage.addActor(table);
    }

    @Override
    public void show() {

        updateGreetingText();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Draw the background
        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, backgroundTexture.getWidth(), backgroundTexture.getHeight());
        game.batch.draw(backgroundTexture, GAME_WIDTH - backgroundTexture.getWidth(), 0, backgroundTexture.getWidth(), backgroundTexture.getHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private void updateGreetingText() {
        if (userEmail != null) {
            greetingLabel.setText("Hello, " + userEmail);
        } else {
            greetingLabel.setText("Hello, Guest");
        }
    }
}

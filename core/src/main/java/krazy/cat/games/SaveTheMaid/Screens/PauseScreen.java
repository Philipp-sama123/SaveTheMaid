package krazy.cat.games.SaveTheMaid.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class PauseScreen implements Screen {
    private final SaveTheMaidGame game;
    private Stage stage;
    private Texture backgroundTexture;
    private ImageButton restartButton;

    public PauseScreen(SaveTheMaidGame game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());

        // Load the semi-transparent background texture
        backgroundTexture = new Texture(Gdx.files.internal("PlatformerAssets/BackgroundLayers/Normal BG/BG_1.png"));
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setColor(1, 1, 1, 0.5f); // Semi-transparent
        backgroundImage.setFillParent(true);

        stage.addActor(backgroundImage);

        // Add restart button

        Texture restartTextureUp = new Texture(Gdx.files.internal("UiSprites/128 px/Buttons/Repeat.png"));
        Texture restartTextureDown = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Repeat.png"));
        ImageButton.ImageButtonStyle buttonStyleRestartButton = new ImageButton.ImageButtonStyle();

        buttonStyleRestartButton.up = new TextureRegionDrawable(restartTextureUp);
        buttonStyleRestartButton.down = new TextureRegionDrawable(restartTextureDown);

        restartButton = new ImageButton(buttonStyleRestartButton);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("INPUT", "game.setScreen(game.getGameScreen());");
                game.setScreen(game.getGameScreen()); // Switch back to GameScreen
            }
        });

        // Arrange buttons in a table layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(restartButton).pad(10);

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the pause screen UI
        stage.act();
        stage.draw();
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
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}

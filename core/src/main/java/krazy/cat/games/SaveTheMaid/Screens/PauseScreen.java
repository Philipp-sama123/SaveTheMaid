package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class PauseScreen implements Screen {
    private final SaveTheMaidGame game;
    private final Stage stage;
    private final Texture backgroundTexture;
    private final ImageButton resumeButton;
    private final ImageButton homeButton;
    private final ImageButton restartButton;

    public PauseScreen(SaveTheMaidGame game) {
        this.game = game;

        stage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT));

        // Load the semi-transparent background texture
        backgroundTexture = new Texture(Gdx.files.internal("PlatformerAssets/BackgroundLayers/Normal BG/BG_1.png"));
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setColor(1, 1, 1, 0.5f); // Semi-transparent
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        Texture resumeTextureUp = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Simple right.png"));
        Texture resumeTextureDown = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Simple right.png"));
        ImageButton.ImageButtonStyle buttonStyleResumeButton = new ImageButton.ImageButtonStyle();

        buttonStyleResumeButton.up = new TextureRegionDrawable(resumeTextureUp);
        buttonStyleResumeButton.down = new TextureRegionDrawable(resumeTextureDown);

        resumeButton = new ImageButton(buttonStyleResumeButton);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.getVillageLevel()); // Switch back to GameScreen
            }
        });
        Texture homeTextureUp = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Home.png"));
        Texture homeTextureDown = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Home.png"));
        ImageButton.ImageButtonStyle buttonStyleHomeButton = new ImageButton.ImageButtonStyle();

        buttonStyleHomeButton.up = new TextureRegionDrawable(homeTextureUp);
        buttonStyleHomeButton.down = new TextureRegionDrawable(homeTextureDown);

        homeButton = new ImageButton(buttonStyleHomeButton);
        homeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.reloadCurrentLevel();
                game.setScreen(game.getStartupScreen()); // Switch back to GameScreen
            }
        });
        Texture restartTextureUp = new Texture(Gdx.files.internal("UiSprites/128 px/Blue/Repeat.png"));
        Texture restartTextureDown = new Texture(Gdx.files.internal("UiSprites/128 px/Yellow/Repeat.png"));
        ImageButton.ImageButtonStyle buttonStyleRestartButton = new ImageButton.ImageButtonStyle();

        buttonStyleRestartButton.up = new TextureRegionDrawable(restartTextureUp);
        buttonStyleRestartButton.down = new TextureRegionDrawable(restartTextureDown);

        restartButton = new ImageButton(buttonStyleRestartButton);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.reloadCurrentLevel();
                game.setScreen(game.getCurrentLevel());
            }
        });

        // Arrange buttons in a table layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(resumeButton).size(35, 35).pad(10);
        table.add(homeButton).size(35, 35).pad(10);
        table.add(restartButton).size(35, 35).pad(10);

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

package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class StartupScreen implements Screen {

    private final SaveTheMaidGame game;
    private final Stage stage;
    private final Viewport viewport;
    Texture backgroundTexture;

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


        // Set up Play button
        ImageButton playButton = new ImageButton(new TextureRegionDrawable(playTexture), new TextureRegionDrawable(playPressedTexture));
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.getGameScreen());
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
        // Layout buttons in a table
        Table table = new Table();
        table.center();
        table.setFillParent(true);
        table.add(playButton).size(50).pad(20);
        table.add(customizeButton).size(50).pad(20);

        stage.addActor(table);
    }

    @Override
    public void show() {
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
}

package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class GameOverScreen implements Screen {
    private final SaveTheMaidGame game;
    private final Stage stage;
    private final Texture backgroundTexture;

    public GameOverScreen(SaveTheMaidGame game, float timePlayed, int enemiesKilled,int catsSaved) {
        this.game = game;

        stage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT));

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal("PlatformerAssets/BackgroundLayers/Normal BG/BG_1.png"));
        Table backgroundTable = new Table();
        backgroundTable.setFillParent(true);
        backgroundTable.setBackground(new TextureRegionDrawable(backgroundTexture));
        stage.addActor(backgroundTable);

        // Skin for UI elements
        Skin skin = new Skin(Gdx.files.internal("Fonts/uiskin.json"));

        // Game Over label
        Label gameOverLabel = new Label("GAME OVER", skin);
        gameOverLabel.setFontScale(2);

        // Time Played label
        String timeFormatted = formatTime(timePlayed);
        Label timeLabel = new Label("Time Played: " + timeFormatted, skin);

        // Enemies Killed label
        Label enemiesKilledLabel = new Label("Enemies killed: " + enemiesKilled, skin);

        // Cats Saved label
        Label catsSavedLabel = new Label("Cats Saved: " + catsSaved, skin);

        // Buttons
        TextButton mainMenuButton = new TextButton("Main Menu", skin);
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.getStartupScreen());
            }
        });

        TextButton nextLevelButton = new TextButton("Next Level", skin);
        nextLevelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //  game.loadNextLevel(); // Replace with your method to load the next level
            }
        });

        TextButton replayButton = new TextButton("Replay", skin);
        replayButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.reloadCurrentLevel();
                game.setScreen(game.getCurrentLevel());
            }
        });

        // Arrange UI components
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.center();
        uiTable.add(gameOverLabel).padBottom(10).row();
        uiTable.add(timeLabel).padBottom(5).row();
        uiTable.add(enemiesKilledLabel).padBottom(5).row();
        uiTable.add(catsSavedLabel).padBottom(5).row();
        uiTable.add(mainMenuButton).size(100, 25).padBottom(5).row();
        uiTable.add(replayButton).size(100, 25);

        stage.addActor(uiTable);
    }

    private String formatTime(float seconds) {
        int minutes = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

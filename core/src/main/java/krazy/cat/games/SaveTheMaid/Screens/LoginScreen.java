package krazy.cat.games.SaveTheMaid.Screens;

import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_HEIGHT;
import static krazy.cat.games.SaveTheMaid.SaveTheMaidGame.GAME_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class LoginScreen implements Screen {
    private final SaveTheMaidGame game;
    private final Stage stage;
    private final Viewport viewport;

    private final Texture backgroundTexture;

    private final Skin skin;
    private final TextButton logInButton;
    private final TextButton backButton;
    private final TextButton createUserButton;

    private final TextField emailField;
    private final TextField passwordField;
    private final TextField usernameField;

    private final Label errorLabel;
    private final Label loadingLabel;

    private final Preferences preferences;

    public LoginScreen(SaveTheMaidGame game) {
        this.game = game;

        skin = new Skin(Gdx.files.internal("Fonts/uiskin.json"));

        viewport = new FitViewport(GAME_WIDTH, GAME_HEIGHT);
        stage = new Stage(viewport, game.batch);

        // Initialize preferences for local storage
        preferences = Gdx.app.getPreferences("SaveTheMaidPrefs");

        // Initialize the loading label
        loadingLabel = new Label("Loading...", skin);
        loadingLabel.setVisible(false); // Hidden by default
        loadingLabel.setAlignment(Align.center);

        // Background texture
        backgroundTexture = new Texture("PlatformerAssets/BackgroundLayers/Normal BG/BG_1.png");

        // Create buttons
        createUserButton = new TextButton("Create User", skin);
        createUserButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String email = emailField.getText();
                String password = passwordField.getText();
                String username = usernameField.getText();

                showLoading();
                if (preferences.contains(email)) {
                    showError("User already exists!");
                } else {
                    preferences.putString(email + "_password", password);
                    preferences.putString(email + "_username", username);
                    preferences.flush();
                    hideLoading();
                    game.setScreen(game.getStartupScreen()); // Switch screen on success
                }
            }
        });

        backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.getStartupScreen());
            }
        });

        // Create Sign In button
        logInButton = new TextButton("LogIn", skin);
        logInButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String email = emailField.getText();
                String password = passwordField.getText();

                showLoading();
                if (!preferences.contains(email)) {
                    showError("User does not exist!");
                } else if (!preferences.getString(email + "_password").equals(password)) {
                    showError("Incorrect password!");
                } else {
                    hideLoading();
                    game.setScreen(game.getStartupScreen()); // Switch screen on success
                }
            }
        });

        usernameField = createInputField("Enter Username", false);
        emailField = createInputField("Enter Email", false);
        passwordField = createInputField("Enter Password", true);

        // Layout with table
        Table table = new Table();
        table.center();
        table.setFillParent(true);

        // Error message label (initially hidden)
        errorLabel = new Label("", skin);
        errorLabel.setWidth(GAME_WIDTH * 0.75f);
        errorLabel.setVisible(false);
        errorLabel.setAlignment(Align.center);

        // Add elements to the table
        table.add(new Label("Username:", skin)).pad(5);
        table.add(usernameField).width(GAME_WIDTH * 0.6f).height(GAME_HEIGHT * 0.06f).pad(5).colspan(2).row();
        table.add(new Label("Email:", skin)).pad(5);
        table.add(emailField).width(GAME_WIDTH * 0.6f).height(GAME_HEIGHT * 0.06f).pad(5).colspan(2).row();
        table.add(new Label("Password:", skin)).pad(5);
        table.add(passwordField).width(GAME_WIDTH * 0.6f).height(GAME_HEIGHT * 0.06f).pad(5).colspan(2).row();
        table.row().pad(10); // Add padding above the button row
        table.add(createUserButton).size(GAME_WIDTH * 0.28f, GAME_HEIGHT * 0.08f).pad(2f); // Reduced size and padding
        table.add(logInButton).size(GAME_WIDTH * 0.28f, GAME_HEIGHT * 0.08f).pad(2f);
        table.add(backButton).size(GAME_WIDTH * 0.28f, GAME_HEIGHT * 0.08f).pad(2f).row();
        // Add the loading indicator to the table
        table.add(loadingLabel).colspan(3).padTop(10).row(); // Position it below the buttons
        table.add(errorLabel).colspan(3);

        stage.addActor(table);
    }

    private TextField createInputField(String placeholder, boolean isPassword) {
        TextField textField = new TextField("", skin);
        textField.setMessageText(placeholder);
        textField.setPasswordMode(isPassword);
        textField.setPasswordCharacter('*');
        textField.setWidth(GAME_WIDTH * 0.6f);
        textField.setHeight(GAME_HEIGHT * 0.06f);
        return textField;
    }

    private void showLoading() {
        loadingLabel.setVisible(true);
        errorLabel.setVisible(false);

        backButton.setVisible(false);
        createUserButton.setVisible(false);
        logInButton.setVisible(false);

        Gdx.graphics.setContinuousRendering(false); // Disable continuous rendering to save resources
    }

    private void hideLoading() {
        loadingLabel.setVisible(false);

        backButton.setVisible(true);
        createUserButton.setVisible(true);
        logInButton.setVisible(true);

        Gdx.graphics.setContinuousRendering(true); // Re-enable continuous rendering
    }

    private void showError(String message) {
        hideLoading();
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Render background
        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, GAME_WIDTH, GAME_HEIGHT);
        game.batch.end();

        // Render stage
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
        backgroundTexture.dispose();
    }
}

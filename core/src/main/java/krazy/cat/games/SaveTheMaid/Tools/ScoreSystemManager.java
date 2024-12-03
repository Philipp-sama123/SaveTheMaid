package krazy.cat.games.SaveTheMaid.Tools;

import java.util.prefs.Preferences;

public class ScoreSystemManager {
    private static ScoreSystemManager instance;

    private int score;
    private int highScore;

    // Preferences for persistent storage
    private Preferences preferences;
    private static final String HIGH_SCORE_KEY = "high_score";

    private ScoreSystemManager() {
        this.preferences = Preferences.userNodeForPackage(ScoreSystemManager.class);
        this.score = 0;
        this.highScore = loadHighScore(); // Load high score from storage on initialization
    }

    public static synchronized ScoreSystemManager getInstance() {
        if (instance == null) {
            instance = new ScoreSystemManager();
        }
        return instance;
    }

    // Increment the score
    public void addScore(int points) {
        score += points;
        if (score > highScore) {
            highScore = score; // Automatically update high score
            saveHighScore();   // Persist the updated high score
        }
    }

    public void resetScore() {
        score = 0;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    // Save the high score to persistent storage
    private void saveHighScore() {
        preferences.putInt(HIGH_SCORE_KEY, highScore);
    }

    // Load the high score from persistent storage
    private int loadHighScore() {
        return preferences.getInt(HIGH_SCORE_KEY, 0); // Default to 0 if no high score is found
    }
}

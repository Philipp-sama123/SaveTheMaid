package krazy.cat.games.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import krazy.cat.games.SaveTheMaid.FirebaseCallback;
import krazy.cat.games.SaveTheMaid.FirebaseInterface;
import krazy.cat.games.SaveTheMaid.SaveTheMaidGame;

public class AndroidLauncher extends AndroidApplication implements FirebaseInterface {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.

        // Check if FirebaseApp is already initialized
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(ApiKeys.FIREBASE_APP_ID) // From google-services.json
                .setApiKey(ApiKeys.FIREBASE_API_KEY) // From google-services.json
                .setDatabaseUrl(ApiKeys.FIREBASE_DATABASE_URL) // From Firebase Console (Realtime Database URL)
                .setProjectId(ApiKeys.FIREBASE_PROJECT_ID) // From google-services.json
                .build();

            FirebaseApp.initializeApp(this, options);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initialize(new SaveTheMaidGame(this), configuration);
    }

    @Override
    public void signIn(String email, String password, FirebaseCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign-in was successful, trigger the onSuccess callback
                    System.out.println("Sign-in successful: " + firebaseAuth.getCurrentUser().getEmail());
                    callback.onSuccess();  // Notify success
                } else {
                    // Sign-in failed, trigger the onFailure callback with error message
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    System.err.println("Sign-in failed: " + errorMessage);
                    callback.onFailure(errorMessage);  // Notify failure
                }
            });
    }


    @Override
    public void writeData(String path, Object data) {
        System.out.println("Trying to write: " + data);
        databaseReference.child(path).setValue(data)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    System.out.println("Data written to: " + path);
                } else {
                    System.err.println("Failed to write data: " + task.getException().getMessage());
                }
            });
    }

    @Override
    public void createUser(String email, String password, FirebaseCallback callback) {
        // Perform the user creation asynchronously
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    System.out.println("User created successfully: " + firebaseAuth.getCurrentUser().getEmail());
                    callback.onSuccess();  // Notify success through the callback
                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        System.err.println("User creation failed: " + exception.getMessage());
                        callback.onFailure(exception.getMessage());  // Notify failure with the error message
                    } else {
                        System.err.println("User creation failed: Unknown error occurred");
                        callback.onFailure("Unknown error occurred");  // Notify failure with a generic message
                    }
                }
            });

    }

    // New methods to fetch user data
    @Override
    public String getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getEmail() : null;  // Return user's email or null if not signed in
    }

    @Override
    public String getUserDisplayName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getDisplayName() : null;  // Return user's display name or null if not signed in
    }
    @Override
    public void setUserDisplayName(String name, FirebaseCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name) // Set the new display name
                .build();

            user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Display name updated to: " + name);
                        callback.onSuccess(); // Notify success through the callback
                    } else {
                        String errorMessage = task.getException() != null
                            ? task.getException().getMessage()
                            : "Unknown error occurred";
                        System.err.println("Failed to update display name: " + errorMessage);
                        callback.onFailure(errorMessage); // Notify failure through the callback
                    }
                });
        } else {
            String errorMessage = "No user is currently signed in.";
            System.err.println(errorMessage);
            callback.onFailure(errorMessage); // Notify failure if no user is signed in
        }
    }


    @Override
    public String getUserPhotoUrl() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;  // Return user's photo URL or null if not signed in
    }
    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}

package krazy.cat.games.SaveTheMaid;


public interface FirebaseInterface {
    void signIn(String email, String password, FirebaseCallback callback);

    void writeData(String path, Object data);

    void createUser(String email, String password, FirebaseCallback callback);
    // New methods to fetch user data
    String getUserEmail();

    String getUserDisplayName();

    void setUserDisplayName(String name, FirebaseCallback callback);

    String getUserPhotoUrl();

    void logout();
}

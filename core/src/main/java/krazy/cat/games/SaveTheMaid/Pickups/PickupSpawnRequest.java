package krazy.cat.games.SaveTheMaid.Pickups;

import com.badlogic.gdx.math.Vector2;

public class PickupSpawnRequest {
    public final Vector2 position;
    public final PickupObject.PickupType type;

    public PickupSpawnRequest(Vector2 position, PickupObject.PickupType type) {
        this.position = position;
        this.type = type;
    }
}

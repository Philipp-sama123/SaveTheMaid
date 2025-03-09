package krazy.cat.games.SaveTheMaid.Characters.AI;

public class EdgeSensorData {
    public final BaseAICharacter<?> character;
    public final String side; // "left" or "right"

    public EdgeSensorData(BaseAICharacter<?> character, String side) {
        this.character = character;
        this.side = side;
    }
}

package krazy.cat.games.SaveTheMaid.Characters.AI;

import static krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.EnemyType.BAT;
import static krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.EnemyType.DAMNED;
import static krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.EnemyType.RAT;
import static krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.EnemyType.ZOMBIE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.BatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.DamnedAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.EnemyType;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.RatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.ZombieAICharacter;
import krazy.cat.games.SaveTheMaid.Screens.BaseLevel;
import krazy.cat.games.SaveTheMaid.Tools.AssetPaths;
import krazy.cat.games.SaveTheMaid.Tools.GameAssetManager;

public class EnemySpawnPoint {
    private final World world;
    private final Vector2 position;
    private final BaseLevel baseLevel;
    private final float triggerRange;
    private final float spawnInterval;
    private final int maxEnemies;
    private final boolean spawnAllAtOnce;

    // New field: the enemy type for this spawn point
    private final EnemyType enemyType;

    private float timeSinceLastSpawn;
    private int currentEnemyCount;
    private boolean playerInRange;

    public EnemySpawnPoint(World world, Vector2 position, BaseLevel baseLevel, float triggerRange, float spawnInterval, int maxEnemies, boolean spawnAllAtOnce, EnemyType enemyType) {
        this.world = world;
        this.position = position;
        this.baseLevel = baseLevel;
        this.triggerRange = triggerRange;
        this.spawnInterval = spawnInterval;
        this.maxEnemies = maxEnemies;
        this.spawnAllAtOnce = spawnAllAtOnce;
        this.enemyType = enemyType;
        this.timeSinceLastSpawn = 0;
        this.currentEnemyCount = 0;
        this.playerInRange = false;
    }

    public void update(float deltaTime, Vector2 playerPosition) {
        float distance = position.dst(playerPosition);
        playerInRange = distance <= triggerRange;

        if (playerInRange && currentEnemyCount < maxEnemies) {
            if (spawnAllAtOnce) {
                spawnAllEnemies();
            } else {
                timeSinceLastSpawn += deltaTime;
                if (timeSinceLastSpawn >= spawnInterval) {
                    spawnEnemy();
                    timeSinceLastSpawn = 0;
                }
            }
        }
    }

    private void spawnAllEnemies() {
        while (currentEnemyCount < maxEnemies) {
            spawnEnemy();
        }
    }

    private void spawnEnemy() {
        if (currentEnemyCount >= maxEnemies) return;

        switch (enemyType) {
            case BAT:
                baseLevel.addEnemy(new BatAICharacter(world, new Vector2(position.x, position.y), baseLevel));
                break;
            case ZOMBIE:
                baseLevel.addEnemy(new ZombieAICharacter(world, new Vector2(position.x, position.y), baseLevel));
                break;
            case RAT:
                baseLevel.addEnemy(new RatAICharacter(world, new Vector2(position.x, position.y), baseLevel));
                break;
            case DAMNED:
                Texture spriteSheetDamnedMale = GameAssetManager.getInstance().get(AssetPaths.DAMNED_MALE, Texture.class);
                Texture spriteSheetDamnedFemale = GameAssetManager.getInstance().get(AssetPaths.DAMNED_FEMALE, Texture.class);

                Random rand = new Random();
                int random = rand.nextInt(2);// 0,1
                Texture chosenSprite = (random == 0) ? spriteSheetDamnedMale : spriteSheetDamnedFemale;

                baseLevel.addEnemy(new DamnedAICharacter(world, new Vector2(position.x, position.y), baseLevel, chosenSprite));
                break;
        }
        currentEnemyCount++;
    }
}

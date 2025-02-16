package krazy.cat.games.SaveTheMaid.Screens;

import com.badlogic.gdx.Screen;

import krazy.cat.games.SaveTheMaid.Characters.AI.BaseAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Enemies.RatAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.BaseFriendAICharacter;
import krazy.cat.games.SaveTheMaid.Characters.AI.Friends.CatCharacter;
import krazy.cat.games.SaveTheMaid.Characters.Player;

public abstract class BaseLevel implements Screen {
    public boolean isShowBox2dDebug;

    protected Player player;

    public abstract void addEnemyKill();

    public abstract void showGameOverScreen();

    public abstract void addCat(BaseFriendAICharacter baseFriendAICharacter);

    public abstract void addEnemy(BaseAICharacter baseAICharacter);

    public abstract void addCatSaved();

    public Player getPlayer() {
        return player;
    }
}

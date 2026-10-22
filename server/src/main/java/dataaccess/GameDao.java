
package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    void clear();
    int createGame(String name);
    GameData getGame(int id);
    Collection<GameData> listGames();
    void updateGame(GameData game);
}

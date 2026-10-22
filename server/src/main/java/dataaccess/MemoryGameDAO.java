
package dataaccess;

import model.GameData;
import chess.ChessGame;
import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextId = 1;

    public void clear() {
        games.clear();
        nextId = 1;
    }

    public int createGame(String name) {
        int id = nextId++;
        games.put(id, new GameData(
                id, null, null, name, new ChessGame()
        ));
        return id;
    }

    public GameData getGame(int id) {
        return games.get(id);
    }

    public Collection<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }
}

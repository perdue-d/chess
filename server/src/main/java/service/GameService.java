
package service;

import dataaccess.GameDAO;
import model.GameData;
import java.util.Collection;

public class GameService {
    private final GameDAO games;
    private final UserService users;

    public GameService(GameDAO games, UserService users) {
        this.games = games;
        this.users = users;
    }

    public record GameSummary(
            int gameID,
            String whiteUsername,
            String blackUsername,
            String gameName
    ) {}

    public record ListResult(Collection<GameSummary> games) {}
    public record CreateResult(int gameID) {}

    public ListResult list(String token) {
        users.authenticate(token);

        return new ListResult(games.listGames().stream()
                .map(g -> new GameSummary(
                        g.gameID(),
                        g.whiteUsername(),
                        g.blackUsername(),
                        g.gameName()
                )).toList());
    }

    public CreateResult create(String token, String name) {
        users.authenticate(token);

        if (name == null || name.isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        return new CreateResult(games.createGame(name));
    }

    public void join(String token, String color, Integer id) {
        String username = users.authenticate(token);

        if (id == null || color == null ||
                !(color.equals("WHITE") || color.equals("BLACK"))) {
            throw new ServiceException(400, "Error: bad request");
        }

        GameData game = games.getGame(id);

        if (game == null) {
            throw new ServiceException(400, "Error: bad request");
        }

        if (color.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new ServiceException(403, "Error: already taken");
            }

            games.updateGame(new GameData(
                    id, username, game.blackUsername(),
                    game.gameName(), game.game()
            ));
        } else {
            if (game.blackUsername() != null) {
                throw new ServiceException(403, "Error: already taken");
            }

            games.updateGame(new GameData(
                    id, game.whiteUsername(), username,
                    game.gameName(), game.game()
            ));
        }
    }
}

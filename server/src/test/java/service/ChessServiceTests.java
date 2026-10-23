
package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChessServiceTests {
    private final UserDAO users = new MemoryUserDAO();
    private final AuthDAO auths = new MemoryAuthDAO();
    private final GameDAO games = new MemoryGameDAO();

    private final UserService us = new UserService(users, auths);
    private final GameService gs = new GameService(games, us);

    private String register() {
        return us.register(
                new UserData("alice", "pass", "a@example.com")
        ).authToken();
    }

    @Test
    void registerAndLogin() {
        String token = register();

        assertEquals("alice", us.authenticate(token));
        assertEquals("alice", us.login("alice", "pass").username());

        assertThrows(ServiceException.class, () ->
                us.register(new UserData("alice", "again", "b@example.com"))
        );

        assertThrows(ServiceException.class, () ->
                us.register(new UserData("", "pass", "email"))
        );
    }

    @Test
    void logout() {
        String token = register();
        us.logout(token);

        assertThrows(ServiceException.class, () ->
                us.authenticate(token)
        );
    }

    @Test
    void invalidLogin() {
        register();

        assertThrows(ServiceException.class, () ->
                us.login("alice", "wrong")
        );

        assertThrows(ServiceException.class, () ->
                us.login("nobody", "pass")
        );
    }

    @Test
    void createListJoin() {
        String token = register();
        int id = gs.create(token, "First game").gameID();

        assertEquals(1, gs.list(token).games().size());

        gs.join(token, "WHITE", id);

        assertEquals("alice", games.getGame(id).whiteUsername());

        assertThrows(ServiceException.class, () ->
                gs.join(token, "WHITE", id)
        );

        assertThrows(ServiceException.class, () ->
                gs.join(token, "BLUE", id)
        );
    }

    @Test
    void blackJoinAndClear() {
        String token = register();
        int id = gs.create(token, "Second game").gameID();

        gs.join(token, "BLACK", id);

        assertEquals("alice", games.getGame(id).blackUsername());

        users.clear();
        auths.clear();
        games.clear();

        assertEquals(0, games.listGames().size());
    }
}

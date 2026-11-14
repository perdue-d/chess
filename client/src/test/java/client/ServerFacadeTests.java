package client;

import chess.ChessGame.TeamColor;
import client.ServerFacade;
import client.ServerFacade.RequestException;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    static void startServer() {
        server = new Server();
        facade = new ServerFacade(server.run(0));
    }

    @BeforeEach
    void clearDatabase() {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void registerSucceeds() {
        var auth = facade.register("alice", "password", "alice@example.com");
        assertEquals("alice", auth.username());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void duplicateRegistrationFails() {
        registerAlice();
        var error = assertThrows(RequestException.class, this::registerAlice);
        assertEquals(403, error.status());
    }

    @Test
    void loginSucceeds() {
        registerAlice();
        var auth = facade.login("alice", "password");
        assertEquals("alice", auth.username());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void wrongPasswordFails() {
        registerAlice();
        var error = assertThrows(RequestException.class, () -> facade.login("alice", "wrong"));
        assertEquals(401, error.status());
    }

    @Test
    void logoutInvalidatesToken() {
        String token = registerAlice();
        facade.logout(token);
        var error = assertThrows(RequestException.class, () -> facade.listGames(token));
        assertEquals(401, error.status());
    }

    @Test
    void logoutRejectsInvalidToken() {
        var error = assertThrows(RequestException.class, () -> facade.logout("invalid"));
        assertEquals(401, error.status());
    }

    @Test
    void createLeavesBothSeatsOpen() {
        String token = registerAlice();
        int id = facade.createGame(token, "Friday Chess");
        var games = facade.listGames(token);

        assertEquals(1, games.size());
        var game = games.getFirst();
        assertEquals(id, game.gameID());
        assertEquals("Friday Chess", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void createRejectsBlankName() {
        String token = registerAlice();
        var error = assertThrows(RequestException.class, () -> facade.createGame(token, " "));
        assertEquals(400, error.status());
    }

    @Test
    void listReturnsEmptyWhenThereAreNoGames() {
        assertTrue(facade.listGames(registerAlice()).isEmpty());
    }

    @Test
    void listRejectsInvalidToken() {
        var error = assertThrows(RequestException.class, () -> facade.listGames("invalid"));
        assertEquals(401, error.status());
    }

    @Test
    void joinAssignsRequestedSeat() {
        String token = registerAlice();
        int id = facade.createGame(token, "Friday Chess");
        facade.joinGame(token, id, TeamColor.BLACK);

        var game = facade.listGames(token).getFirst();
        assertEquals("alice", game.blackUsername());
        assertNull(game.whiteUsername());
    }

    @Test
    void joinRejectsOccupiedSeat() {
        String alice = registerAlice();
        String bob = facade.register("bob", "password", "bob@example.com").authToken();
        int id = facade.createGame(alice, "Friday Chess");
        facade.joinGame(alice, id, TeamColor.WHITE);

        var error = assertThrows(RequestException.class, () -> facade.joinGame(bob, id, TeamColor.WHITE));
        assertEquals(403, error.status());
    }

    @Test
    void clearRemovesUsersAndGames() {
        String token = registerAlice();
        facade.createGame(token, "Friday Chess");
        facade.clear();

        var error = assertThrows(RequestException.class, () -> facade.login("alice", "password"));
        assertEquals(401, error.status());
        assertTrue(facade.listGames(registerAlice()).isEmpty());
    }

    @Test
    void clearFailsWhenTheServerIsUnreachable() {
        assertThrows(RequestException.class, () -> new ServerFacade(0).clear());
    }

    private String registerAlice() {
        return facade.register("alice", "password", "alice@example.com").authToken();
    }

    @Test
    void createRejectsInvalidToken() {
        var error = assertThrows(RequestException.class,
                () -> facade.createGame("invalid", "Friday Chess"));

        assertEquals(401, error.status());
    }
    @Test
    void joinRejectsMissingGame() {
        String token = registerAlice();

        var error = assertThrows(RequestException.class,
                () -> facade.joinGame(token, -1, TeamColor.WHITE));

        assertEquals(400, error.status());
    }
    @Test
    void joinRequiresAColor() {
        String token = registerAlice();
        int gameID = facade.createGame(token, "Friday Chess");

        var error = assertThrows(RequestException.class,
                () -> facade.joinGame(token, gameID, null));

        assertEquals(400, error.status());
    }
}

package dataaccess.sql;

import dataaccess.UserDAO;
import model.UserData;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTests {

    private UserDAO.MySqlDataAccess database;

    @BeforeEach
    public void setup() {
        database = new UserDAO.MySqlDataAccess();
        database.clear();
    }

    @Test
    public void createUserTest() {
        UserData user = new UserData(
                "player1",
                "password123",
                "player1@test.com"
        );

        database.createUser(user);

        UserData result = database.getUser("player1");

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertEquals("player1@test.com", result.email());
    }

    @Test
    public void getMissingUserTest() {
        UserData result = database.getUser("missingUser");

        assertNull(result);
    }

    @Test
    public void duplicateUserTest() {
        UserData user = new UserData(
                "player1",
                "password123",
                "player1@test.com"
        );

        database.createUser(user);

        assertThrows(
                IllegalStateException.class,
                () -> database.createUser(user)
        );
    }

    @Test
    public void createAuthTest() {
        AuthData auth = new AuthData(
                "token123",
                "player1"
        );

        database.createAuth(auth);

        AuthData result = database.getAuth("token123");

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertEquals("token123", result.authToken());
    }

    @Test
    public void getMissingAuthTest() {
        AuthData result = database.getAuth("missingToken");

        assertNull(result);
    }

    @Test
    public void deleteAuthTest() {
        AuthData auth = new AuthData(
                "token123",
                "player1"
        );

        database.createAuth(auth);
        database.deleteAuth("token123");

        assertNull(database.getAuth("token123"));
    }

    @Test
    public void createGameTest() {
        int id = database.createGame("My Chess Game");

        GameData result = database.getGame(id);

        assertTrue(id > 0);
        assertNotNull(result);
        assertEquals("My Chess Game", result.gameName());
        assertNotNull(result.game());
    }

    @Test
    public void getMissingGameTest() {
        GameData result = database.getGame(-1);

        assertNull(result);
    }

    @Test
    public void listGamesTest() {
        database.createGame("Game One");
        database.createGame("Game Two");

        Collection<GameData> games = database.listGames();

        assertEquals(2, games.size());
    }

    @Test
    public void listEmptyGamesTest() {
        Collection<GameData> games = database.listGames();

        assertTrue(games.isEmpty());
    }

    @Test
    public void updateGameTest() {
        int id = database.createGame("Original Game");

        GameData updated = new GameData(
                id,
                "player1",
                "player2",
                "Updated Game",
                new ChessGame()
        );

        database.updateGame(updated);

        GameData result = database.getGame(id);

        assertNotNull(result);
        assertEquals("player1", result.whiteUsername());
        assertEquals("player2", result.blackUsername());
        assertEquals("Updated Game", result.gameName());
        assertNotNull(result.game());
    }

    @Test
    public void clearDatabaseTest() {
        database.createUser(new UserData(
                "player1",
                "password123",
                "player1@test.com"
        ));

        database.createAuth(new AuthData(
                "token123",
                "player1"
        ));

        database.createGame("Game One");

        database.clear();

        assertNull(database.getUser("player1"));
        assertNull(database.getAuth("token123"));
        assertTrue(database.listGames().isEmpty());
    }
}


package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlDataAccess implements UserDAO, AuthDAO, GameDAO {

    private final Gson gson = new Gson();

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                game TEXT NOT NULL,
                PRIMARY KEY (gameID)
            )
            """
    };

    public MySqlDataAccess() {
        configureDatabase();
    }

    // USERS

    @Override
    public void createUser(UserData user) {
        String sql = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;

        executeUpdate(
                sql,
                user.username(),
                user.password(),
                user.email()
        );
    }

    @Override
    public UserData getUser(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to get user", e);
        }

        return null;
    }

    // AUTHENTICATION

    @Override
    public void createAuth(AuthData auth) {
        String sql = """
                INSERT INTO auth (authToken, username)
                VALUES (?, ?)
                """;

        executeUpdate(
                sql,
                auth.authToken(),
                auth.username()
        );
    }

    @Override
    public AuthData getAuth(String token) {
        String sql = "SELECT * FROM auth WHERE authToken=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to get auth", e);
        }

        return null;
    }

    @Override
    public void deleteAuth(String token) {
        executeUpdate(
                "DELETE FROM auth WHERE authToken=?",
                token
        );
    }

    // GAMES

    @Override
    public int createGame(String name) {
        ChessGame game = new ChessGame();
        String json = gson.toJson(game);

        String sql = """
                INSERT INTO games (gameName, game)
                VALUES (?, ?)
                """;

        return executeUpdate(sql, name, json);
    }

    @Override
    public GameData getGame(int id) {
        String sql = "SELECT * FROM games WHERE gameID=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readGame(rs);
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to get game", e);
        }

        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        Collection<GameData> games = new ArrayList<>();

        String sql = "SELECT * FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                games.add(readGame(rs));
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to list games", e);
        }

        return games;
    }

    @Override
    public void updateGame(GameData game) {
        String sql = """
                UPDATE games
                SET whiteUsername=?,
                    blackUsername=?,
                    gameName=?,
                    game=?
                WHERE gameID=?
                """;

        executeUpdate(
                sql,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                gson.toJson(game.game()),
                game.gameID()
        );
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        String json = rs.getString("game");

        ChessGame game = gson.fromJson(
                json,
                ChessGame.class
        );

        return new GameData(
                rs.getInt("gameID"),
                rs.getString("whiteUsername"),
                rs.getString("blackUsername"),
                rs.getString("gameName"),
                game
        );
    }

    // CLEAR DATABASE

    @Override
    public void clear() {
        executeUpdate("DELETE FROM auth");
        executeUpdate("DELETE FROM games");
        executeUpdate("DELETE FROM users");
    }

    // SQL HELPER

    private int executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     RETURN_GENERATED_KEYS
             )) {

            for (int i = 0; i < params.length; i++) {
                Object param = params[i];

                if (param instanceof String value) {
                    ps.setString(i + 1, value);
                } else if (param instanceof Integer value) {
                    ps.setInt(i + 1, value);
                } else if (param == null) {
                    ps.setNull(i + 1, Types.NULL);
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported parameter type"
                    );
                }
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            return 0;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to update database: " + sql, e
            );
        }
    }

    // DATABASE SETUP

    private void configureDatabase() {
        try {
            DatabaseManager.createDatabase();

            try (Connection conn = DatabaseManager.getConnection()) {
                for (String sql : createStatements) {
                    try (PreparedStatement ps =
                                 conn.prepareStatement(sql)) {
                        ps.executeUpdate();
                    }
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to configure database", e
            );
        }
    }
}

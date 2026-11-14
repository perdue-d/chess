package client;

import chess.ChessGame.TeamColor;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import model.AuthData;
import model.UserData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class ServerFacade {
    private final String baseUrl;
    private final Gson gson = new Gson();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public ServerFacade(int port) {
        baseUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) {
        return send("POST", "/user", null,
                new UserData(username, password, email), AuthData.class);
    }

    public AuthData login(String username, String password) {
        return send("POST", "/session", null,
                new LoginRequest(username, password), AuthData.class);
    }

    public void logout(String token) {
        send("DELETE", "/session", token, null, Void.class);
    }

    public int createGame(String token, String gameName) {
        return send("POST", "/game", token,
                new CreateRequest(gameName), CreateResult.class).gameID();
    }

    public List<GameSummary> listGames(String token) {
        return send("GET", "/game", token, null, ListResult.class).games();
    }

    public void joinGame(String token, int gameID, TeamColor color) {
        send("PUT", "/game", token, new JoinRequest(color, gameID), Void.class);
    }

    void clear() {
        send("DELETE", "/db", null, null, Void.class);
    }

    private <T> T send(String method, String path, String token, Object body, Class<T> type) {
        var request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(5));
        if (token != null) {
            request.header("Authorization", token);
        }
        var publisher = HttpRequest.BodyPublishers.noBody();
        if (body != null) {
            request.header("Content-Type", "application/json");
            publisher = HttpRequest.BodyPublishers.ofString(gson.toJson(body));
        }

        try {
            var response = http.send(request.method(method, publisher).build(),
                    HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new RequestException(status, errorMessage(status, path));
            }
            if (type == Void.class) {
                return null;
            }
            T result = gson.fromJson(response.body(), type);
            if (result == null) {
                throw new RequestException(0, "The chess server returned an empty response.");
            }
            return result;
        } catch (IOException e) {
            throw new RequestException(0, "Could not reach the chess server. Check that it is running.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RequestException(0, "The request was interrupted.");
        } catch (JsonParseException e) {
            throw new RequestException(0, "The chess server returned an unreadable response.");
        }
    }

    private String errorMessage(int status, String path) {
        return switch (status) {
            case 400 -> "The server rejected that request. Check your input.";
            case 401 -> "Check your login details or sign in again.";
            case 403 -> path.equals("/user")
                    ? "That username is already taken."
                    : "That seat is already taken.";
            default -> "The chess server could not complete the request.";
        };
    }

    public record GameSummary(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    private record LoginRequest(String username, String password) {}
    private record CreateRequest(String gameName) {}
    private record CreateResult(int gameID) {}
    private record ListResult(List<GameSummary> games) {}
    private record JoinRequest(TeamColor playerColor, int gameID) {}

    public static class RequestException extends RuntimeException {
        private final int status;

        RequestException(int status, String message) {
            super(message);
            this.status = status;
        }

        int status() {
            return status;
        }
    }
}
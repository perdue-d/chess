package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.*;
import io.javalin.Javalin;
import model.UserData;
import service.*;
import java.util.Map;

public class Server {
    private final Javalin javalin;

    private final UserDAO users = new MemoryUserDAO();
    private final AuthDAO auths = new MemoryAuthDAO();
    private final GameDAO games = new MemoryGameDAO();

    private final UserService userService =
            new UserService(users, auths);

    private final GameService gameService =
            new GameService(games, userService);

    private final Gson gson = new Gson();

    private record LoginRequest(String username, String password) {}
    private record CreateRequest(String gameName) {}
    private record JoinRequest(String playerColor, Integer gameID) {}

    public Server() {
        javalin = Javalin.create(config ->
                config.staticFiles.add("web")
        );

        javalin.exception(ServiceException.class, (e, ctx) -> {
            ctx.status(e.status());
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        });

        javalin.exception(JsonSyntaxException.class, (e, ctx) -> {
            ctx.status(400);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
        });

        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of("message", "Error: internal server error")));
        });

        // Clear database
        javalin.delete("/db", ctx -> {
            users.clear();
            auths.clear();
            games.clear();
            ctx.contentType("application/json");
            ctx.result("{}");
        });

        // Register
        javalin.post("/user", ctx -> {
            UserData user = gson.fromJson(ctx.body(), UserData.class);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(userService.register(user)));
        });

        // Login
        javalin.post("/session", ctx -> {
            LoginRequest request =
                    gson.fromJson(ctx.body(), LoginRequest.class);

            var result = userService.login(
                    request == null ? null : request.username(),
                    request == null ? null : request.password()
            );

            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        // Logout
        javalin.delete("/session", ctx -> {
            userService.logout(ctx.header("authorization"));
            ctx.contentType("application/json");
            ctx.result("{}");
        });

        // List games
        javalin.get("/game", ctx -> {
            var result = gameService.list(ctx.header("authorization"));
            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        // Create game
        javalin.post("/game", ctx -> {
            CreateRequest request =
                    gson.fromJson(ctx.body(), CreateRequest.class);

            var result = gameService.create(
                    ctx.header("authorization"),
                    request == null ? null : request.gameName()
            );

            ctx.contentType("application/json");
            ctx.result(gson.toJson(result));
        });

        // Join game
        javalin.put("/game", ctx -> {
            JoinRequest request =
                    gson.fromJson(ctx.body(), JoinRequest.class);

            gameService.join(
                    ctx.header("authorization"),
                    request == null ? null : request.playerColor(),
                    request == null ? null : request.gameID()
            );

            ctx.contentType("application/json");
            ctx.result("{}");
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}

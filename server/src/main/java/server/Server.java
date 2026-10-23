
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
            ctx.status(e.status())
                    .json(Map.of("message", e.getMessage()));
        });

        javalin.exception(JsonSyntaxException.class, (e, ctx) -> {
            ctx.status(400)
                    .json(Map.of("message", "Error: bad request"));
        });

        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500)
                    .json(Map.of("message", "Error: " + e.getMessage()));
        });

        javalin.delete("/db", ctx -> {
            users.clear();
            auths.clear();
            games.clear();
            ctx.json(Map.of());
        });

        javalin.post("/user", ctx -> {
            UserData user = gson.fromJson(ctx.body(), UserData.class);
            ctx.json(userService.register(user));
        });

        javalin.post("/session", ctx -> {
            LoginRequest request =
                    gson.fromJson(ctx.body(), LoginRequest.class);

            ctx.json(userService.login(
                    request == null ? null : request.username(),
                    request == null ? null : request.password()
            ));
        });

        javalin.delete("/session", ctx -> {
            userService.logout(ctx.header("authorization"));
            ctx.json(Map.of());
        });

        javalin.get("/game", ctx -> {
            ctx.json(gameService.list(ctx.header("authorization")));
        });

        javalin.post("/game", ctx -> {
            CreateRequest request =
                    gson.fromJson(ctx.body(), CreateRequest.class);

            ctx.json(gameService.create(
                    ctx.header("authorization"),
                    request == null ? null : request.gameName()
            ));
        });

        javalin.put("/game", ctx -> {
            JoinRequest request =
                    gson.fromJson(ctx.body(), JoinRequest.class);

            gameService.join(
                    ctx.header("authorization"),
                    request == null ? null : request.playerColor(),
                    request == null ? null : request.gameID()
            );

            ctx.json(Map.of());
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

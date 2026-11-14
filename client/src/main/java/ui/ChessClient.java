package ui;

import chess.ChessGame.TeamColor;
import client.ServerFacade;
import client.ServerFacade.GameSummary;
import client.ServerFacade.RequestException;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private String token;
    private List<GameSummary> listedGames = List.of();

    public ChessClient(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        var input = new Scanner(System.in);
        System.out.println("Chess. Type help for commands.");

        while (true) {
            System.out.print(token == null ? "[SIGNED OUT] > " : "[SIGNED IN] > ");
            if (!input.hasNextLine()) {
                return;
            }

            String line = input.nextLine().strip();
            if (line.isEmpty()) {
                continue;
            }

            String[] words = line.split("\\s+");
            String command = words[0].toLowerCase(Locale.ROOT);
            try {
                if (command.equals("quit")) {
                    requireArgs(words, 1, "quit");
                    return;
                }
                if (command.equals("help")) {
                    requireArgs(words, 1, "help");
                    printHelp();
                } else if (token == null) {
                    prelogin(command, words);
                } else {
                    postlogin(command, words, line);
                }
            } catch (RequestException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void prelogin(String command, String[] words) {
        switch (command) {
            case "login" -> {
                requireArgs(words, 3, "login <username> <password>");
                token = server.login(words[1], words[2]).authToken();
            }
            case "register" -> {
                requireArgs(words, 4, "register <username> <password> <email>");
                token = server.register(words[1], words[2], words[3]).authToken();
            }
            default -> throw new IllegalArgumentException("Unknown command. Type help.");
        }
        System.out.println("Signed in as " + words[1] + ".");
    }

    private void postlogin(String command, String[] words, String line) {
        switch (command) {
            case "logout" -> {
                requireArgs(words, 1, "logout");
                server.logout(token);
                token = null;
                listedGames = List.of();
                System.out.println("Signed out.");
            }
            case "create" -> {
                if (words.length < 2) {
                    throw new IllegalArgumentException("Usage: create <game name>");
                }
                server.createGame(token, line.substring(words[0].length()).strip());
                System.out.println("Game created. Type list to see your games.");
            }
            case "list" -> {
                requireArgs(words, 1, "list");
                listGames();
            }
            case "play" -> {
                requireArgs(words, 3, "play <number> <white|black>");
                play(words[1], words[2]);
            }
            case "observe" -> {
                requireArgs(words, 2, "observe <number>");
                var game = selectGame(words[1]);
                System.out.println("Observing " + game.gameName() + ".");
                BoardRenderer.draw(TeamColor.WHITE);
            }
            default -> throw new IllegalArgumentException("Unknown command. Type help.");
        }
    }

    private void listGames() {
        listedGames = server.listGames(token);
        if (listedGames.isEmpty()) {
            System.out.println("There are no games yet.");
        }

        for (int i = 0; i < listedGames.size(); i++) {
            var game = listedGames.get(i);
            System.out.printf("%d. %s | White: %s | Black: %s%n",
                    i + 1, game.gameName(), player(game.whiteUsername()), player(game.blackUsername()));
        }
    }

    private void play(String number, String color) {
        var game = selectGame(number);
        TeamColor side = switch (color.toLowerCase(Locale.ROOT)) {
            case "white" -> TeamColor.WHITE;
            case "black" -> TeamColor.BLACK;
            default -> throw new IllegalArgumentException("Choose white or black.");
        };

        server.joinGame(token, game.gameID(), side);
        System.out.printf("Joined %s as %s.%n", game.gameName(), side.name().toLowerCase(Locale.ROOT));
        BoardRenderer.draw(side);
    }

    private GameSummary selectGame(String text) {
        int number;
        try {
            number = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a whole game number from the list.");
        }

        if (number < 1 || number > listedGames.size()) {
            throw new IllegalArgumentException("Type list and choose one of the displayed game numbers.");
        }
        return listedGames.get(number - 1);
    }

    private static String player(String username) {
        return username == null ? "Open" : username;
    }

    private static void requireArgs(String[] words, int expected, String usage) {
        if (words.length != expected) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }

    private void printHelp() {
        if (token == null) {
            System.out.println("register <username> <password> <email>\nlogin <username> <password>");
        } else {
            System.out.println("create <game name>\nlist\nplay <number> <white|black>\nobserve <number>\nlogout");
        }
        System.out.println("help\nquit");
    }
}
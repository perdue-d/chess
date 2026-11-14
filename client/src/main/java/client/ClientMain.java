package client;

import ui.ChessClient;

public class ClientMain {
    public static void main(String[] args) {
        new ChessClient(new ServerFacade(8080)).run();
    }
}
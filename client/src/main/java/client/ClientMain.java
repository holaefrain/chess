package client;

import ui.ChessClient;
import ui.Repl;

public class ClientMain {
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        ServerFacade facade = new ServerFacade(SERVER_PORT);
        new Repl(new ChessClient(facade)).run();
    }
}

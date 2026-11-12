package ui;

import client.ResponseException;
import client.ServerFacade;
import model.AuthData;

import java.util.Arrays;

//turns one line of user input into a server call and the text to show for it
public class ChessClient {

    public enum State {
        SIGNED_OUT,
        SIGNED_IN,
        QUIT
    }

    private static final String UNKNOWN_COMMAND = "Unknown command. Type help to see what you can do.";

    private final ServerFacade facade;
    private State state = State.SIGNED_OUT;
    private String authToken;

    public ChessClient(ServerFacade facade) {
        this.facade = facade;
    }

    public State state() {
        return state;
    }
}

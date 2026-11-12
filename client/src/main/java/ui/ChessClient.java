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

    //server failures come back as readable text instead of an exception
    public String eval(String input) {
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        try {
            if (state == State.SIGNED_OUT) {
                return evalSignedOut(command, params);
            }
            return evalSignedIn(command, params);
        } catch (ResponseException ex) {
            return describe(ex);
        }
    }

    //a rejected token means the server no longer knows this login
    private String endSession() {
        signOut();
        return "Your session has ended. Please log in again.";
    }

    private String evalSignedOut(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> signedOutHelp();
            case "quit" -> quit();
            case "login" -> login(params);
            case "register" -> register(params);
            default -> UNKNOWN_COMMAND;
        };
    }

    private String evalSignedIn(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> signedInHelp();
            case "quit" -> quit();
            case "logout" -> logout();
            default -> UNKNOWN_COMMAND;
        };
    }

    private String register(String[] params) throws ResponseException {
        if (params.length != 3) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        }
        signIn(facade.register(params[0], params[1], params[2]));
        return "Registered and logged in as " + params[0] + ".";
    }

    private String login(String[] params) throws ResponseException {
        if (params.length != 2) {
            return "Usage: login <USERNAME> <PASSWORD>";
        }
        signIn(facade.login(params[0], params[1]));
        return "Logged in as " + params[0] + ".";
    }

    private void signIn(AuthData auth) {
        authToken = auth.authToken();
        state = State.SIGNED_IN;
    }

    private String logout() throws ResponseException {
        facade.logout(authToken);
        signOut();
        return "Logged out.";
    }

    private void signOut() {
        authToken = null;
        state = State.SIGNED_OUT;
    }

    private String quit() {
        state = State.QUIT;
        return "Goodbye!";
    }
}

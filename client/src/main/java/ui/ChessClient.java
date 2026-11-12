package ui;

import client.ResponseException;
import client.ServerFacade;
import model.AuthData;
import model.GameData;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

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

    //games in the order last shown; list number N is index N - 1
    private List<GameData> games = new ArrayList<>();

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

    //the same failure means different things before and after login
    private String describe(ResponseException ex) {
        boolean signedOut = state == State.SIGNED_OUT;
        return switch (ex.reason()) {
            case UNAUTHORIZED -> signedOut ? "Wrong username or password." : endSession();
            case ALREADY_TAKEN -> signedOut ? "That username is already taken." : "That color is already taken.";
            default -> ex.getMessage();
        };
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
            case "create" -> create(params);
            case "list" -> list();
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
        games = new ArrayList<>();
        state = State.SIGNED_OUT;
    }

    private String quit() {
        state = State.QUIT;
        return "Goodbye!";
    }

    private String create(String[] params) throws ResponseException {
        if (params.length == 0) {
            return "Usage: create <NAME>";
        }
        String gameName = String.join(" ", params);
        facade.createGame(authToken, gameName);
        return "Created game " + gameName + ". Use list to see it.";
    }

    private String list() throws ResponseException {
        games = facade.listGames(authToken);
        if (games.isEmpty()) {
            return "No games yet. Use create <NAME> to start one.";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            out.append(i + 1).append(". ").append(game.gameName())
                    .append(" | white: ").append(seat(game.whiteUsername()))
                    .append(" | black: ").append(seat(game.blackUsername()))
                    .append("\n");
        }
        return out.toString().stripTrailing();
    }

    private static String seat(String username) {
        return username == null ? "(open)" : username;
    }

    private static String signedOutHelp() {
        return String.join("\n",
                helpLine("register <USERNAME> <PASSWORD> <EMAIL>", "create an account"),
                helpLine("login <USERNAME> <PASSWORD>", "log in to play chess"),
                helpLine("quit", "exit the program"),
                helpLine("help", "show these commands"));
    }

    private static String signedInHelp() {
        return String.join("\n",
                helpLine("create <NAME>", "create a new game"),
                helpLine("list", "show all games"),
                helpLine("logout", "log out of your account"),
                helpLine("quit", "exit the program"),
                helpLine("help", "show these commands"));
    }

    private static String helpLine(String command, String description) {
        return EscapeSequences.SET_TEXT_COLOR_BLUE + command
                + EscapeSequences.RESET_TEXT_COLOR + " - " + description;
    }
}

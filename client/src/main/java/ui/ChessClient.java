package ui;

import chess.ChessGame;
import client.ResponseException;
import client.ServerFacade;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//turns one line of user input into a server call and the text to show for it
public class ChessClient {

    public enum State {
        SIGNED_OUT,
        SIGNED_IN,
        QUIT
    }

    private static final String UNKNOWN_COMMAND = "Unknown command. Type help to see what you can do.";

    private static final String NO_SUCH_GAME = "No game with that number. Use list to see the current games.";

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
            case "join" -> join(params);
            case "observe" -> observe(params);
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

    private String join(String[] params) throws ResponseException {
        if (params.length != 2) {
            return "Usage: join <NUMBER> <WHITE|BLACK>";
        }
        GameData game = gameByNumber(params[0]);
        if (game == null) {
            return NO_SUCH_GAME;
        }
        ChessGame.TeamColor color = parseColor(params[1]);
        if (color == null) {
            return "Color must be WHITE or BLACK.";
        }
        facade.joinGame(authToken, game.gameID(), color);
        return "Joined " + game.gameName() + " as " + color + ".\n" + drawBoard(game, color);
    }

    //observing needs no server call until phase 6
    private String observe(String[] params) {
        if (params.length != 1) {
            return "Usage: observe <NUMBER>";
        }
        GameData game = gameByNumber(params[0]);
        if (game == null) {
            return NO_SUCH_GAME;
        }
        return "Observing " + game.gameName() + ".\n" + drawBoard(game, ChessGame.TeamColor.WHITE);
    }

    //null when the text is not a number from the last list
    private GameData gameByNumber(String text) {
        try {
            int number = Integer.parseInt(text);
            boolean listed = number >= 1 && number <= games.size();
            return listed ? games.get(number - 1) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static ChessGame.TeamColor parseColor(String text) {
        return switch (text.toUpperCase()) {
            case "WHITE" -> ChessGame.TeamColor.WHITE;
            case "BLACK" -> ChessGame.TeamColor.BLACK;
            default -> null;
        };
    }

    private static String drawBoard(GameData game, ChessGame.TeamColor perspective) {
        return BoardRenderer.render(game.game().getBoard(), perspective);
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

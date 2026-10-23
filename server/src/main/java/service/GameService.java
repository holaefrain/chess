package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ListGamesResult listGames(String authToken)
            throws UnauthorizedException, DataAccessException {
        authenticate(authToken);
        return new ListGamesResult(gameDAO.listGames());
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        authenticate(authToken);

        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException();
        }

        // DAO picks the ID
        int gameID = gameDAO.createGame(request.gameName());
        return new CreateGameResult(gameID);
    }

    public void joinGame(String authToken, JoinGameRequest request)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {
        // Need username from token to put into game
        AuthData auth = authenticate(authToken);

        if (request == null || request.gameID() == null) {
            throw new BadRequestException();
        }

        // Turns white/black into the enum; anything else = 400
        ChessGame.TeamColor color = parseColor(request.playerColor());

        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException();
        }

        // Records are immutable
        GameData updated;
        if (color == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException();
            }
            updated = new GameData(game.gameID(), auth.username(), game.blackUsername(),
                    game.gameName(), game.game());
        } else {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException();
            }
            updated = new GameData(game.gameID(), game.whiteUsername(), auth.username(),
                    game.gameName(), game.game());
        }

        gameDAO.updateGame(updated);
    }

    // Looks up token, returns AuthData so callers can read username
    private AuthData authenticate(String authToken)
            throws UnauthorizedException, DataAccessException {
        AuthData auth = authToken == null ? null : authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException();
        }
        return auth;
    }

    private static ChessGame.TeamColor parseColor(String playerColor) throws BadRequestException {
        if ("WHITE".equals(playerColor)) {
            return ChessGame.TeamColor.WHITE;
        }
        if ("BLACK".equals(playerColor)) {
            return ChessGame.TeamColor.BLACK;
        }
        throw new BadRequestException();
    }
}

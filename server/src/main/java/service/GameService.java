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

    public ListGamesResult listGames(String authToken) throws UnauthorizedException, DataAccessException {
        authenticate(authToken);
        return new ListGamesResult(gameDAO.listGames());
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws UnauthorizedException, BadRequestException, DataAccessException {
        authenticate(authToken);

        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException();
        }

        //DAO picks the ID
        int gameID = gameDAO.createGame(request.gameName());
        return new CreateGameResult(gameID);
    }

}
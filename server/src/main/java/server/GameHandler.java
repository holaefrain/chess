package server;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.CreateGameRequest;
import service.GameService;
import service.JoinGameRequest;
import service.UnauthorizedException;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }
}
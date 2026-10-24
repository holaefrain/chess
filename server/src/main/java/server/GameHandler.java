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

    // GET /game
    public void listGames(Context ctx) throws UnauthorizedException, DataAccessException {
        HandlerUtil.send(ctx, gameService.listGames(HandlerUtil.authToken(ctx)));
    }

    // POST /game
    public void createGame(Context ctx)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        CreateGameRequest request = HandlerUtil.readBody(ctx, CreateGameRequest.class);
        HandlerUtil.send(ctx, gameService.createGame(HandlerUtil.authToken(ctx), request));
    }

    // PUT /game
    public void joinGame(Context ctx)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {
        JoinGameRequest request = HandlerUtil.readBody(ctx, JoinGameRequest.class);
        gameService.joinGame(HandlerUtil.authToken(ctx), request);
        HandlerUtil.sendEmpty(ctx);
    }
}

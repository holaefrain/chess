package server;

import com.google.gson.JsonSyntaxException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.sql.MySqlAuthDAO;
import dataaccess.sql.MySqlGameDAO;
import dataaccess.sql.MySqlUserDAO;
import io.javalin.*;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.ClearService;
import service.GameService;
import service.UnauthorizedException;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        registerEndpoints();
        registerExceptionHandlers();
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    // One shared set of DAOs so every service sees the same data
    private void registerEndpoints() {
        UserDAO userDAO;
        GameDAO gameDAO;
        AuthDAO authDAO;
        try {
            userDAO = new MySqlUserDAO();
            gameDAO = new MySqlGameDAO();
            authDAO = new MySqlAuthDAO();
        } catch (DataAccessException ex) {
            // Every endpoint needs the tables, so fail at startup rather than on each request
            throw new IllegalStateException("Error: unable to set up database", ex);
        }

        ClearHandler clearHandler = new ClearHandler(new ClearService(userDAO, gameDAO, authDAO));
        UserHandler userHandler = new UserHandler(new UserService(userDAO, authDAO));
        GameHandler gameHandler = new GameHandler(new GameService(gameDAO, authDAO));

        javalin.delete("/db", clearHandler::clear);
        javalin.post("/user", userHandler::register);
        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);
        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);
    }

    // Javalin picks the most specific match, so Exception only catches what's left
    private void registerExceptionHandlers() {
        javalin.exception(BadRequestException.class,
                (e, ctx) -> HandlerUtil.sendError(ctx, 400, e.getMessage()));
        javalin.exception(JsonSyntaxException.class,
                (e, ctx) -> HandlerUtil.sendError(ctx, 400, "Error: bad request"));
        javalin.exception(UnauthorizedException.class,
                (e, ctx) -> HandlerUtil.sendError(ctx, 401, e.getMessage()));
        javalin.exception(AlreadyTakenException.class,
                (e, ctx) -> HandlerUtil.sendError(ctx, 403, e.getMessage()));
        javalin.exception(Exception.class,
                (e, ctx) -> HandlerUtil.sendError(ctx, 500, "Error: " + e.getMessage()));
    }
}

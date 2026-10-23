package server;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.LoginRequest;
import service.RegisterRequest;
import service.UnauthorizedException;
import service.UserService;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    // POST /user
    public void register(Context ctx)
            throws BadRequestException, AlreadyTakenException, DataAccessException {
        RegisterRequest request = HandlerUtil.readBody(ctx, RegisterRequest.class);
        HandlerUtil.send(ctx, userService.register(request));
    }

    // POST /session
    public void login(Context ctx)
            throws BadRequestException, UnauthorizedException, DataAccessException {
        LoginRequest request = HandlerUtil.readBody(ctx, LoginRequest.class);
        HandlerUtil.send(ctx, userService.login(request));
    }
}

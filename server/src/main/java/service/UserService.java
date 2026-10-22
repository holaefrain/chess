package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    // DAOs are injected
    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthResult register(RegisterRequest request)
            throws BadRequestException, AlreadyTakenException, DataAccessException {
        if (request == null
                || isBlank(request.username())
                || isBlank(request.password())
                || isBlank(request.email())) {
            throw new BadRequestException();
        }

        // Unique username
        if (userDAO.getUser(request.username()) != null) {
            throw new AlreadyTakenException();
        }

        userDAO.createUser(new UserData(request.username(), request.password(), request.email()));

        // Registering logs the user in
        return createSession(request.username());
    }

    public AuthResult login(LoginRequest request)
            throws BadRequestException, UnauthorizedException, DataAccessException {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new BadRequestException();
        }

        UserData user = userDAO.getUser(request.username());

        // Unknown user and wrong password both get the same 401
        if (user == null || !user.password().equals(request.password())) {
            throw new UnauthorizedException();
        }

        return createSession(request.username());
    }

    public void logout(String authToken) throws UnauthorizedException, DataAccessException {
        // Token must exist to be removed
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException();
        }

        authDAO.deleteAuth(authToken);
    }

    // Create a new token and store it; used by register and login
    private AuthResult createSession(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, username));
        return new AuthResult(username, token);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

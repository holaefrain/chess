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

    public AuthResult register(RegisterRequest request) throws BadRequestException, AlreadyTakenException, DataAccessException {
        if (request == null
            || isBlank(request.username())
            || isBlank(request.password())
            || isBlank(request.email())) {
                throw new BadRequestException();
            }

            //Unique Username
            if (userDAO.getUser(request.username()) != null) {
                throw new AlreadyTakenException();
            }

            userDAO.createUser(new UserData(request.username(), request.password(), request.email()));

            //Registering logs the user in
            return createSession(request.username());
        }
    }
}

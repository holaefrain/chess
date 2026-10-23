package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;

import org.jupiter.api.Assertions;
import org.jupiter.api.BeforeEach;
import org.jupiter.api.Test;

public class UserServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService service;

    private final RegisterRequest validRegister = new RegisterRequest("alex", "pw" , "a@email.com");

    //fresh DAOs for every test so nothing leaks

}

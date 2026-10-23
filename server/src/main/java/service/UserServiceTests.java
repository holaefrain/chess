package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService service;

    private final RegisterRequest validRegister = new RegisterRequest("alex", "pw" , "a@email.com");

    //fresh DAOs for every test so nothing leaks
    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        service = new UserService(userDAO, authDAO);
    }

    //tests
    @Test
    void registerSuccess() throws Exception {
        AuthResult result = service.register(validRegister);

        Assertions.assertEquals("alex", result.username());
        Assertions.assertNotNull(result.authToken());
        //check the stores not just return values
        Assertions.assertNotNull(userDAO.getUser("alex"));
        Assertions.assertNotNull(authDAO.getAuth(result.authToken()));
    }

    //duplicate username
    @Test
    void registerDuplicateUsernameThrowsAlreadyTaken() throws Exception {
        service.register(validRegister);

        Assertions.assertThrows(AlreadyTakenException.class, () -> service.register(validRegister));
    }

    //missing fields
    @Test
    void registerMissingFieldThrowsBadRequest() {
        //each required field missing in order
        Assertions.assertThrows(BadRequestException.class, () -> service.register(new RegisterRequest("alex", null, "a@email.com")));
        Assertions.assertThrows(BadRequestException.class, () -> service.register(new RegisterRequest(null, "pw", "a@email.com")));
        Assertions.assertThrows(BadRequestException.class, () -> service.register(new RegisterRequest("alex", "pw", "")));
        Assertions.assertThrows(BadRequestException.class, () -> service.register(null));
    } 

    //login success
    @Test
    void loginSuccess() throws Exception {
        service.register(validRegister);

        AuthResult result = service.login(new LoginRequest("alex", "pw"));

        Assertions.assertEquals("alex", result.username());
        Assertions.assertNotNull(authDAO.getAuth(result.authToken()));
    }

    //login token
    @Test
    void loginIssuesUniqueTokens() throws Exception {
        service.register(validRegister);

        AuthResult first = service.login(new LoginRequest("alex", "pw"));
        AuthResult second = service.login(new LoginRequest("alex", "pw"));

        // two logins, two tokens, both valid
        Assertions.assertNotEquals(first.authToken(), second.authToken());
        Assertions.assertNotNull(authDAO.getAuth(first.authToken()));
        Assertions.assertNotNull(authDAO.getAuth(second.authToken()));
    }

    //wrong password
    @Test
    void loginWrongPasswordThrowsUnauthorized() throws Exception {
        service.register(validRegister);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("alex", "wrong")));
    }

    //unknown user
    @Test
    void loginUnknownUserThrowsUnauthorized() {
        Assertions.assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("nobody", "pw")));
    }

}

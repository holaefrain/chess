package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTests {
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, gameDAO, authDAO);
    }

    @Test
    void clearRemovesEverything() throws Exception {
        // Put something in all three stores
        AuthResult auth = userService.register(new RegisterRequest("alex", "pw", "a@mail.com"));
        gameService.createGame(auth.authToken(), new CreateGameRequest("game"));

        clearService.clear();

        Assertions.assertNull(userDAO.getUser("alex"));
        Assertions.assertNull(authDAO.getAuth(auth.authToken()));
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
        // Old token no longer works
        Assertions.assertThrows(UnauthorizedException.class,
                () -> gameService.listGames(auth.authToken()));
    }

    @Test
    void clearOnEmptyStoreDoesNotThrow() {
        Assertions.assertDoesNotThrow(clearService::clear);
    }

    @Test
    void registerWorksAgainAfterClear() throws Exception {
        userService.register(new RegisterRequest("alex", "pw", "a@mail.com"));

        clearService.clear();

        // Username is free again, so this must not throw AlreadyTaken
        AuthResult result = userService.register(new RegisterRequest("alex", "pw", "a@mail.com"));
        Assertions.assertEquals("alex", result.username());
    }
}
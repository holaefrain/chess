package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTests {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private GameService service;

    private static final String ALEX_TOKEN = "alex-token";
    private static final String BOB_TOKEN = "bob-token";

    @BeforeEach
    void setUp() throws Exception {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        service = new GameService(gameDAO, authDAO);

        //seed two logged-in users straight into the auth store.
        //skipping UserService keeps these tests about game logic only.
        authDAO.createAuth(new AuthData(ALEX_TOKEN, "alex"));
        authDAO.createAuth(new AuthData(BOB_TOKEN, "bob"));
    }

    //helper so join tests read clean
    private int newGame() throws Exception {
        return service.createGame(ALEX_TOKEN, new CreateGameRequest("game")).gameID();
    }

    @Test
    void createGameSuccess() throws Exception {
        CreateGameResult result = service.createGame(ALEX_TOKEN, new CreateGameRequest("first"));

        Assertions.assertTrue(result.gameID() > 0);
        GameData stored = gameDAO.getGame(result.gameID());
        Assertions.assertEquals("first", stored.gameName());
        Assertions.assertNull(stored.whiteUsername());
        Assertions.assertNull(stored.blackUsername());
        Assertions.assertNotNull(stored.game());
    }

    @Test
    void createGameBadTokenThrowsUnauthorized() {
        Assertions.assertThrows(UnauthorizedException.class,
                () -> service.createGame("bad", new CreateGameRequest("first")));
        Assertions.assertThrows(UnauthorizedException.class,
                () -> service.createGame(null, new CreateGameRequest("first")));
    }
}

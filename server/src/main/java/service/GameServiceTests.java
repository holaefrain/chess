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

    @Test
    void createGameMissingNameThrowsBadRequest() {
        Assertions.assertThrows(BadRequestException.class,
                () -> service.createGame(ALEX_TOKEN, new CreateGameRequest(null)));
        Assertions.assertThrows(BadRequestException.class,
                () -> service.createGame(ALEX_TOKEN, new CreateGameRequest("")));
        Assertions.assertThrows(BadRequestException.class,
                () -> service.createGame(ALEX_TOKEN, null));
    }

    @Test
    void listGamesEmpty() throws Exception {
        ListGamesResult result = service.listGames(ALEX_TOKEN);

        Assertions.assertTrue(result.games().isEmpty());
    }

    @Test
    void listGamesMultiple() throws Exception {
        service.createGame(ALEX_TOKEN, new CreateGameRequest("one"));
        service.createGame(ALEX_TOKEN, new CreateGameRequest("two"));
        service.createGame(BOB_TOKEN, new CreateGameRequest("three"));

        //any valid user sees every game
        ListGamesResult result = service.listGames(BOB_TOKEN);

        Assertions.assertEquals(3, result.games().size());
    }
}

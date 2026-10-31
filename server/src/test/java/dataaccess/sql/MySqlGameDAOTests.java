package dataaccess.sql;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MySqlGameDAOTests {
    private MySqlGameDAO gameDAO;

    //fresh empty table for every test
    @BeforeEach
    void setUp() throws Exception {
        gameDAO = new MySqlGameDAO();
        gameDAO.clear();
    }

    //new games start with no players and a fresh board
    @Test
    void createGameStoresNewGame() throws Exception {
        int gameID = gameDAO.createGame("first");

        GameData stored = gameDAO.getGame(gameID);
        Assertions.assertEquals("first", stored.gameName());
        Assertions.assertNull(stored.whiteUsername());
        Assertions.assertNull(stored.blackUsername());
        Assertions.assertEquals(new ChessGame(), stored.game());
    }

    @Test
    void createGameAssignsUniqueIds() throws Exception {
        int first = gameDAO.createGame("first");
        int second = gameDAO.createGame("second");

        Assertions.assertNotEquals(first, second);
    }

    //game_name is NOT NULL
    @Test
    void createGameNullNameThrows() {
        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    void getGameMissingReturnsNull() throws Exception {
        Assertions.assertNull(gameDAO.getGame(12345));
    }

    @Test
    void listGamesReturnsAllGames() throws Exception {
        gameDAO.createGame("first");
        gameDAO.createGame("second");

        List<String> names = gameDAO.listGames().stream().map(GameData::gameName).toList();
        Assertions.assertEquals(List.of("first", "second"), names);
    }

    @Test
    void listGamesEmptyReturnsEmpty() throws Exception {
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    void updateGameStoresPlayers() throws Exception {
        int gameID = gameDAO.createGame("game");
        GameData game = gameDAO.getGame(gameID);

        gameDAO.updateGame(new GameData(gameID, "alex", null, game.gameName(), game.game()));

        GameData stored = gameDAO.getGame(gameID);
        Assertions.assertEquals("alex", stored.whiteUsername());
        Assertions.assertNull(stored.blackUsername());
    }

    //board, turn, and moved-piece state all survive the JSON round trip
    @Test
    void updateGameStoresMoves() throws Exception {
        int gameID = gameDAO.createGame("game");
        ChessGame chess = gameDAO.getGame(gameID).game();
        chess.makeMove(new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null));

        gameDAO.updateGame(new GameData(gameID, "alex", "bob", "game", chess));

        ChessGame stored = gameDAO.getGame(gameID).game();
        Assertions.assertEquals(chess, stored);
        Assertions.assertEquals(ChessGame.TeamColor.BLACK, stored.getTeamTurn());
        Assertions.assertTrue(stored.getBoard().getPiece(new ChessPosition(4, 5)).hasMoved());
    }

    //MySQL reports matched rows, so a no-op update must not look like a missing game
    @Test
    void updateGameUnchangedDoesNotThrow() throws Exception {
        GameData game = gameDAO.getGame(gameDAO.createGame("game"));

        Assertions.assertDoesNotThrow(() -> gameDAO.updateGame(game));
    }

    @Test
    void updateGameMissingThrows() {
        GameData ghost = new GameData(12345, null, null, "ghost", new ChessGame());

        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.updateGame(ghost));
    }

    //ids restart at 1 like MemoryGameDAO
    @Test
    void clearRemovesGamesAndResetsIds() throws Exception {
        gameDAO.createGame("first");
        gameDAO.createGame("second");

        gameDAO.clear();

        Assertions.assertTrue(gameDAO.listGames().isEmpty());
        Assertions.assertEquals(1, gameDAO.createGame("again"));
    }
}

package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {
    //ChessGame holds only concrete classes, so plain Gson round-trips it without a type adapter
    private static final Gson GSON = new Gson();

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS games (
              game_id INT NOT NULL AUTO_INCREMENT,
              white_username VARCHAR(255) DEFAULT NULL,
              black_username VARCHAR(255) DEFAULT NULL,
              game_name VARCHAR(255) NOT NULL,
              game_json TEXT NOT NULL,
              PRIMARY KEY (game_id)
            )
            """;

    private static final String SELECT_GAMES =
            "SELECT game_id, white_username, black_username, game_name, game_json FROM games";

    public MySqlGameDAO() throws DataAccessException {
        SqlUtil.configureTable(CREATE_TABLE);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return SqlUtil.executeInsert("INSERT INTO games (game_name, game_json) VALUES (?, ?)",
                gameName, GSON.toJson(new ChessGame()));
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return SqlUtil.queryOne(SELECT_GAMES + " WHERE game_id = ?", MySqlGameDAO::readGame, gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return SqlUtil.query(SELECT_GAMES + " ORDER BY game_id", MySqlGameDAO::readGame);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        int rows = SqlUtil.executeUpdate("UPDATE games SET white_username = ?, black_username = ?, "
                        + "game_name = ?, game_json = ? WHERE game_id = ?",
                game.whiteUsername(), game.blackUsername(), game.gameName(), GSON.toJson(game.game()), game.gameID());
        //same contract as MemoryGameDAO: updating a missing game is an error
        if (rows == 0) {
            throw new DataAccessException("Game not found: " + game.gameID());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        //TRUNCATE also resets AUTO_INCREMENT, matching MemoryGameDAO restarting IDs at 1
        SqlUtil.executeUpdate("TRUNCATE TABLE games");
    }

    private static GameData readGame(ResultSet rs) throws SQLException {
        ChessGame game = GSON.fromJson(rs.getString("game_json"), ChessGame.class);
        return new GameData(rs.getInt("game_id"), rs.getString("white_username"),
                rs.getString("black_username"), rs.getString("game_name"), game);
    }
}
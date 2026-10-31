package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class MySqlAuthDAO implements AuthDAO {
    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS auth_tokens (
              auth_token VARCHAR(255) NOT NULL,
              username VARCHAR(255) NOT NULL,
              PRIMARY KEY (auth_token)
            )
            """;

    public MySqlAuthDAO() throws DataAccessException {
        SqlUtil.configureTable(CREATE_TABLE);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        SqlUtil.executeUpdate("INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)",
                auth.authToken(), auth.username());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return SqlUtil.queryOne("SELECT auth_token, username FROM auth_tokens WHERE auth_token = ?",
                rs -> new AuthData(rs.getString("auth_token"), rs.getString("username")),
                authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        SqlUtil.executeUpdate("DELETE FROM auth_tokens WHERE auth_token = ?", authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        SqlUtil.executeUpdate("TRUNCATE TABLE auth_tokens");
    }
}

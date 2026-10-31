package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

public class MySqlUserDAO implements UserDAO {
    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
              username VARCHAR(255) NOT NULL,
              password_hash VARCHAR(255) NOT NULL,
              email VARCHAR(255) NOT NULL,
              PRIMARY KEY (username)
            )
            """;

    public MySqlUserDAO() throws DataAccessException {
        SqlUtil.configureTable(CREATE_TABLE);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        SqlUtil.executeUpdate("INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)",
                user.username(), user.password(), user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return SqlUtil.queryOne("SELECT username, password_hash, email FROM users WHERE username = ?",
                rs -> new UserData(rs.getString("username"), rs.getString("password_hash"), rs.getString("email")),
                username);
    }

    @Override
    public void clear() throws DataAccessException {
        SqlUtil.executeUpdate("TRUNCATE TABLE users");
    }
}
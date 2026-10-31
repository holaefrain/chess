package dataaccess.sql;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;

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
}

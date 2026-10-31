package dataaccess.sql;

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
}

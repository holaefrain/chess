package dataaccess.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dataaccess.DatabaseManager;

//shared JDBC plumbing so each SQL DAO only holds its own SQL and row mapping
final class SqlUtil {

    //like Function, but allowed to throw the SQLException that ResultSet getters declare
    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private SqlUtil() {
    }

    // Creates the database and the given table if missing; safe to run on every startup
    static void configureTable(String createStatement) throws DataAccessException {
        DatabaseManager.createDatabase();
        executeUpdate(createStatement);
    }

    // Runs INSERT/UPDATE/DELETE/DDL and returns how many rows matched
    static int executeUpdate(String sql, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("unable to update database", ex);
        }
    }

    // Runs an INSERT into an AUTO_INCREMENT table and returns the new row's ID
    static int executeInsert(String sql, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("unable to update database", ex);
        }
    }
}
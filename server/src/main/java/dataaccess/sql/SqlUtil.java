package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

//shared JDBC plumbing so each SQL DAO only holds its own SQL and row mapping
final class SqlUtil {

    //like Function, but allowed to throw the SQLException that ResultSet getters declare
    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private SqlUtil() {
    }

    //creates the database and the given table if missing; safe to run on every startup
    static void configureTable(String createStatement) throws DataAccessException {
        DatabaseManager.createDatabase();
        executeUpdate(createStatement);
    }

    //runs INSERT/UPDATE/DELETE/DDL and returns how many rows matched
    static int executeUpdate(String sql, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("unable to update database", ex);
        }
    }

    //runs an INSERT into an AUTO_INCREMENT table and returns the new row's ID
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

    static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
                return rows;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("unable to read database", ex);
        }
    }

    //first matching row, or null when nothing matches
    static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws DataAccessException {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    //setObject picks the JDBC type from the Java value and binds null as SQL NULL
    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
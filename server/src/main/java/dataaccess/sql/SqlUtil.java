package dataaccess.sql;

//shared JDBC plumbing so each SQL DAO only holds its own SQL and row mapping
final class SqlUtil {

    //like Function, but allowed to throw the SQLException that ResultSet getters declare
    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private SqlUtil() {
    }
}
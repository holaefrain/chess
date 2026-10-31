package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SqlUtilTests {

    //connections select the chess catalog, so it has to exist first
    @BeforeAll
    static void createDatabase() throws Exception {
        DatabaseManager.createDatabase();
    }

    @Test
    void queryMapsEveryRow() throws Exception {
        List<Integer> rows = SqlUtil.query("SELECT 1 UNION ALL SELECT 2", rs -> rs.getInt(1));
        Assertions.assertEquals(List.of(1, 2), rows);
    }

    @Test
    void queryBindsParameters() throws Exception {
        Integer sum = SqlUtil.queryOne("SELECT ? + ?", rs -> rs.getInt(1), 2, 3);
        Assertions.assertEquals(5, sum);
    }

    //nullable columns like white_username depend on this
    @Test
    void queryBindsNull() throws Exception {
        Boolean isNull = SqlUtil.queryOne("SELECT ? IS NULL", rs -> rs.getBoolean(1), (Object) null);
        Assertions.assertTrue(isNull);
    }

    @Test
    void queryOneNoRowsReturnsNull() throws Exception {
        Assertions.assertNull(SqlUtil.queryOne("SELECT 1 FROM DUAL WHERE 1 = 0", rs -> rs.getInt(1)));
    }

    @Test
    void queryBadSqlThrows() {
        Assertions.assertThrows(DataAccessException.class,
                () -> SqlUtil.query("SELECT * FROM no_such_table", rs -> rs.getInt(1)));
    }

    @Test
    void executeUpdateBadSqlThrows() {
        Assertions.assertThrows(DataAccessException.class,
                () -> SqlUtil.executeUpdate("DELETE FROM no_such_table"));
    }
}

package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import java.util.List;

public class SqlUtilTests {
    
    //connections select the chess catalog, so it has to exist first
    @BeforeAll
    static void createDatabase() throws Exception {
        DatabaseManager.createDatabase();
    }


}

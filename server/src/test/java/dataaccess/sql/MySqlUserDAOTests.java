package dataaccess.sql;

import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySqlUserDAOTests {
    private MySqlUserDAO userDAO;

    private final UserData alex = new UserData("alex", "hashed-pw", "a@mail.com");

    //fresh empty table for every test
    @BeforeEach
    void setUp() throws Exception {
        userDAO = new MySqlUserDAO();
        userDAO.clear();
    }

    @Test
    void createUserSuccess() throws Exception {
        userDAO.createUser(alex);

        Assertions.assertEquals(alex, userDAO.getUser("alex"));
    }
    
    //username is the primary key
    @Test
    void createUserDuplicateUsernameThrows() throws Exception {
        userDAO.createUser(alex);

        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(alex));
    }
}

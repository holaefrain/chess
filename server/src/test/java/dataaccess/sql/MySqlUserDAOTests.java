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

    //every column is NOT NULL
    @Test
    void createUserMissingFieldThrows() {
        Assertions.assertThrows(DataAccessException.class,
                () -> userDAO.createUser(new UserData("alex", null, "a@mail.com")));
    }

    @Test
    void getUserReturnsMatchingUser() throws Exception {
        UserData bob = new UserData("bob", "other-pw", "b@mail.com");
        userDAO.createUser(alex);
        userDAO.createUser(bob);

        Assertions.assertEquals(bob, userDAO.getUser("bob"));
    }

    @Test
    void getUserMissingReturnsNull() throws Exception {
        Assertions.assertNull(userDAO.getUser("nobody"));
    }

    @Test
    void clearRemovesAllUsers() throws Exception {
        userDAO.createUser(alex);

        userDAO.clear();

        Assertions.assertNull(userDAO.getUser("alex"));
    }
}

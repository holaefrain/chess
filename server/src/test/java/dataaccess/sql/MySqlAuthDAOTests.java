package dataaccess.sql;

import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySqlAuthDAOTests {
    private MySqlAuthDAO authDAO;

    private final AuthData alexAuth = new AuthData("alex-token", "alex");

    //fresh empty table for every test
    @BeforeEach
    void setUp() throws Exception {
        authDAO = new MySqlAuthDAO();
        authDAO.clear();
    }

    @Test
    void createAuthSuccess() throws Exception {
        authDAO.createAuth(alexAuth);

        Assertions.assertEquals(alexAuth, authDAO.getAuth("alex-token"));
    }

    //token is the primary key
    @Test
    void createAuthDuplicateTokenThrows() throws Exception {
        authDAO.createAuth(alexAuth);

        Assertions.assertThrows(DataAccessException.class, () -> authDAO.createAuth(alexAuth));
    }

    //one user can hold several sessions
    @Test
    void getAuthReturnsEachSession() throws Exception {
        AuthData second = new AuthData("alex-token-2", "alex");
        authDAO.createAuth(alexAuth);
        authDAO.createAuth(second);

        Assertions.assertEquals(alexAuth, authDAO.getAuth("alex-token"));
        Assertions.assertEquals(second, authDAO.getAuth("alex-token-2"));
    }

    @Test
    void getAuthMissingReturnsNull() throws Exception {
        Assertions.assertNull(authDAO.getAuth("not-a-token"));
    }

    @Test
    void deleteAuthRemovesOnlyThatToken() throws Exception {
        AuthData bobAuth = new AuthData("bob-token", "bob");
        authDAO.createAuth(alexAuth);
        authDAO.createAuth(bobAuth);

        authDAO.deleteAuth("alex-token");

        Assertions.assertNull(authDAO.getAuth("alex-token"));
        Assertions.assertEquals(bobAuth, authDAO.getAuth("bob-token"));
    }

    //service checks the token first, so the DAO just no-ops on a missing one
    @Test
    void deleteAuthMissingTokenDoesNotThrow() {
        Assertions.assertDoesNotThrow(() -> authDAO.deleteAuth("not-a-token"));
    }

    @Test
    void clearRemovesAllAuths() throws Exception {
        authDAO.createAuth(alexAuth);

        authDAO.clear();

        Assertions.assertNull(authDAO.getAuth("alex-token"));
    }
}
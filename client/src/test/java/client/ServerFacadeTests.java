package client;

import model.AuthData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    //fresh empty database for every test
    @BeforeEach
    void clearDatabase() throws ResponseException {
        facade.clear();
    }

    @Test
    void clearSuccess() throws ResponseException {
        registerPlayer();
        facade.clear();
        //the same username is free again only if clear really wiped the users
        AuthData auth = registerPlayer();
        Assertions.assertEquals("player1", auth.username());
    }

    @Test
    void clearServerUnreachable() throws IOException {
        ServerFacade offline = new ServerFacade(unusedPort());
        ResponseException ex = Assertions.assertThrows(ResponseException.class, offline::clear);
        Assertions.assertEquals(ResponseException.Reason.UNREACHABLE, ex.reason());
    }

    //asks the OS for a free port, then releases it so nothing is listening there
    private static int unusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
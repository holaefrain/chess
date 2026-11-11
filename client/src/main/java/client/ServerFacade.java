package client;

import java.time.Duration;

//only class that knows the server is reached over HTTP and JSON
public class ServerFacade {
    private static final Gson GSON = new Gson();
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int HTTP_OK = 200;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String serverUrl;

    private ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public void clear() throws ResponseException {
        send(buildRequest("DELETE", "/db", null, null), null);
    }
}

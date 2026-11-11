package client;

import java.net.http.HttpRequest;
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

    //authToken and body are optional; pass null to leave them off
    private HttpRequest buildRequest(String method, String path, String authToken, Object body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl + path))
            .timeout(TIMEOUT)
            .method(method, bodyPublisher(body));
        if (authToken != null) {
            builder.setHeader("authorization", authToken);
        }
        return builder.build();
    }
}

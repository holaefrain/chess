package client;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
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

    private static HttpRequest.BodyPublisher bodyPublisher(Object body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofString(GSON.toJson(body));
    }

    //pass a null responseType when the endpoints return nothing
    private <T> T send(HttpRequest request, Class<T> responseType) throws ResponseException {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            throw new ResponseException(ResponseException.Reason.UNREACHABLE);
        }
        if (response.statusCode() != HTTP_OK) {
            throw new ResponseException(reasonFor(response.statusCode()));
        }
        return responseType == null ? null : GSON.fromJson(response.body(), responseType);
    }

    private static ResponseException.Reason reasonFor(int status) {
        return switch (status) {
            case 400 -> ResponseException.Reason.BAD_REQUEST;
            case 401 -> ResponseException.Reason.UNAUTHORIZED;
            case 409 -> ResponseException.Reason.ALREADY_TAKEN;
            default -> ResponseException.Reason.SERVER_ERROR;
        };
    }
}

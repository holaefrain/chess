package server;

import com.google.gson.Gson;
import io.javalin.http.Context;

// Shared JSON and header helpers so every handler talks HTTP the same way
final class HandlerUtil {
    private static final Gson GSON = new Gson();

    private HandlerUtil() {
    }

    // Token comes from the authorization header; null when it's missing
    static String authToken(Context ctx) {
        return ctx.header("authorization");
    }

    // Empty or missing body becomes null, which the services treat as a bad request
    static <T> T readBody(Context ctx, Class<T> type) {
        return GSON.fromJson(ctx.body(), type);
    }

    static void send(Context ctx, Object body) {
        ctx.contentType("application/json");
        ctx.result(GSON.toJson(body));
    }

    // Endpoints with nothing to return still send {}
    static void sendEmpty(Context ctx) {
        ctx.contentType("application/json");
        ctx.result("{}");
    }

    static void sendError(Context ctx, int status, String message) {
        ctx.status(status);
        send(ctx, new ErrorResult(message));
    }
}
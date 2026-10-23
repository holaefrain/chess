package server;

import com.google.gson.Gson;
import io.javalin.http.Context;

// Shared JSON and header helpers so every handler talks HTTP the same way
final class HandlerUtil {
    private static final Gson GSON = new Gson();

    private HandlerUtil() {
    }
}
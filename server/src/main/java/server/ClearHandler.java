package server;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    // DELETE /db
    public void clear(Context ctx) throws DataAccessException {
        clearService.clear();
        HandlerUtil.sendEmpty(ctx);
    }
}

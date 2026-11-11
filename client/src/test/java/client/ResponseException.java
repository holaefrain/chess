package client;

//failure type for UI; no HTTP details leak past the facade
public class ResponseException extends Exception {
    
    public enum Reason {
        BAD_REQUEST("That request wasn't valide. Check what you typed and try again."), UNAUTHORIZED("Not authorized. Check your username and password."), ALREADY_TAKEN("Already taken. Choose a different one."), SERVER_ERROR("The server had a problem. Please try again later."), UNREACHABLE("Can't reach the server. Make sure it is running");

        private final String message;

        Reason(String message) {
            this.message = message;
        }
    }

    private final Reason reason;

    public ResponseException(Reason reason) {
        super(reason.message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}

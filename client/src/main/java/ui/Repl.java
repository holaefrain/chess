package ui;

import java.util.Scanner;

//reads lines and prints results; the only class that touches the console
public class Repl {
    private final ChessClient client;

    public Repl(ChessClient client) {
        this.client = client;
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess. Type help to get started.");
        Scanner scanner = new Scanner(System.in);
        while (client.state() != ChessClient.State.QUIT) {
            System.out.print(prompt());
            //end of input (ctrl-D) has no next line, so leave instead of crashing
            if (!scanner.hasNextLine()) {
                break;
            }
            System.out.println(evalSafely(scanner.nextLine()));
        }
    }

    private String prompt() {
        String label = client.state() == ChessClient.State.SIGNED_IN ? "LOGGED_IN" : "LOGGED_OUT";
        return "\n[" + label + "] >>> ";
    }

    //safety net so an unexpected bug shows a message, never a stack trace
    private String evalSafely(String line) {
        try {
            return client.eval(line);
        } catch (RuntimeException ex) {
            return "Something went wrong. Please try again.";
        }
    }
}
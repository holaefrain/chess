package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

//turns a board into coloured text; knows nothing about the server or the console
public final class BoardRenderer {
    private static final int BOARD_SIZE = 8;
    private static final String COLUMN_LETTERS = "abcdefgh";

    private static final String BORDER = EscapeSequences.SET_BG_COLOR_LIGHT_GREY
            + EscapeSequences.SET_TEXT_COLOR_BLACK;
    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = EscapeSequences.SET_BG_COLOR_BLACK;
    private static final String WHITE_PIECE = EscapeSequences.SET_TEXT_COLOR_RED;
    private static final String BLACK_PIECE = EscapeSequences.SET_TEXT_COLOR_BLUE;
    private static final String END_OF_LINE = EscapeSequences.RESET_BG_COLOR
            + EscapeSequences.RESET_TEXT_COLOR + "\n";

    private BoardRenderer() {
    }

    //black sees the board rotated; white and observers see a1 at the bottom left
    public static String render(ChessBoard board, ChessGame.TeamColor perspective) {
        boolean whiteView = perspective != ChessGame.TeamColor.BLACK;
        StringBuilder out = new StringBuilder(EscapeSequences.SET_TEXT_BOLD);
        appendColumnLabels(out, whiteView);
        for (int i = 0; i < BOARD_SIZE; i++) {
            int row = whiteView ? BOARD_SIZE - i : i + 1;
            appendRow(out, board, row, whiteView);
        }
        appendColumnLabels(out, whiteView);
        return out.append(EscapeSequences.RESET_TEXT_BOLD_FAINT).toString();
    }

    private static void appendColumnLabels(StringBuilder out, boolean whiteView) {
        out.append(borderCell(' '));
        for (int i = 0; i < BOARD_SIZE; i++) {
            out.append(borderCell(COLUMN_LETTERS.charAt(columnAt(i, whiteView) - 1)));
        }
        out.append(borderCell(' ')).append(END_OF_LINE);
    }

    private static void appendRow(StringBuilder out, ChessBoard board, int row, boolean whiteView) {
        char label = Character.forDigit(row, 10);
        out.append(borderCell(label));
        for (int i = 0; i < BOARD_SIZE; i++) {
            int col = columnAt(i, whiteView);
            ChessPiece piece = board.getPiece(new ChessPosition(row, col));
            out.append(square(piece, isLightSquare(row, col)));
        }
        out.append(borderCell(label)).append(END_OF_LINE);
    }

    //screen position 0-7 to board column 1-8; black sees column h on the left
    private static int columnAt(int index, boolean whiteView) {
        return whiteView ? index + 1 : BOARD_SIZE - index;
    }

    //a1 is row 1 col 1 (even sum) and must be dark, so odd sums are light
    private static boolean isLightSquare(int row, int col) {
        return (row + col) % 2 != 0;
    }

    private static String borderCell(char label) {
        return BORDER + " " + label + " ";
    }

    private static String square(ChessPiece piece, boolean light) {
        String background = light ? LIGHT_SQUARE : DARK_SQUARE;
        if (piece == null) {
            return background + "   ";
        }
        String color = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PIECE : BLACK_PIECE;
        return background + color + " " + letter(piece.getPieceType()) + " ";
    }

    private static char letter(ChessPiece.PieceType type) {
        return switch (type) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case ROOK -> 'R';
            case PAWN -> 'P';
        };
    }
}

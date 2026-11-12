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
}

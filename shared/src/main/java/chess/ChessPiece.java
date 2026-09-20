package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    /** Whether this piece has moved during the current game. Used by ChessGame to decide castling privleges. Excluded from equals/hashCode so that two boards with the same pieces in the same squares are equal */
    private boolean hasMoved = false;

    // Bishop directions of travel, as pairs
    private static final int[][] DIAGONALS = {
        {1,1}, {1,-1}, {-1,1}, {-1,-1}
    };

    // Rook directions of travel
    private static final int[][] STRAIGHTS = {
        {1,0}, {-1, 0}, {0, 1}, {0, -1}
    };

    // Queen + King directions of travel
    private static final int[][] ALL_DIRECTIONS = {
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1},{0, -1}
    };

    // Knight directions of travel; offsets the jump
    private static final int[][] KNIGHT_OFFSETS = {
        {2,1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };

    // Pawn promotion; the pieces it can promote to
    private static final PieceType[] PROMOTION_TYPES = {
        PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * @return true if this piece has moved during the current game
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        switch (type) {
            case KING -> addSteppingMoves(board, myPosition, moves, ALL_DIRECTIONS);
            case KNIGHT -> addSteppingMoves(board, myPosition, moves, KNIGHT_OFFSETS);
            case BISHOP -> addSlidingMoves(board, myPosition, moves, DIAGONALS);
            case ROOK -> addSlidingMoves(board, myPosition, moves, STRAIGHTS);
            case QUEEN -> addSlidingMoves(board, myPosition, moves, ALL_DIRECTIONS);
            case PAWN -> addPawnMoves(board, myPosition, moves);
        }
        return moves;
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            int row = start.getRow();
            int col = start.getColumn();
            while (true) {
                row += direction[0];
                col += direction[1];
                ChessPosition end = new ChessPosition(row, col);
                if (!end.isOnBoard()) {
                    break;
                }
                ChessPiece occupant = board.getPiece(end);
                if (occupant == null) {
                    moves.add(new ChessMove(start, end, null));
                } else {
                    if (occupant.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(start, end, null));
                    }
                    break;
                }
            }
        }
    }

    private void addSteppingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves, int[][] offsets) {
        for (int[] offset : offsets) {
            ChessPosition end = new ChessPosition(start.getRow() + offset[0], start.getColumn() + offset[1]);
            if (!end.isOnBoard()) {
                continue;
            }
            ChessPiece occupant = board.getPiece(end);
            if (occupant == null || occupant.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(start, end, null));
            }
        }
    }

    private void addPawnMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startingRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int row = start.getRow();
        int col = start.getColumn();

        ChessPosition oneForward = new ChessPosition(row + direction, col);
        if (oneForward.isOnBoard() && board.getPiece(oneForward) == null) {
            addPawnMove(start, oneForward, moves);

            ChessPosition twoForward = new ChessPosition(row + 2 * direction, col);
            if (row == startingRow && board.getPiece(twoForward) == null) {
                addPawnMove(start, twoForward, moves);
            }
        }

        for (int colOffset : new int[]{-1, 1}) {
            ChessPosition capture = new ChessPosition(row + direction , col + colOffset);
            if (!capture.isOnBoard()) {
                continue;
            }
            ChessPiece occupant = board.getPiece(capture);
            if (occupant != null && occupant.getTeamColor() != pieceColor) {
                addPawnMove(start, capture, moves);
            }
        }
    }

    private void addPawnMove(ChessPosition start, ChessPosition end, Collection<ChessMove> moves) {
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;
        if (end.getRow() == promotionRow) {
            for (PieceType promotion : PROMOTION_TYPES) {
                moves.add(new ChessMove(start, end, promotion));
            }
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        char symbol = switch (type) {
            case KING -> 'k';
            case QUEEN -> 'q';
            case BISHOP -> 'b';
            case KNIGHT -> 'n';
            case ROOK -> 'r';
            case PAWN -> 'p';
        };
        return String.valueOf(pieceColor == ChessGame.TeamColor.WHITE
                ? Character.toUpperCase(symbol)
                : symbol);
    }
}

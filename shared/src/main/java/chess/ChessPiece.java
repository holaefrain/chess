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
        throw new RuntimeException("Not implemented");
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        throw new RuntimeException("Not implemented");
    }
}

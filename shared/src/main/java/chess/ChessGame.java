package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    private ChessPosition enPassantTarget;
    private ChessPosition enPassantVictim;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
        }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
        }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> candidates = new ArrayList<>(piece.pieceMoves(board, startPosition));
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            addEnPassantMoves(startPosition, piece, candidates);
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            addCastlingMoves(startPosition, piece, candidates);
        }

        Collection<ChessMove> legalMoves = new ArrayList<>();
        for (ChessMove move : candidates) {
            if (!leavesKingInCheck(move, piece.getTeamColor())) {
                legalMoves.add(moves);
            }
        }
        return legalMoves;
        }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("There is no piece at " + start);
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It is " + teamTurn + "'s turn");
        }

        Collection<ChessMove> legalMoves = validMoves(start);
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Illegal move: " + move);
        }

        // Mark before applying: the rook is easier to find on its starting square.
        piece.markMoved();
        if (isCastle(piece, move)) {
            ChessPiece rook = board.getPiece(new ChessPosition(start.getRow(), castleRookStartColumn(move)));
            if (rook != null) {
                rook.markMoved();
            }
        }

        applyMove(board, move, enPassantTarget);
        updateEnPassantState(piece, move);
        teamTurn = opponentOf(teamTurn);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(board, teamColor);
        }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidMoves(teamColor);
        }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && hasNoValidMoves(teamColor);
        }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        this.enPassantTarget = null;
        this.enPassantVictim = null;
        }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
        }
}

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
                legalMoves.add(move);
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
    
     /**
     * @return true if the given team has no legal move with any of its pieces
     */
    private boolean hasNoValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }
                Collection<ChessMove> moves = validMoves(position);
                if (moves != null && !moves.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Plays the move on a scratch copy of the board to see whether it would leave
     * the mover's own king under attack.
     */
    private boolean leavesKingInCheck(ChessMove move, TeamColor teamColor) {
        ChessBoard testBoard = new ChessBoard(board);
        applyMove(testBoard, move, enPassantTarget);
        return isInCheck(testBoard, teamColor);
    }

    /**
     * @return true if the given team's king is attacked by any enemy piece on the board
     */
    private static boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPosition = findKing(board, teamColor);
        if (kingPosition == null) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }
                for (ChessMove attack : piece.pieceMoves(board, position)) {
                    if (attack.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return the square holding the given team's king, or null if it is not on the board
     */
    private static ChessPosition findKing(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null
                        && piece.getTeamColor() == teamColor
                        && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Moves the piece on the given board, also handling the rook relocation of a
     * castle, the captured pawn of an en passant, and pawn promotion. Does not
     * validate the move and does not update game state, so it is safe to use on a
     * scratch board.
     */
    private static void applyMove(ChessBoard board, ChessMove move, ChessPosition enPassantTarget) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        if (piece == null) {
            return;
        }

        board.addPiece(start, null);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                && end.equals(enPassantTarget)
                && board.getPiece(end) == null) {
            board.addPiece(new ChessPosition(start.getRow(), end.getColumn()), null);
        }

        if (isCastle(piece, move)) {
            int rookStartColumn = castleRookStartColumn(move);
            int rookEndColumn = (end.getColumn() == 7) ? 6 : 4;
            ChessPiece rook = board.getPiece(new ChessPosition(start.getRow(), rookStartColumn));
            board.addPiece(new ChessPosition(start.getRow(), rookStartColumn), null);
            board.addPiece(new ChessPosition(start.getRow(), rookEndColumn), rook);
        }

        ChessPiece landing = (move.getPromotionPiece() == null)
                ? piece
                : new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        board.addPiece(end, landing);
    }

    /**
     * @return true if the move is a king sliding two columns, which only castling does
     */
    private static boolean isCastle(ChessPiece piece, ChessMove move) {
        return piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2;
    }

    /**
     * @return the column the castling rook starts on for the given king move
     */
    private static int castleRookStartColumn(ChessMove move) {
        return (move.getEndPosition().getColumn() == 7) ? 8 : 1;
    }

    /**
     * Records the skipped square after a pawn advances two ranks, and clears it after
     * any other move, so an en passant capture is only available on the very next turn.
     */
    private void updateEnPassantState(ChessPiece piece, ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        boolean doubleAdvance = piece.getPieceType() == ChessPiece.PieceType.PAWN
                && Math.abs(end.getRow() - start.getRow()) == 2;

        if (doubleAdvance) {
            int skippedRow = (start.getRow() + end.getRow()) / 2;
            enPassantTarget = new ChessPosition(skippedRow, start.getColumn());
            enPassantVictim = end;
        } else {
            enPassantTarget = null;
            enPassantVictim = null;
        }
    }

    /**
     * Adds the en passant capture for a pawn standing beside a pawn that just made a
     * double advance.
     */
    private void addEnPassantMoves(ChessPosition start, ChessPiece pawn, Collection<ChessMove> moves) {
        if (enPassantTarget == null || enPassantVictim == null) {
            return;
        }
        ChessPiece victim = board.getPiece(enPassantVictim);
        if (victim == null || victim.getTeamColor() == pawn.getTeamColor()) {
            return;
        }

        int direction = (pawn.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
        if (start.getRow() + direction == enPassantTarget.getRow()
                && Math.abs(start.getColumn() - enPassantTarget.getColumn()) == 1) {
            moves.add(new ChessMove(start, enPassantTarget, null));
        }
    }

    /**
     * Adds any castle the king is still entitled to: neither the king nor that rook
     * has moved this game, the squares between them are empty, and the king is
     * neither in check nor passing through an attacked square. The square the king
     * lands on is screened by the usual check filter in validMoves.
     */
    private void addCastlingMoves(ChessPosition kingPosition, ChessPiece king, Collection<ChessMove> moves) {
        int homeRow = (king.getTeamColor() == TeamColor.WHITE) ? 1 : 8;
        if (king.hasMoved()
                || kingPosition.getRow() != homeRow
                || kingPosition.getColumn() != 5
                || isInCheck(king.getTeamColor())) {
            return;
        }

        addCastleIfLegal(kingPosition, king, moves, 8, new int[]{6, 7}, 7, 6);
        addCastleIfLegal(kingPosition, king, moves, 1, new int[]{2, 3, 4}, 3, 4);
    }

    private void addCastleIfLegal(ChessPosition kingPosition, ChessPiece king, Collection<ChessMove> moves,
                                  int rookColumn, int[] emptyColumns, int kingEndColumn, int transitColumn) {
        int row = kingPosition.getRow();

        ChessPiece rook = board.getPiece(new ChessPosition(row, rookColumn));
        if (rook == null
                || rook.getPieceType() != ChessPiece.PieceType.ROOK
                || rook.getTeamColor() != king.getTeamColor()
                || rook.hasMoved()) {
            return;
        }

        for (int column : emptyColumns) {
            if (board.getPiece(new ChessPosition(row, column)) != null) {
                return;
            }
        }

        ChessMove throughSquare = new ChessMove(kingPosition, new ChessPosition(row, transitColumn), null);
        if (leavesKingInCheck(throughSquare, king.getTeamColor())) {
            return;
        }

        moves.add(new ChessMove(kingPosition, new ChessPosition(row, kingEndColumn), null));
    }

    private static TeamColor opponentOf(TeamColor teamColor) {
        return (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame that = (ChessGame) o;
        return teamTurn == that.teamTurn && Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    @Override
    public String toString() {
        return teamTurn + " to move\n" + board;
    }
}


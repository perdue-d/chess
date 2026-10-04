package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A class that can manage a chess game, making moves on a board
 *
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
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
        teamTurn = team;
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

        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : piece.pieceMoves(board, startPosition)) {

            ChessPosition endPosition = move.getEndPosition();
            ChessPiece capturedPiece = board.getPiece(endPosition);

            board.addPiece(startPosition, null);
            board.addPiece(endPosition, piece);

            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }

            board.addPiece(startPosition, piece);
            board.addPiece(endPosition, capturedPiece);
        }

        return validMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        if (move == null) {
            throw new InvalidMoveException();
        }

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        if (startPosition == null || endPosition == null) {
            throw new InvalidMoveException();
        }

        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            throw new InvalidMoveException();
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> legalMoves = validMoves(startPosition);

        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        board.addPiece(startPosition, null);

        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(endPosition, piece);

        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        ChessPosition kingPosition = findKing(teamColor);

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

                if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {

                    int direction;

                    if (piece.getTeamColor() == TeamColor.WHITE) {
                        direction = 1;
                    } else {
                        direction = -1;
                    }

                    int attackRow = row + direction;

                    if (attackRow >= 1 && attackRow <= 8) {

                        int leftColumn = col - 1;
                        int rightColumn = col + 1;

                        if (leftColumn >= 1) {
                            if (kingPosition.getRow() == attackRow
                                    && kingPosition.getColumn() == leftColumn) {
                                return true;
                            }
                        }

                        if (rightColumn <= 8) {
                            if (kingPosition.getRow() == attackRow
                                    && kingPosition.getColumn() == rightColumn) {
                                return true;
                            }
                        }
                    }
                } else {

                    for (ChessMove move : piece.pieceMoves(board, position)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {

        if (!isInCheck(teamColor)) {
            return false;
        }

        return !hasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate
     */
    public boolean isInStalemate(TeamColor teamColor) {

        if (isInCheck(teamColor)) {
            return false;
        }

        return !hasValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private boolean hasValidMoves(TeamColor teamColor) {

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {

                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {

                    Collection<ChessMove> moves = validMoves(position);

                    if (moves != null && !moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private ChessPosition findKing(TeamColor teamColor) {

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
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ChessGame)) {
            return false;
        }

        ChessGame other = (ChessGame) obj;

        return teamTurn == other.teamTurn
                && board.equals(other.board);
    }

    @Override
    public int hashCode() {
        int result = board.hashCode();
        result = 31 * result + teamTurn.hashCode();
        return result;
    }
}
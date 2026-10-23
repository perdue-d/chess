
package chess;

import java.util.Collection;
import java.util.ArrayList;

public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteLeftRookMoved = false;
    private boolean whiteRightRookMoved = false;
    private boolean blackLeftRookMoved = false;
    private boolean blackRightRookMoved = false;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            ChessPosition end = move.getEndPosition();
            ChessPiece captured = board.getPiece(end);

            board.addPiece(startPosition, null);
            board.addPiece(end, piece);

            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }

            board.addPiece(startPosition, piece);
            board.addPiece(end, captured);
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            int row = piece.getTeamColor() == TeamColor.WHITE ? 1 : 8;

            if (startPosition.equals(new ChessPosition(row, 5))) {
                if (canCastle(piece.getTeamColor(), true)) {
                    validMoves.add(new ChessMove(
                            startPosition,
                            new ChessPosition(row, 7),
                            null));
                }

                if (canCastle(piece.getTeamColor(), false)) {
                    validMoves.add(new ChessMove(
                            startPosition,
                            new ChessPosition(row, 3),
                            null));
                }
            }
        }

        return validMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null) {
            throw new InvalidMoveException();
        }

        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        if (start == null || end == null) {
            throw new InvalidMoveException();
        }

        ChessPiece piece = board.getPiece(start);

        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> moves = validMoves(start);

        if (moves == null || !moves.contains(move)) {
            throw new InvalidMoveException();
        }

        ChessPiece captured = board.getPiece(end);

        if (captured != null &&
                captured.getPieceType() == ChessPiece.PieceType.ROOK) {
            updateRookMoved(end, captured.getTeamColor());
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (teamTurn == TeamColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            updateRookMoved(start, teamTurn);
        }

        board.addPiece(start, null);

        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(teamTurn, move.getPromotionPiece());
        }

        board.addPiece(end, piece);

        if (piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(end.getColumn() - start.getColumn()) == 2) {

            int row = start.getRow();
            boolean kingSide = end.getColumn() == 7;

            ChessPosition rookStart =
                    new ChessPosition(row, kingSide ? 8 : 1);

            ChessPosition rookEnd =
                    new ChessPosition(row, kingSide ? 6 : 4);

            ChessPiece rook = board.getPiece(rookStart);

            board.addPiece(rookStart, null);
            board.addPiece(rookEnd, rook);
        }

        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

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

                if (attacksKing(piece, position, kingPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean attacksKing(ChessPiece piece,
                                ChessPosition position,
                                ChessPosition kingPosition) {

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int direction =
                    piece.getTeamColor() == TeamColor.WHITE ? 1 : -1;

            return kingPosition.getRow() == position.getRow() + direction
                    && Math.abs(kingPosition.getColumn()
                    - position.getColumn()) == 1;
        }

        for (ChessMove move : piece.pieceMoves(board, position)) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return !hasValidMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return !hasValidMoves(teamColor);
    }

    public void setBoard(ChessBoard board) {
        this.board = board;

        whiteKingMoved = false;
        blackKingMoved = false;
        whiteLeftRookMoved = false;
        whiteRightRookMoved = false;
        blackLeftRookMoved = false;
        blackRightRookMoved = false;
    }

    public ChessBoard getBoard() {
        return board;
    }

    private boolean hasValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }

                Collection<ChessMove> moves = validMoves(position);

                if (moves != null && !moves.isEmpty()) {
                    return true;
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

    private void updateRookMoved(ChessPosition position, TeamColor color) {
        int row = color == TeamColor.WHITE ? 1 : 8;

        if (position.getRow() != row) {
            return;
        }

        if (color == TeamColor.WHITE) {
            if (position.getColumn() == 1) {
                whiteLeftRookMoved = true;
            }

            if (position.getColumn() == 8) {
                whiteRightRookMoved = true;
            }
        } else {
            if (position.getColumn() == 1) {
                blackLeftRookMoved = true;
            }

            if (position.getColumn() == 8) {
                blackRightRookMoved = true;
            }
        }
    }

    private boolean canCastle(TeamColor color, boolean kingSide) {
        int row = color == TeamColor.WHITE ? 1 : 8;

        if (color == TeamColor.WHITE) {
            if (whiteKingMoved ||
                    (kingSide ? whiteRightRookMoved : whiteLeftRookMoved)) {
                return false;
            }
        } else {
            if (blackKingMoved ||
                    (kingSide ? blackRightRookMoved : blackLeftRookMoved)) {
                return false;
            }
        }

        ChessPosition king = new ChessPosition(row, 5);
        ChessPosition rook = new ChessPosition(row, kingSide ? 8 : 1);

        ChessPiece kingPiece = board.getPiece(king);
        ChessPiece rookPiece = board.getPiece(rook);

        if (kingPiece == null || rookPiece == null) {
            return false;
        }

        if (kingPiece.getPieceType() != ChessPiece.PieceType.KING ||
                rookPiece.getPieceType() != ChessPiece.PieceType.ROOK ||
                kingPiece.getTeamColor() != color ||
                rookPiece.getTeamColor() != color) {
            return false;
        }

        if (isInCheck(color)) {
            return false;
        }

        int direction = kingSide ? 1 : -1;

        for (int col = 5 + direction;
             col != rook.getColumn();
             col += direction) {

            if (board.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }

        board.addPiece(king, null);

        for (int step = 1; step <= 2; step++) {
            ChessPosition position =
                    new ChessPosition(row, 5 + direction * step);

            board.addPiece(position, kingPiece);

            boolean inCheck = isInCheck(color);

            board.addPiece(position, null);

            if (inCheck) {
                board.addPiece(king, kingPiece);
                return false;
            }
        }

        board.addPiece(king, kingPiece);

        return true;
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

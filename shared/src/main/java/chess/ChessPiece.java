
package chess;

import java.util.Collection;
import java.util.ArrayList;

public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board,
                                            ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        switch (type) {
            case BISHOP -> slidingMoves(board, position, moves,
                    new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});

            case ROOK -> slidingMoves(board, position, moves,
                    new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});

            case QUEEN -> slidingMoves(board, position, moves,
                    new int[][]{
                            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
                    });

            case KNIGHT -> singleMoves(board, position, moves,
                    new int[][]{
                            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
                    });

            case KING -> singleMoves(board, position, moves,
                    new int[][]{
                            {1, 1}, {1, 0}, {1, -1},
                            {0, 1}, {0, -1},
                            {-1, 1}, {-1, 0}, {-1, -1}
                    });

            case PAWN -> pawnMoves(board, position, moves);
        }

        return moves;
    }

    private boolean insideBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private void slidingMoves(ChessBoard board, ChessPosition start,
                              Collection<ChessMove> moves,
                              int[][] directions) {

        for (int[] direction : directions) {
            int row = start.getRow() + direction[0];
            int col = start.getColumn() + direction[1];

            while (insideBoard(row, col)) {
                ChessPosition end = new ChessPosition(row, col);
                ChessPiece other = board.getPiece(end);

                if (other == null) {
                    moves.add(new ChessMove(start, end, null));
                } else {
                    if (other.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(start, end, null));
                    }
                    break;
                }

                row += direction[0];
                col += direction[1];
            }
        }
    }

    private void singleMoves(ChessBoard board, ChessPosition start,
                             Collection<ChessMove> moves,
                             int[][] directions) {

        for (int[] direction : directions) {
            int row = start.getRow() + direction[0];
            int col = start.getColumn() + direction[1];

            if (!insideBoard(row, col)) {
                continue;
            }

            ChessPosition end = new ChessPosition(row, col);
            ChessPiece other = board.getPiece(end);

            if (other == null || other.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(start, end, null));
            }
        }
    }

    private void pawnMoves(ChessBoard board, ChessPosition start,
                           Collection<ChessMove> moves) {

        int direction =
                pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;

        int startingRow =
                pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7;

        int promotionRow =
                pieceColor == ChessGame.TeamColor.WHITE ? 8 : 1;

        int row = start.getRow();
        int col = start.getColumn();
        int nextRow = row + direction;

        if (!insideBoard(nextRow, col)) {
            return;
        }

        ChessPosition forward = new ChessPosition(nextRow, col);

        if (board.getPiece(forward) == null) {
            addPawnMove(moves, start, forward, promotionRow);

            if (row == startingRow) {
                ChessPosition doubleForward =
                        new ChessPosition(row + 2 * direction, col);

                if (board.getPiece(doubleForward) == null) {
                    moves.add(new ChessMove(start, doubleForward, null));
                }
            }
        }

        for (int dc : new int[]{-1, 1}) {
            int newCol = col + dc;

            if (!insideBoard(nextRow, newCol)) {
                continue;
            }

            ChessPosition diagonal = new ChessPosition(nextRow, newCol);
            ChessPiece other = board.getPiece(diagonal);

            if (other != null && other.getTeamColor() != pieceColor) {
                addPawnMove(moves, start, diagonal, promotionRow);
            }
        }
    }

    private void addPawnMove(Collection<ChessMove> moves,
                             ChessPosition start,
                             ChessPosition end,
                             int promotionRow) {

        if (end.getRow() == promotionRow) {
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ChessPiece other)) {
            return false;
        }

        return pieceColor == other.pieceColor && type == other.type;
    }

    @Override
    public int hashCode() {
        int result = pieceColor.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}

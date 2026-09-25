
package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 *
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

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
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if (type == PieceType.BISHOP || type == PieceType.ROOK
                || type == PieceType.QUEEN) {

            int[][] directions;

            if (type == PieceType.BISHOP) {
                directions = new int[][]{
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
            } else if (type == PieceType.ROOK) {
                directions = new int[][]{
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
                };
            } else {
                directions = new int[][]{
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
                };
            }

            for (int[] direction : directions) {
                int r = row + direction[0];
                int c = col + direction[1];

                while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                    ChessPosition end = new ChessPosition(r, c);
                    ChessPiece other = board.getPiece(end);

                    if (other == null) {
                        moves.add(new ChessMove(myPosition, end, null));
                    } else {
                        if (other.getTeamColor() != pieceColor) {
                            moves.add(new ChessMove(myPosition, end, null));
                        }
                        break;
                    }

                    r += direction[0];
                    c += direction[1];
                }
            }
        }

        if (type == PieceType.KNIGHT) {
            int[][] directions = {
                    {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                    {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
            };

            for (int[] direction : directions) {
                int r = row + direction[0];
                int c = col + direction[1];

                if (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                    ChessPosition end = new ChessPosition(r, c);
                    ChessPiece other = board.getPiece(end);

                    if (other == null || other.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition, end, null));
                    }
                }
            }
        }

        if (type == PieceType.KING) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) {
                        continue;
                    }

                    int r = row + dr;
                    int c = col + dc;

                    if (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                        ChessPosition end = new ChessPosition(r, c);
                        ChessPiece other = board.getPiece(end);

                        if (other == null || other.getTeamColor() != pieceColor) {
                            moves.add(new ChessMove(myPosition, end, null));
                        }
                    }
                }
            }
        }

        if (type == PieceType.PAWN) {
            int direction = pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
            int startingRow = pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7;
            int promotionRow = pieceColor == ChessGame.TeamColor.WHITE ? 8 : 1;

            int nextRow = row + direction;

            if (nextRow >= 1 && nextRow <= 8) {
                ChessPosition forward = new ChessPosition(nextRow, col);

                if (board.getPiece(forward) == null) {
                    addPawnMove(moves, myPosition, forward, promotionRow);

                    if (row == startingRow) {
                        ChessPosition doubleForward =
                                new ChessPosition(row + 2 * direction, col);

                        if (board.getPiece(doubleForward) == null) {
                            moves.add(new ChessMove(myPosition, doubleForward, null));
                        }
                    }
                }

                for (int dc : new int[]{-1, 1}) {
                    int c = col + dc;

                    if (c >= 1 && c <= 8) {
                        ChessPosition diagonal = new ChessPosition(nextRow, c);
                        ChessPiece other = board.getPiece(diagonal);

                        if (other != null && other.getTeamColor() != pieceColor) {
                            addPawnMove(moves, myPosition, diagonal, promotionRow);
                        }
                    }
                }
            }
        }

        return moves;
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
}

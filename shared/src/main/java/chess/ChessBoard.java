
package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 *
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[8][8];

        ChessPiece.PieceType[] pieces = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(1, col),
                    new ChessPiece(ChessGame.TeamColor.WHITE, pieces[col - 1]));

            addPiece(new ChessPosition(2, col),
                    new ChessPiece(ChessGame.TeamColor.WHITE,
                            ChessPiece.PieceType.PAWN));

            addPiece(new ChessPosition(7, col),
                    new ChessPiece(ChessGame.TeamColor.BLACK,
                            ChessPiece.PieceType.PAWN));

            addPiece(new ChessPosition(8, col),
                    new ChessPiece(ChessGame.TeamColor.BLACK, pieces[col - 1]));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ChessBoard)) {
            return false;
        }

        ChessBoard other = (ChessBoard) obj;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);

                ChessPiece first = getPiece(position);
                ChessPiece second = other.getPiece(position);

                if (first == null && second == null) {
                    continue;
                }

                if (first == null || second == null) {
                    return false;
                }

                if (first.getPieceType() != second.getPieceType()
                        || first.getTeamColor() != second.getTeamColor()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = getPiece(new ChessPosition(row, col));

                result = 31 * result;

                if (piece != null) {
                    result += piece.getPieceType().hashCode();
                    result = 31 * result + piece.getTeamColor().hashCode();
                }
            }
        }

        return result;
    }
}

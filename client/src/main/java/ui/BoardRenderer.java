package ui;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

final class BoardRenderer {
    private BoardRenderer() {}

    static void draw(TeamColor perspective) {
        var board = new ChessGame().getBoard();
        boolean flipped = perspective == TeamColor.BLACK;
        String files = flipped ? "hgfedcba" : "abcdefgh";
        String labels = "    " + String.join("  ", files.split(""));
        System.out.println(labels);

        for (int row = 0; row < 8; row++) {
            int rank = flipped ? row + 1 : 8 - row;
            System.out.print(rank + "  ");
            for (int col = 0; col < 8; col++) {
                int file = flipped ? 8 - col : col + 1;
                var piece = board.getPiece(new ChessPosition(rank, file));
                String background = (rank + file) % 2 == 0
                        ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_WHITE;
                System.out.print(background + pieceText(piece));
            }
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR + "  " + rank);
        }

        System.out.println(labels);
    }

    private static String pieceText(ChessPiece piece) {
        if (piece == null) {
            return "   ";
        }

        String letter = switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };

        if (piece.getTeamColor() == TeamColor.WHITE) {
            return SET_TEXT_COLOR_BLACK + " " + letter + " ";
        }
        return SET_TEXT_COLOR_BLACK + " " + letter.toLowerCase(java.util.Locale.ROOT) + " ";
    }
}
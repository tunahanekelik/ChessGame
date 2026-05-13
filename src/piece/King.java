package piece;

import main.GamePanel;
import main.Type;

public class King extends Piece {

    public King(int color, int col, int row) {
        super(color, col, row);

        type = Type.KING;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/spr_king_white");
        } else {
            image = getImage("/piece/spr_king_black");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow)) {

            // we can say that it is a small function that checks the rows and columns
            // while changing the position this function calculates the different between
            // the current position and the next position
            if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1 || // only checks N-W-S-E
                    Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1) { // checks NW-SW-NE-SE

               if (isValidSquare(targetCol, targetRow)) {
                   return true;
               }
            }
            // Castling with the rooks
            if (moved == false) {

                // KING SIDE CASTLING O-O
                if (targetCol == preCol+2 &&
                        targetRow == preRow &&
                        pieceIsOnStraightLine(targetCol, targetRow) == false &&
                        GamePanel.isSquareAttacked(preCol, preRow, 1-color) == false && // checking king is not in check
                        GamePanel.isSquareAttacked(preCol+1, preRow, 1-color) == false) { // checking the square king passes through

                    // check that the target square is empty (the rook starts at preCol+3 so no need to check it)
                    boolean targetEmpty = true;
                    for (Piece piece : GamePanel.simPieces) {
                        if (piece != this && piece.col == targetCol && piece.row == targetRow) {
                            targetEmpty = false;
                            break;
                        }
                    }
                    if (!targetEmpty) return false;

                    for (Piece piece : GamePanel.simPieces) { // scaning the simPieces
                        if (piece.col == preCol+3 &&  // if there is a piece that is 3 squares ahead (rook) to the king side
                                piece.row == preRow &&  // on the same row
                                piece.moved == false) { // and didn't move yet
                            GamePanel.castlingP = piece;
                            return true;
                        }
                    }
                }

                // QUEEN SIDE CASTLING O-O-O
                if (targetCol == preCol-2 &&
                        targetRow == preRow &&
                        pieceIsOnStraightLine(targetCol, targetRow) == false &&
                        GamePanel.isSquareAttacked(preCol, preRow, 1-color) == false && // checking king is not in check
                        GamePanel.isSquareAttacked(preCol-1, preRow, 1-color) == false) { // checking the square king passes through
                    Piece knight = null, rook = null;
                    for (Piece piece : GamePanel.simPieces) {
                        if (piece.col == preCol-3 && piece.row == targetRow) { // checking the square next to king
                            knight = piece;
                        }
                        if (piece.col == preCol-4 && piece.row == targetRow) { // rooks position
                            rook = piece;
                        }
                    }
                    // also check the target square is empty
                    boolean targetEmpty = true;
                    for (Piece p : GamePanel.simPieces) {
                        if (p != this && p.col == targetCol && p.row == targetRow) {
                            targetEmpty = false;
                            break;
                        }
                    }
                    if (targetEmpty && knight == null && rook != null && rook.moved == false) {
                        GamePanel.castlingP = rook;
                        return true;
                    }
                }

            }
        }
        return false;
    }
}

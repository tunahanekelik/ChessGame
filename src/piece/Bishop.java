package piece;

import main.GamePanel;
import main.Type;


public class Bishop extends Piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);

        type = Type.BISHOP;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/spr_bishop_white");
        } else {
            image = getImage("/piece/spr_bishop_black");
        }
    }
    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {

            // we get col and row difference after that we are checking if they are equal
            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
                if (isValidSquare(targetCol, targetRow) && pieceIsDiagonalLine(targetCol, targetRow) == false) { // and if they are equal
                    return true; // we can approve it that it can go that direction
                }

            }
        }

        return false;
    }
}

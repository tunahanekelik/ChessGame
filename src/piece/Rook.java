package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece {

    public Rook(int color, int col, int row) {
        super(color, col, row);

        type = Type.ROOK;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/spr_tower_white");
        } else {
            image = getImage("/piece/spr_tower_black");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol,targetRow) && isSameSquare(targetCol, targetRow) == false) {
            // it can mobe as long as the squares vertically or horizontally
            // but the square is not available in the path it can't go there
            // also it can move only in one direction so there is two options here
            //1. the rook will only change the square between rows and the column is not going to change or
            //2. the rook will only change the square between columns and the row is not going to change
            if (targetCol == preCol || targetRow == preRow) {
                if (isValidSquare(targetCol,targetRow) && pieceIsOnStraightLine(targetCol, targetRow) == false) {
                    return true;
                }

            }

        }
        return false;
    }
}

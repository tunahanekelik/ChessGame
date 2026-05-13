package piece;

import main.GamePanel;
import main.Type;

public class Knight extends Piece {

    public Knight(int color, int col, int row) {
        super(color, col, row);

        type = Type.KNIGHT;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/spr_knight_white");
        } else {
            image = getImage("/piece/spr_knight_black");
        }
    }
    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow)) {
            // knight can move if its movement ratio of column and row is 1:2 or 2:1 like a little L shape
            // calculation works like one of the column or row has to be 1 and the other one has to be 2
            // for example Col is 2 Row is 1 also it can be like Col is 1 and Row is 2
            if (Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 2) {
                if (isValidSquare(targetCol, targetRow)) {
                    return true; // it can move between these squares
                }

            }
        }
        return false;
    }
}

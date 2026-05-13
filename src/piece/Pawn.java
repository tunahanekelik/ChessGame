package piece;

import main.Board;
import main.GamePanel;
import main.Type;

import java.awt.*;

public class Pawn extends Piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);

        type = Type.PAWN;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/spr_pawn_white");
        } else {
            image = getImage("/piece/spr_pawn_black");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {

            // defining the movement value based on its color
            int moveValue;
            if (color == GamePanel.WHITE) { // if the color is white it is going up
                moveValue = -1;
            }
            else { // if the color is black it will go down
                moveValue = 1;
            }
            // check the hitting piece
            hittingP = getHittingP(targetCol, targetRow);

            // 1 square movement depending on the moveValue if its -1 it will go up if its +1 it will go up
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) { // and it is not hitting a piece it can move
                return true;
            }

            // --- EVERY PAWN'S FIRST MOVEMENT IS 2 SQUARE ---
            // if the preposition and current position were same and tried to move 2 row forward
            // and is not hitting a piece and not moved before and piece is on straight line is false
            // then it can move 2 square
            if (targetCol == preCol &&
                    targetRow == preRow + moveValue*2 &&
                        hittingP == null && moved == false &&
                            pieceIsOnStraightLine(targetCol, targetRow) == false) {
                return true;
            }
            // eating or capturing whatever you say movement for pawns are has to be diagonally front of it
            if (Math.abs(targetCol - preCol) == 1 && // if the target column is 1 square ahead of precolumn
                    targetRow == preRow + moveValue && // and target row is prerow's added version with moveValue
                    hittingP != null && // and there is a piece diagonal above
                    hittingP.color != color) { // and that piece is not the same color with the pawn
                return true; // then you can capture/eat it
            }

            // --- EN PASSANT ---
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == targetCol && piece.row == preRow && piece.twoStepped == true) {
                        hittingP = piece;
                        return true;
                    }
                }
            }

        }
        return false;
    }

    // --- PAWN DESIGN IS SMALLER ---
    @Override
    public void draw(Graphics2D g2D) {
        // base coordinates from the piece
        int currentX = this.x;
        int currentY = this.y;

        // --- SPECIFIC PAWN_SIZING LOGIC ---

        // horizontally it supposed to be same with the other pieces
        int marginX = 25;
        int pieceSizeX = Board.SQUARE_SIZE - (marginX * 2); // 100 - 50 = 50 pixel for horizontal

        // vertically we are making the pawns shorter according to other pieces
        int marginY = 15;
        int pieceSizeY = Board.SQUARE_SIZE - (marginY * 2); // 100 - 50 = 50 pixel vertically
        // after this change we are going to get a smaller pawn pieces

        // --- DRAW ---
        if (image != null) {

            // with adding margin to x and y we forcing the drawing stay at the center of the Board.SQUARE_SIZE
            g2D.drawImage(image, currentX + marginX, currentY + marginY, pieceSizeX, pieceSizeY, null);
        }

    }
}

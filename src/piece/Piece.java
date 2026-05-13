package piece;

import main.Board;
import main.GamePanel;
import main.Type;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Piece {

    // basic parameters
    public Type type;
    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingP;
    public boolean moved, twoStepped;


    public Piece(int color, int col, int row) {

        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;

    }

    //getImage method will import the images with using BufferedImage
    //and the images we uploaded the file res/piece/... will be getting to the program
    public BufferedImage getImage(String imagePath) {

        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass() .getResourceAsStream(imagePath + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  image;
    }


    // created 2 method to get the current column and row's coordinate
    public int getX(int col) {
        return col * Board.SQUARE_SIZE;
    }

    public int getY(int row) {
        return row * Board.SQUARE_SIZE;
    }


    // we are taking it as a HALF_SQUARE_SIZE because
    // half does mean that we are taking the center of the square

    public int getCol(int x) {
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getRow(int y) {
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getIndex() {
        for (int index = 0; index < GamePanel.simPieces.size(); index++) {
            if (GamePanel.simPieces.get(index) == this) {
                return index;
            }
        }
        return -1;
    }

    public void updatePosition() {

        // for checking is En Passant is available
        if (type == Type.PAWN) {   // if the updated piece type is pawn
            if (Math.abs(row - preRow) == 2) { // and that pawn moved 2 or -2 rows
                twoStepped = true; // call that as a two stepped pawn
            }
        }

        // updating x and y based on its current column and row
        x = getX(col);
        y = getY(row);
        // updating previous column and row also
        preCol = getCol(x);
        preRow = getRow(y);
        moved = true; // checking the piece is moved before or its the first movement of the specific piece
    }

    // if the move is undoable reset the position of the piece that tried to change position
    public void resetPosition() {
        col = preCol;
        row = preRow;
        x = getX(col);
        y = getY(row);
    }

    // recieves target column and row
    public boolean canMove(int targetCol, int targetRow) {
        return false;
    }
    public boolean isWithinBoard(int targetCol, int targetRow) {
        if (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
            return true;
        }
        return false;
    }

    //check method for understanding the targetCol and targetRow is the same
    public boolean isSameSquare(int targetCol, int targetRow) {
        if (targetCol == preCol && targetRow == preRow) {
            return true;
        }
        return false;
    }

    public Piece getHittingP(int targetCol, int targetRow) { // we receive target col and row
        for (Piece piece : GamePanel.simPieces) { // and scan simPiece
            if(piece.col == targetCol && piece.row == targetRow && // and we will see if there is a piece that has exactly same col and row
                    piece != this) { // except this active piece
                return piece;
            }
        }
        return null;
    }

    public boolean isValidSquare(int targetCol, int targetRow) {

        hittingP = getHittingP(targetCol, targetRow);

        if (hittingP == null) { // this square is empty
            return true;
        }
        else { // this square is full with another piece
            if (hittingP.color != this.color) { // if the color of the piece is different then that means it can be captured
                return true;
            } else {
                hittingP = null; // if the color of the piece is same, the piece cannot move there
            }
            return false;
        }
    }

    public boolean pieceIsOnStraightLine(int targetCol, int targetRow) {

        // check this piece while its moving left
        for (int c = preCol-1; c > targetCol; c--) { // decrasing the column number in every loop
            for (Piece piece : GamePanel.simPieces) { // scan the simPieces for if there is a piece currently
                if (piece.col == c && piece.row == targetRow) { // if there is a piece on the line
                    hittingP = piece; // we are setting it as a hittingPiece and thats the end of the line for us
                    return true; // so we return it as a true for understand it this piece will be our end square
                }
            }
        }

        // check this piece while its moving right
        for (int c = preCol+1; c < targetCol; c++) { // increasing the column number in every loop
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingP = piece;
                    return true;
                }
            }
        }

        // check this piece while its moving up
        for (int r = preRow-1; r > targetRow; r--) { // decreasing the row number in every loop
            for (Piece piece : GamePanel.simPieces) {
                if (piece.row == r && piece.col == targetCol) {
                    hittingP = piece;
                    return true;
                }
            }
        }


        // check this piece while its moving down
        for (int r = preRow+1; r < targetRow; r++) { // increasing the row number in every loop
            for (Piece piece : GamePanel.simPieces) {
                if (piece.row == r && piece.col == targetCol) {
                    hittingP = piece;
                    return true;
                }
            }
        }



        return false;
    }

    public boolean pieceIsDiagonalLine(int targetCol, int targetRow) {

        if (targetRow < preRow) {
            // up left
            for (int c = preCol-1; c > targetCol; c--) {   // decreasing the column
                int diff = Math.abs(c - preCol) ; // calling the difference as a diff
                for (Piece piece : GamePanel.simPieces) { // scan the list if
                    if (piece.col == c && piece.row == preRow - diff) { // adding new column and according to that column giving new row
                        hittingP = piece; // checks if there is a piece that has the same col and row
                        return true;
                    }
                }
            }

            // up right
            for (int c = preCol+1; c < targetCol; c++) {   // increasing the column
                int diff = Math.abs(c - preCol) ;
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }

        if (targetRow > preRow) {
            // down left
            for (int c = preCol-1; c > targetCol; c--) {   // decreasing the column
                int diff = Math.abs(c - preCol) ;
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) { // the only difference here because while column is changin we have to add row a this difference
                        hittingP = piece;
                        return true;
                    }
                }
            }

            // down right
            for (int c = preCol+1; c < targetCol; c++) {   // increasing the column
                int diff = Math.abs(c - preCol) ;
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }

        }

        return false;
    }


    // added these to use the pieces drawing with promoting screen
    public int getWidth() {
        return Board.SQUARE_SIZE - 40;
    }

    public int getHeight() {
        return Board.SQUARE_SIZE - 10;
    }

    // adding margin to make pieces more centered method
    public int getDrawX() {
        return getX(this.col) + 20;
    }

    public int getDrawY() {
        return getY(this.row) + 5;
    }


    // redesigning the method draw again
    public void draw(Graphics2D g2D) {
        // we are using x and y coordinates
        int currentX = this.x;
        int currentY = this.y;

        // --- NEW SIZING LOGIC ---

        // the pieces has to fit perfectly to the square vertically (y)
        int marginY = 5;
        int pieceSizeY = Board.SQUARE_SIZE - (marginY * 2); // 100 - 10 = 90 pixel for vertical

        // here I'm making the pieces slimmer because they have to be elegant
        // so I will increase the margin horizontally (x)
        int marginX = 20; // depends on the margin size the piece will be more slimmer or more fatter
        int pieceSizeX = Board.SQUARE_SIZE - (marginX * 2); // 100 - 50 = 50 pixel for horizontal

        // --- DRAWING ---
        if (image != null) {
            // g2D.drawImage(image, x, y, width, height, null);
            // we are adding more margin to the x besides y because
            // we are forcing it to be at the center of the Board.SQUARE_SIZE
            g2D.drawImage(image, currentX + marginX, currentY + marginY, pieceSizeX, pieceSizeY, null);
        }
    }
}

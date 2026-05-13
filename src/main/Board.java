package main;

import java.awt.*;

public class Board {

    final int MAX_COL = 8;
    final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100; // this means 1 square is going to be a 100x100 pixels
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2; // and that means a board size will 800x800 pixel

    // loop for 2d chess board drawing
    // once this loop is done then we increase this row  by 1 and then after we are increasing column again
    public void draw(Graphics2D g2D, Theme theme) {

        int c = 0;

        for(int row = 0; row < MAX_ROW; row++) {

            for(int col =0; col < MAX_COL; col++) {

                // using the theme's colours instead of hardcoded ones
                if (c == 0) {
                    g2D.setColor(theme.lightSquare);
                    c = 1;
                } else {
                    g2D.setColor(theme.darkSquare);
                    c = 0;
                }

                // drawing a square one by one
                g2D.fillRect(col*SQUARE_SIZE, row*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE); // x, y, width, height
                // we get the x and the y by multiplying the column and the row by SQUARE_SIZE
            }

            // after creating the columns the colors are not going to change horizontally
            // the upper method will only draw vertically lines so we are forcing the rows
            // to don't change color while creating the vertical lines after first row is finished
            // second row will start with the same color that last row's ending color
            if (c == 0) {
                c = 1;
            } else {
                c = 0;
            }
        }
    }
}

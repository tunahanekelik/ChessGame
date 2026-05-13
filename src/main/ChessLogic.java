package main;

import piece.*;
import java.awt.Point;
import java.util.ArrayList;

// breaking this into classes so it can breathe — all the game rule stuff lives here now
public class ChessLogic {

    private GamePanel gp;

    public ChessLogic(GamePanel gp) {
        this.gp = gp;
    }

    // --- KING SAFETY ---

    public boolean isIllegal(Piece king) {
        if (king.type == Type.KING) {
            for (Piece piece : gp.simPieces) {
                if (piece != king &&
                        piece.color != king.color &&
                        piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isKingInCheck() {
        Piece king = getKing(true);
        gp.checkingP = null;
        for (Piece piece : gp.simPieces) {
            if (piece.color == gp.currentColor && piece.canMove(king.col, king.row)) {
                gp.checkingP = piece;
                return true;
            }
        }
        return false;
    }

    public boolean opponentCanCaptureKing() {
        Piece king = getKing(false);
        for (Piece piece : gp.simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    public Piece getKing(boolean opponent) {
        for (Piece piece : gp.simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != gp.currentColor) return piece;
            } else {
                if (piece.type == Type.KING && piece.color == gp.currentColor) return piece;
            }
        }
        return null;
    }

    // --- CHECKMATE / STALEMATE ---

    public boolean isCheckmate() {
        Piece king = getKing(true);
        if (kingCanMove(king)) return false;

        // check if the checking piece can be captured
        for (Piece piece : gp.simPieces) {
            if (piece != king && piece.color != gp.currentColor && piece.canMove(gp.checkingP.col, gp.checkingP.row)) {
                return false;
            }
        }

        // checking if check can be blocked with another piece
        int colDiff = Math.abs(gp.checkingP.col - king.col);
        int rowDiff = Math.abs(gp.checkingP.row - king.row);

        if (colDiff == 0) {
            if (gp.checkingP.row < king.row) {
                for (int row = gp.checkingP.row; row < king.row; row++) {
                    for (Piece piece : gp.simPieces) {
                        if (piece != king && piece.color != gp.currentColor && piece.canMove(gp.checkingP.col, row)) {
                            return false;
                        }
                    }
                }
            }
            if (gp.checkingP.row > king.row) {
                for (int row = gp.checkingP.row; row > king.row; row--) {
                    for (Piece piece : gp.simPieces) {
                        if (piece != king && piece.color != gp.currentColor && piece.canMove(gp.checkingP.col, row)) {
                            return false;
                        }
                    }
                }
            }
        } else if (rowDiff == 0) {
            if (gp.checkingP.col < king.col) {
                for (int col = gp.checkingP.col; col < king.col; col++) {
                    for (Piece piece : gp.simPieces) {
                        if (piece != king && piece.color != gp.currentColor && piece.canMove(gp.checkingP.row, col)) {
                            return false;
                        }
                    }
                }
            }
            if (gp.checkingP.col > king.col) {
                for (int col = gp.checkingP.col; col > king.col; col--) {
                    for (Piece piece : gp.simPieces) {
                        if (piece != king && piece.color != gp.currentColor && piece.canMove(gp.checkingP.row, col)) {
                            return false;
                        }
                    }
                }
            }
        } else if (colDiff == rowDiff) {
            if (gp.checkingP.row < king.row) {
                if (gp.checkingP.col < king.col) {
                    for (int col = gp.checkingP.col, row = gp.checkingP.row; col < king.col; col++, row++) {
                        for (Piece piece : gp.simPieces) {
                            if (piece != king && piece.color != gp.currentColor && piece.canMove(col, row)) return false;
                        }
                    }
                }
                if (gp.checkingP.col > king.col) {
                    for (int col = gp.checkingP.col, row = gp.checkingP.row; col > king.col; col--, row++) {
                        for (Piece piece : gp.simPieces) {
                            if (piece != king && piece.color != gp.currentColor && piece.canMove(col, row)) return false;
                        }
                    }
                }
            }
            if (gp.checkingP.row > king.row) {
                if (gp.checkingP.col < king.col) {
                    for (int col = gp.checkingP.col, row = gp.checkingP.row; col < king.col; col++, row--) {
                        for (Piece piece : gp.simPieces) {
                            if (piece != king && piece.color != gp.currentColor && piece.canMove(col, row)) return false;
                        }
                    }
                }
                if (gp.checkingP.col > king.col) {
                    for (int col = gp.checkingP.col, row = gp.checkingP.row; col > king.col; col--, row--) {
                        for (Piece piece : gp.simPieces) {
                            if (piece != king && piece.color != gp.currentColor && piece.canMove(col, row)) return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isStalemate() {
        int opponentColor = (gp.currentColor == GamePanel.WHITE) ? GamePanel.BLACK : GamePanel.WHITE;
        return !hasLegalMoves(opponentColor);
    }

    // checking if a specific color has any legal move or not for stalemate understanding
    public boolean hasLegalMoves(int color) {
        ArrayList<Piece> allPieces = new ArrayList<>(gp.simPieces);
        for (Piece piece : allPieces) {
            if (piece.color != color) continue;

            int startCol = Math.max(0, piece.col - 2);
            int endCol = Math.min(7, piece.col + 2);
            int startRow = Math.max(0, piece.row - 2);
            int endRow = Math.min(7, piece.row + 2);

            if (piece.type == Type.KNIGHT) {
                startCol = Math.max(0, piece.col - 3);
                endCol = Math.min(7, piece.col + 3);
                startRow = Math.max(0, piece.row - 3);
                endRow = Math.min(7, piece.row + 3);
            }
            if (piece.type == Type.ROOK || piece.type == Type.QUEEN || piece.type == Type.BISHOP) {
                startCol = 0; endCol = 7; startRow = 0; endRow = 7;
            }

            for (int r = startRow; r <= endRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    if (piece.canMove(c, r)) {
                        Piece hitPiece = piece.hittingP;
                        int savedCol = piece.col;
                        int savedRow = piece.row;

                        piece.col = c;
                        piece.row = r;
                        if (hitPiece != null) gp.simPieces.remove(hitPiece);

                        Piece king = null;
                        for (Piece p : gp.simPieces) {
                            if (p.type == Type.KING && p.color == color) {
                                king = p; break;
                            }
                        }

                        boolean legal = true;
                        if (king != null) {
                            for (Piece p : gp.simPieces) {
                                if (p.color != color && p.canMove(king.col, king.row)) {
                                    legal = false; break;
                                }
                            }
                        }

                        piece.col = savedCol;
                        piece.row = savedRow;
                        if (hitPiece != null) gp.simPieces.add(hitPiece);

                        if (legal) return true;
                    }
                }
            }
        }
        return false;
    }

    // --- KING MOVE HELPERS ---

    public boolean kingCanMove(Piece king) {
        if (isValidMove(king, -1, -1)) return true;
        if (isValidMove(king, 0, -1)) return true;
        if (isValidMove(king, 1, -1)) return true;
        if (isValidMove(king, -1, 0)) return true;
        if (isValidMove(king, 1, 0)) return true;
        if (isValidMove(king, -1, 1)) return true;
        if (isValidMove(king, 0, 1)) return true;
        if (isValidMove(king, 1, 1)) return true;
        return false;
    }

    public boolean isValidMove(Piece king, int colPlus, int rowPlus) {
        boolean isValidMove = false;
        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {
            if (king.hittingP != null) gp.simPieces.remove(king.hittingP);
            if (!isIllegal(king)) isValidMove = true;
        }

        king.resetPosition();
        gp.copyPieces(gp.pieces, gp.simPieces);
        return isValidMove;
    }

    // created a method for checking if a square is attacked by a specific color
    public static boolean isSquareAttacked(ArrayList<Piece> simPieces, int col, int row, int byColor) {
        for (Piece piece : simPieces) {
            if (piece.color == byColor && piece.canMove(col, row)) return true;
        }
        return false;
    }

    // --- CASTLING ---

    public void checkCastling() {
        if (gp.castlingP != null) {
            if (gp.castlingP.col == 0) {
                gp.castlingP.col += 3;
            } else if (gp.castlingP.col == 7) {
                gp.castlingP.col -= 2;
            }
            gp.castlingP.x = gp.castlingP.getX(gp.castlingP.col);
        }
    }

    // --- VALID SQUARES / SIMULATION ---

    // scanning all 64 squares to see which squares the active piece can go while also checking king safety
    public void computeValidSquares() {
        gp.validSquares.clear();
        if (gp.activeP == null) return;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (gp.activeP.canMove(c, r)) {
                    Piece hitPiece = gp.activeP.hittingP;
                    int savedCol = gp.activeP.col;
                    int savedRow = gp.activeP.row;

                    gp.activeP.col = c;
                    gp.activeP.row = r;
                    if (hitPiece != null) gp.simPieces.remove(hitPiece);

                    boolean kingSafe = !opponentCanCaptureKing();

                    gp.activeP.col = savedCol;
                    gp.activeP.row = savedRow;
                    if (hitPiece != null) gp.simPieces.add(hitPiece);
                    gp.activeP.hittingP = hitPiece;

                    if (kingSafe) gp.validSquares.add(new Point(c, r));
                }
            }
        }
    }

    // simulates the move while the player is dragging a piece
    public void simulate() {
        gp.canMove = false;
        gp.validSquare = false;

        gp.copyPieces(gp.pieces, gp.simPieces);

        if (gp.castlingP != null) {
            gp.castlingP.col = gp.castlingP.preCol;
            gp.castlingP.x = gp.castlingP.getX(gp.castlingP.col);
            gp.castlingP = null;
        }

        gp.activeP.x = gp.mouse.x - Board.HALF_SQUARE_SIZE;
        gp.activeP.y = gp.mouse.y - Board.HALF_SQUARE_SIZE;
        gp.activeP.col = gp.activeP.getCol(gp.activeP.x);
        gp.activeP.row = gp.activeP.getRow(gp.activeP.y);

        if (gp.activeP.canMove(gp.activeP.col, gp.activeP.row)) {
            gp.canMove = true;
            if (gp.activeP.hittingP != null) gp.simPieces.remove(gp.activeP.hittingP);
            checkCastling();
            if (!isIllegal(gp.activeP) && !opponentCanCaptureKing()) gp.validSquare = true;
        }
    }
}

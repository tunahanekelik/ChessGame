package main;

import piece.*;
import java.awt.*;
import java.util.ArrayList;

// replays were cluttering GamePanel so here is their new home — all the replay state and logic
public class ReplaySystem {

    private GamePanel gp;

    // --- REPLAY STATE ---
    public ArrayList<Piece> replayPieces = new ArrayList<>();
    public ArrayList<String> currentReplayMoves = new ArrayList<>();
    public int replayMoveIndex = 0;
    public int replayGameId = -1;
    public String replayResult = "";
    public String replayDate = "";

    // --- REPLAY SCROLL ---
    public int replayScrollOffset = 0;

    // --- REPLAY ANIMATION ---
    public Piece replayAnimPiece;
    public int animFromX, animFromY, animToX, animToY;
    public long animStartTime;
    public final long ANIM_DURATION = 400;
    public String animLastNotation;

    public ReplaySystem(GamePanel gp) {
        this.gp = gp;
    }

    // setting up the replay for a specific past game
    public void loadReplay(int gameId, String result, String date) {
        replayGameId = gameId;
        replayResult = result;
        replayDate = date;
        currentReplayMoves = DatabaseManager.getMovesForGame(gameId);
        replayMoveIndex = 0;
        resetReplayBoard();
        gp.currentState = GameState.REPLAY;
    }

    // reset replay pieces back to the starting chess position
    public void resetReplayBoard() {
        replayPieces.clear();
        gp.setPiecesInto(replayPieces);
    }

    // apply moves from 0 up to targetCount on the replayPieces list
    public void applyReplayMoves(int targetCount) {
        resetReplayBoard();
        animLastNotation = null;

        for (int i = 0; i < targetCount && i < currentReplayMoves.size(); i++) {
            String notation = currentReplayMoves.get(i);
            animLastNotation = notation;
            if (notation.equals("O-O") || notation.equals("O-O-O")) {
                applyCastlingReplay(notation, i, replayPieces);
            } else {
                parseAndApplyMove(notation, replayPieces);
            }
        }

        // sync every piece's pixel position with its board position
        // doing this so they dont teleport back to their starting squares lol
        for (Piece p : replayPieces) {
            p.x = p.getX(p.col);
            p.y = p.getY(p.row);
        }

        replayMoveIndex = targetCount;
    }

    // starts the slide animation using the notation of the last applied move
    public void startReplayAnim(int targetStep) {
        if (animLastNotation == null) return;

        int fromCol, fromRow, toCol, toRow;

        if (animLastNotation.equals("O-O") || animLastNotation.equals("O-O-O")) {
            int colorRow = ((targetStep - 1) % 2 == 0) ? 7 : 0;
            fromCol = 4;
            fromRow = colorRow;
            toRow = colorRow;
            toCol = animLastNotation.equals("O-O") ? 6 : 2;
        } else {
            int idx = 0;
            if (Character.isUpperCase(animLastNotation.charAt(0))) idx = 1;
            String fromSq = animLastNotation.substring(idx, idx + 2);
            idx += 2;
            if (idx < animLastNotation.length() && animLastNotation.charAt(idx) == 'x') idx++;
            String toSq = animLastNotation.substring(idx, idx + 2);
            fromCol = fromSq.charAt(0) - 'a';
            fromRow = 8 - (fromSq.charAt(1) - '0');
            toCol = toSq.charAt(0) - 'a';
            toRow = 8 - (toSq.charAt(1) - '0');
        }

        replayAnimPiece = null;
        for (Piece p : replayPieces) {
            if (p.col == toCol && p.row == toRow) {
                replayAnimPiece = p;
                break;
            }
        }
        if (replayAnimPiece == null && (animLastNotation.equals("O-O") || animLastNotation.equals("O-O-O"))) {
            for (Piece p : replayPieces) {
                if (p.type == Type.KING && p.row == fromRow) {
                    replayAnimPiece = p;
                    break;
                }
            }
        }

        if (replayAnimPiece == null) return;

        animFromX = fromCol * Board.SQUARE_SIZE;
        animFromY = fromRow * Board.SQUARE_SIZE;
        animToX = toCol * Board.SQUARE_SIZE;
        animToY = toRow * Board.SQUARE_SIZE;

        replayAnimPiece.x = animFromX;
        replayAnimPiece.y = animFromY;

        animStartTime = System.currentTimeMillis();
    }

    // apply a castling move during replay
    public void applyCastlingReplay(String notation, int moveIdx, ArrayList<Piece> list) {
        int row = (moveIdx % 2 == 0) ? 7 : 0;

        Piece king = null, rook = null;
        for (Piece p : list) {
            if (p.type == Type.KING && p.row == row && p.col == 4) {
                king = p;
                break;
            }
        }

        if (notation.equals("O-O")) {
            for (Piece p : list) {
                if (p.type == Type.ROOK && p.row == row && p.col == 7) {
                    rook = p;
                    break;
                }
            }
            if (king != null) king.col = 6;
            if (rook != null) rook.col = 5;
        } else {
            for (Piece p : list) {
                if (p.type == Type.ROOK && p.row == row && p.col == 0) {
                    rook = p;
                    break;
                }
            }
            if (king != null) king.col = 2;
            if (rook != null) rook.col = 3;
        }
    }

    // takes a move notation like "e2e4" or "Nb1c3" and applies it to the given piece list
    public void parseAndApplyMove(String notation, ArrayList<Piece> list) {
        int idx = 0;
        char pieceChar = ' ';
        if (Character.isUpperCase(notation.charAt(0))) {
            pieceChar = notation.charAt(0);
            idx = 1;
        }

        String fromSq = notation.substring(idx, idx + 2);
        idx += 2;
        if (idx < notation.length() && notation.charAt(idx) == 'x') idx++;
        String toSq = notation.substring(idx, idx + 2);

        int fromCol = fromSq.charAt(0) - 'a';
        int fromRow = 8 - (fromSq.charAt(1) - '0');
        int toCol = toSq.charAt(0) - 'a';
        int toRow = 8 - (toSq.charAt(1) - '0');

        Piece movingPiece = null;
        for (Piece p : list) {
            if (p.col == fromCol && p.row == fromRow) {
                if (pieceChar == ' ' && p.type == Type.PAWN) {
                    movingPiece = p;
                    break;
                } else if (pieceChar != ' ' && pieceTypeFromChar(pieceChar) == p.type) {
                    movingPiece = p;
                    break;
                }
            }
        }

        if (movingPiece == null) return;

        Piece captured = null;
        for (Piece p : list) {
            if (p != movingPiece && p.col == toCol && p.row == toRow) {
                captured = p;
                break;
            }
        }
        if (captured != null) list.remove(captured);

        movingPiece.col = toCol;
        movingPiece.row = toRow;
    }

    // convert piece letter char back to Type enum
    public Type pieceTypeFromChar(char c) {
        switch (c) {
            case 'N': return Type.KNIGHT;
            case 'B': return Type.BISHOP;
            case 'R': return Type.ROOK;
            case 'Q': return Type.QUEEN;
            case 'K': return Type.KING;
            default: return Type.PAWN;
        }
    }

    // mouse wheel magic — scroll the move list up and down
    public void handleMouseScroll(int rotation) {
        int totalPairs = (currentReplayMoves.size() + 1) / 2;
        int movesEndY = gp.HEIGHT - 150;
        int maxMoveRows = (movesEndY - 110) / 22;
        int visiblePairs = maxMoveRows - 1;
        int maxOffset = Math.max(0, totalPairs - visiblePairs);

        replayScrollOffset += rotation;
        if (replayScrollOffset < 0) replayScrollOffset = 0;
        if (replayScrollOffset > maxOffset) replayScrollOffset = maxOffset;
    }

    // auto-scroll the move list so the current move stays visible
    public void autoScroll() {
        if (replayMoveIndex == 0) {
            replayScrollOffset = 0;
            return;
        }
        int pairIdx = (replayMoveIndex - 1) / 2;
        int totalPairs = (currentReplayMoves.size() + 1) / 2;
        int movesEndY = gp.HEIGHT - 150;
        int maxMoveRows = (movesEndY - 110) / 22;
        int visiblePairs = maxMoveRows - 1;

        if (pairIdx < replayScrollOffset) {
            replayScrollOffset = pairIdx;
        }
        if (pairIdx >= replayScrollOffset + visiblePairs) {
            replayScrollOffset = pairIdx - visiblePairs + 1;
        }
        int maxOffset = Math.max(0, totalPairs - visiblePairs);
        if (replayScrollOffset > maxOffset) replayScrollOffset = maxOffset;
        if (replayScrollOffset < 0) replayScrollOffset = 0;
    }

    // update the piece slide animation each frame
    public void updateReplayAnim() {
        if (replayAnimPiece == null) return;

        long elapsed = System.currentTimeMillis() - animStartTime;

        if (elapsed >= ANIM_DURATION) {
            replayAnimPiece.x = animToX;
            replayAnimPiece.y = animToY;
            replayAnimPiece = null;
            autoScroll();
        } else {
            float t = (float)elapsed / ANIM_DURATION;
            t = 1 - (1 - t) * (1 - t);
            replayAnimPiece.x = (int)(animFromX + (animToX - animFromX) * t);
            replayAnimPiece.y = (int)(animFromY + (animToY - animFromY) * t);
        }
    }

    // handle prev/next clicks during replay
    public void handleReplayClicks() {
        if (replayAnimPiece != null) return;

        if (!gp.mouse.pressed && gp.mouse.lastClicked) {
            gp.mouse.lastClicked = false;

            int panelX = 800;
            int arrowY = GamePanel.HEIGHT - 130;
            int btnW = 100;
            int btnH = 40;

            if (gp.mouse.x >= 30 && gp.mouse.x <= 130 && gp.mouse.y >= 30 && gp.mouse.y <= 70) {
                gp.currentState = GameState.PAST_GAMES;
            } else if (gp.mouse.x >= 150 && gp.mouse.x <= 270 && gp.mouse.y >= 30 && gp.mouse.y <= 70) {
                gp.currentState = GameState.MENU;
            } else if (gp.mouse.x >= panelX + 30 && gp.mouse.x <= panelX + 130 &&
                     gp.mouse.y >= arrowY && gp.mouse.y <= arrowY + btnH) {
                if (replayMoveIndex > 0) {
                    int target = replayMoveIndex - 1;
                    applyReplayMoves(target);
                    startReplayAnim(target);
                }
            } else if (gp.mouse.x >= panelX + 160 && gp.mouse.x <= panelX + 260 &&
                       gp.mouse.y >= arrowY && gp.mouse.y <= arrowY + btnH) {
                if (replayMoveIndex < currentReplayMoves.size()) {
                    int target = replayMoveIndex + 1;
                    applyReplayMoves(target);
                    startReplayAnim(target);
                }
            } else if (gp.mouse.y < GamePanel.HEIGHT - 140 && gp.mouse.x >= panelX + 15 && gp.mouse.x <= panelX + 285) {
                int movesStartY = 110;
                int moveSpacing = 22;
                int clickedRow = (gp.mouse.y - movesStartY + moveSpacing / 2) / moveSpacing;
                if (clickedRow == 0) {
                    replayMoveIndex = 0;
                    applyReplayMoves(0);
                    autoScroll();
                } else if (clickedRow > 0) {
                    int pairIdx = (clickedRow - 1) + replayScrollOffset;
                    int whiteIdx = pairIdx * 2;
                    int targetMove;
                    if (gp.mouse.x >= panelX + 165) {
                        targetMove = whiteIdx + 2;
                    } else {
                        targetMove = whiteIdx + 1;
                    }
                    if (targetMove > currentReplayMoves.size()) targetMove = currentReplayMoves.size();
                    if (targetMove >= 0 && targetMove <= currentReplayMoves.size()) {
                        replayMoveIndex = targetMove;
                        applyReplayMoves(targetMove);
                        autoScroll();
                    }
                }
            }
        }
    }
}

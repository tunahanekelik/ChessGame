package main;

import piece.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable{
    public static final int WIDTH = 1100; // 1100 for width but it can change
    public static final int HEIGHT = 800; // 800 for height but it can change also
    final int FPS = 60; // for the refreshing game screen
    Thread gameThread; // using thread for the refreshing loop
    Board board = new Board(); //initiating the board class
    public Mouse mouse = new Mouse(); // initiating the mouse classs


    // --- PIECES ---
    public static ArrayList<Piece> pieces = new ArrayList<>(); // works like a back-up list
    public static ArrayList<Piece> simPieces = new ArrayList<>(); // main pieces list
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activeP, checkingP;
    public static Piece castlingP;



    // --- COLOR ---
    public static final int WHITE = 0; // setting the white color as 0
    public static final int BLACK = 1; // setting the black color as 1
    int currentColor = WHITE; // first move is white's so current color will be white

    // --- BOOLEANS ---
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;
    boolean newGameClicked;
    boolean menuClicked;

    // --- UI ---
    ArrayList<String> moveLog = new ArrayList<>(); // for holding the moves that players done
    ArrayList<Point> validSquares = new ArrayList<>(); // showing the squares where active piece can go
    public BufferedImage whitePawnIcon, blackPawnIcon; // for the sidepanel pawn images
    public ArrayList<String[]> pastGamesList = new ArrayList<>(); // cached list of past games for the viewer
    public int pastGamesScroll = 0; // scroll offset for past games list

    // --- THEMES ---
    public int currentThemeIndex = 0; // index into Theme.BOARD_THEMES
    public int currentPieceStyleIndex = 0; // 0 = classic (only one for now)
    public String[] pieceStyleNames = {"Classic", "Modern (coming soon)"};

    // --- DELEGATED CLASSES ---
    public ChessLogic chessLogic; // engine for all rule checks
    public MenuManager menuManager; // handles menu screens
    public ReplaySystem replaySystem; // handles replay state + navigation

    // --- SCREEN STATE ---
    public GameState currentState = GameState.MENU; // we start at the menu screen

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        DatabaseManager.connect(); // connecting to database for storing past games

        // spinning up the delegated classes so GamePanel can finally breathe
        chessLogic = new ChessLogic(this);
        menuManager = new MenuManager(this);
        replaySystem = new ReplaySystem(this);

        try { // loading the pawn images for using them at the turn indicator
            whitePawnIcon = ImageIO.read(getClass().getResourceAsStream("/piece/spr_pawn_white.png"));
            blackPawnIcon = ImageIO.read(getClass().getResourceAsStream("/piece/spr_pawn_black.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // key listener for ESC to go back one level from any screen
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    switch (currentState) {
                        case PLAYING -> goToMenu();
                        case PAST_GAMES -> currentState = GameState.MENU;
                        case REPLAY -> currentState = GameState.PAST_GAMES;
                        case OPTIONS -> currentState = GameState.MENU;
                    }
                }
            }
        });

        // adding some mouse wheel magic for scrolling through moves
        addMouseWheelListener(e -> {
            if (currentState == GameState.REPLAY) {
                replaySystem.handleMouseScroll(e.getWheelRotation());
            } else if (currentState == GameState.PAST_GAMES) {
                int maxRows = (HEIGHT - 180) / 40;
                int maxScroll = Math.max(0, pastGamesList.size() - maxRows);
                pastGamesScroll += e.getWheelRotation();
                if (pastGamesScroll < 0) pastGamesScroll = 0;
                if (pastGamesScroll > maxScroll) pastGamesScroll = maxScroll;
            }
        });

        // we dont set up pieces until the user clicks New Game on the menu
    }

    public void launchGame() {
        gameThread= new Thread(this); // I instantiate the thread
        gameThread.start(); // then call uts start method
        // and this will also call run method (the method that under this method
    }

    // adding initial pieces into the given list (used for both new games and replay)
    public void setPiecesInto(ArrayList<Piece> list) {
        // White Pieces
        list.add(new Pawn(WHITE, 0,6));
        list.add(new Pawn(WHITE, 1,6));
        list.add(new Pawn(WHITE, 2,6));
        list.add(new Pawn(WHITE, 3,6));
        list.add(new Pawn(WHITE, 4,6));
        list.add(new Pawn(WHITE, 5,6));
        list.add(new Pawn(WHITE, 6,6));
        list.add(new Pawn(WHITE, 7,6));
        list.add(new Knight(WHITE, 1,7));
        list.add(new Knight(WHITE, 6,7));
        list.add(new Rook(WHITE, 0,7));
        list.add(new Rook(WHITE, 7,7));
        list.add(new Bishop(WHITE, 2,7));
        list.add(new Bishop(WHITE, 5,7));
        list.add(new Queen(WHITE, 3,7));
        list.add(new King(WHITE, 4,7));

        // Black Pieces
        list.add(new Pawn(BLACK, 0,1));
        list.add(new Pawn(BLACK, 1,1));
        list.add(new Pawn(BLACK, 2,1));
        list.add(new Pawn(BLACK, 3,1));
        list.add(new Pawn(BLACK, 4,1));
        list.add(new Pawn(BLACK, 5,1));
        list.add(new Pawn(BLACK, 6,1));
        list.add(new Pawn(BLACK, 7,1));
        list.add(new Knight(BLACK, 1,0));
        list.add(new Knight(BLACK, 6,0));
        list.add(new Rook(BLACK, 0,0));
        list.add(new Rook(BLACK, 7,0));
        list.add(new Bishop(BLACK, 2,0));
        list.add(new Bishop(BLACK, 5,0));
        list.add(new Queen(BLACK, 3,0));
        list.add(new King(BLACK, 4,0));
    }

    // old method that works with the global pieces list
    public void setPieces() {
        setPiecesInto(pieces);
    }

    // this method will receive both lists the one is source and the other one is target
    public void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {

        target.clear(); // first we are clearing the target list
        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i)); // then adding everything to the source list
            // we can say that we moved everything from target list to source list
        }
    }


    // I created this for the implementing the runnable and I am going to create a game loop
    @Override
    public void run() {

        // GAME LOOP
        double drawInterval = 1000000000 /FPS;
        double delta = 0;
        long lastTime = System.nanoTime(); // used the System.nanoTime for measuring the elapsed time
        long currentTime;

        while(gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update(); // after measuring elapsed time we call update and
                repaint(); // repaint methods once every 1/60 of a second
                delta--;
            }
        }

    }

    // delegating update calls to the right class based on the current screen
    private void update() {

        if (currentState == GameState.MENU) {
            menuManager.handleMenuClicks();
        }
        else if (currentState == GameState.PAST_GAMES) {
            menuManager.handlePastGamesClicks();
        }
        else if (currentState == GameState.OPTIONS) {
            menuManager.handleOptionsClicks();
        }
        else if (currentState == GameState.REPLAY) {
            replaySystem.updateReplayAnim();
            replaySystem.handleReplayClicks();
        }
        else if (currentState == GameState.PLAYING) {
            handlePlayingClicks();
        }

        // reset the click flag after we processed it for this frame
        mouse.lastClicked = false;
    }

    // all the playing logic (same as before but now delegating rules to chessLogic)
    private void handlePlayingClicks() {

        if (promotion) {
            promoting();
            return;
        }

        if (gameOver || stalemate) {
            // handle buttons when game is over
            if (mouse.pressed) {
                int panelX = 800;
                // New Game button
                if (mouse.x >= panelX + 50 && mouse.x <= panelX + 145 &&
                    mouse.y >= HEIGHT - 35 && mouse.y <= HEIGHT - 10) {
                    newGameClicked = true;
                }
                // Main Menu button
                if (mouse.x >= panelX + 155 && mouse.x <= panelX + 250 &&
                    mouse.y >= HEIGHT - 35 && mouse.y <= HEIGHT - 10) {
                    menuClicked = true;
                }
            }
            if (!mouse.pressed && newGameClicked) {
                newGameClicked = false;
                restartGame();
            }
            if (!mouse.pressed && menuClicked) {
                menuClicked = false;
                goToMenu();
            }
            return;
        }

        // --- MOUSE LEFT BUTTON PRESSED ---
        if (mouse.pressed) {
            if (activeP == null) {

                // snapshot copy to avoid ConcurrentModificationException from computeValidSquares
                for (Piece piece : new ArrayList<>(simPieces)) {

                    // if everything is correct then that means players mouse is on this piece
                    if (piece.color == currentColor &&
                            piece.col == mouse.x/Board.SQUARE_SIZE &&
                            piece.row == mouse.y/Board.SQUARE_SIZE) {

                        activeP = piece; // we are picking the piece up
                        chessLogic.computeValidSquares(); // and calculating the squares that it can go
                    }
                }
            } else {
                // if player is holding a piece simulate the movement
                chessLogic.simulate();
            }
        }

        // --- MOUSE BUTTON RELEASED---
        if (!mouse.pressed) {

            if (activeP != null) {

                if (validSquare) {

                    // if move is confirmed update the piece list
                    // in case a piece has been captured and removed during the simulation
                    copyPieces(simPieces, pieces);

                    // record the move for the log
                    // doing this BEFORE updatePosition so preCol/preRow still hold the old square
                    if (castlingP != null) { // if its a castling move
                        int diff = activeP.col - activeP.preCol;
                        moveLog.add(diff == 2 ? "O-O" : "O-O-O"); // adding O-O for king side or O-O-O for queen side
                    } else {
                        String move = pieceLetter(activeP); // piece letter like N for knight
                        move += toAlgebraic(activeP.preCol, activeP.preRow); // the square it came from
                        if (activeP.hittingP != null) {
                            move += "x"; // if it capture we add x
                        }
                        move += toAlgebraic(activeP.col, activeP.row); // the square it goes to
                        moveLog.add(move); // adding the move string to log
                    }

                    // now that we logged the move, update the positions so the visuals catch up
                    activeP.updatePosition();
                    if (castlingP != null) {
                        castlingP.updatePosition();
                    }

                    if (chessLogic.isKingInCheck() && chessLogic.isCheckmate()) {
                        // --- CHECK ---
                        gameOver = true;
                        String result = (currentColor == WHITE) ? "1-0" : "0-1";
                        DatabaseManager.saveGame(result, moveLog);
                    }
                    else if (chessLogic.isStalemate()) {
                        stalemate = true;
                        DatabaseManager.saveGame("1/2-1/2", moveLog);
                    }
                    else { // the game is still playable
                        if (canPromote()) {
                            promotion = true;
                        }
                        else {
                            changePlayer();
                        }
                    }
                }
                else {
                    // if the move is not valis so reset everything
                    copyPieces(pieces, simPieces);
                    activeP.resetPosition();
                    validSquares.clear(); // clearing the squares cause activeP is null now
                    activeP = null;
                }
            }
        }
    }

    // setting up pieces and switching to playing mode
    public void startNewGame() {

        pieces.clear();
        simPieces.clear();
        promoPieces.clear();

        setPieces(); // putting new pieces on their starting squares
        copyPieces(pieces, simPieces); // syncing the lists like we did at the start

        gameOver = false;
        stalemate = false;
        promotion = false;
        canMove = false;
        validSquare = false;
        newGameClicked = false;

        activeP = null;
        checkingP = null;
        castlingP = null;

        currentColor = WHITE; // white always starts
        moveLog.clear(); // clearing the move history
        validSquares.clear(); // clearing the highlighted squares

        currentState = GameState.PLAYING; // switching to the game screen
    }

    // resetting everything so we can start a fresh new game
    private void restartGame() {

        gameOver = false;
        stalemate = false;
        promotion = false;
        canMove = false;
        validSquare = false;
        newGameClicked = false;

        activeP = null;
        checkingP = null;
        castlingP = null;

        currentColor = WHITE; // white always starts

        moveLog.clear(); // clearing the move history
        validSquares.clear(); // clearing the highlighted squares
        pieces.clear(); // removing old pieces
        simPieces.clear(); // removing old sim pieces
        promoPieces.clear(); // removing promotion options

        setPieces(); // putting new pieces on their starting squares
        copyPieces(pieces, simPieces); // syncing the lists like we did at the start
    }

    // going back to the main menu from the game screen
    public void goToMenu() {

        gameOver = false;
        stalemate = false;
        promotion = false;
        newGameClicked = false;
        menuClicked = false;

        activeP = null;
        checkingP = null;
        castlingP = null;

        validSquares.clear();

        currentState = GameState.MENU;
    }

    // --- PROMOTION ---
    public boolean canPromote() {

        // promotion can only be done at the end of the board with pawn
        if (activeP.type == Type.PAWN) {
            if (currentColor == WHITE && activeP.row == 0 || // for white pawn, end location is row 0/1
                    currentColor == BLACK && activeP.row == 7) { // for black pawn. end location is row 7/8
                promoPieces.clear();
                // putting the promotion UI in the center of the action instead of the side panel
                promoPieces.add(new Rook(currentColor, 2, 3));
                promoPieces.add(new Knight(currentColor, 3, 3));
                promoPieces.add(new Bishop(currentColor, 4, 3));
                promoPieces.add(new Queen(currentColor, 5, 3));
                return true;

            }
        }

        return false;
    }

    private void promoting() {

        if (mouse.pressed) {
            for (Piece piece : promoPieces) { // if piece is reached the final square and ready to promote
                if (piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE) { // according to mouse positioning
                    switch (piece.type) {
                        case ROOK: simPieces.add(new Rook(currentColor, activeP.col, activeP.row));
                        break; // switch pawn to rook and add it to the current position and break
                        case KNIGHT: simPieces.add(new Knight(currentColor, activeP.col, activeP.row));
                        break; // switch pawn to knight and add it to the current position and break
                        case BISHOP: simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));
                        break; // switch pawn to bishop and add it to the current position and break
                        case QUEEN: simPieces.add(new Queen(currentColor, activeP.col, activeP.row));
                        break; // switch pawn to queen and add it to the current position and break
                        default: break;
                    }
                    simPieces.remove(activeP);
                    copyPieces(simPieces, pieces);
                    validSquares.clear(); // promoting so no valid squares needed
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }

    }

    // created a method for switching between players like turn-based
    // (if white does it moves change the turn to black)
    private void changePlayer() {
        if(currentColor == WHITE) {
            currentColor = BLACK;
            // resetting black's two stepped status
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        }
        else {
            currentColor = WHITE;
            // resetting white's two stepped status
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }
        validSquares.clear(); // clear the squares because turn changed
        activeP = null;
    }

    // converting the given column and row number to the letter and number like a1, e4, h8 etc
    private String toAlgebraic(int col, int row) {
        return "" + (char)('a' + col) + (8 - row);
    }

    // getting the short letter of the type of the piece for the move log (N for knight, B for bishop etc)
    private String pieceLetter(Piece p) {
        switch (p.type) {
            case PAWN: return ""; // pawns don't have a letter they just show the square
            case ROOK: return "R";
            case KNIGHT: return "N";
            case BISHOP: return "B";
            case QUEEN: return "Q";
            case KING: return "K";
            default: return "";
        }
    }

    // drawing the pawn icons with the same method as the pawn class for not looking ugly
    public void drawPawnIcon(Graphics2D g2D, BufferedImage img, int x, int y, int targetHeight) {
        if (img == null) return;
        double scale = targetHeight / 70.0;
        int marginX = (int)(25 * scale);
        int marginY = (int)(15 * scale);
        int w = (int)(50 * scale);
        int h = (int)(70 * scale);
        g2D.drawImage(img, x + marginX, y + marginY, w, h, null);
    }

    // static wrapper so King.java etc can still check attacked squares without knowing about ChessLogic
    public static boolean isSquareAttacked(int col, int row, int byColor) {
        return ChessLogic.isSquareAttacked(simPieces, col, row, byColor);
    }

    // this component will handle all the drawing stuff
    // also after creating board class we are calling the draw method from Board class
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D)g; // there was two graphics so we changed it here

        // draw different screens based on current state
        if (currentState == GameState.MENU) {
            menuManager.drawMenu(g2D);
        }
        else if (currentState == GameState.PAST_GAMES) {
            menuManager.drawPastGames(g2D);
        }
        else if (currentState == GameState.OPTIONS) {
            menuManager.drawOptions(g2D);
        }
        else if (currentState == GameState.REPLAY) {
            drawReplay(g2D);
        }
        else if (currentState == GameState.PLAYING) {
            drawGame(g2D);
        }
    }

    // drawing the replay screen with board, move navigation, and move list
    private void drawReplay(Graphics2D g2D) {

        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // dark background behind the board area
        g2D.setColor(new Color(30, 30, 30));
        g2D.fillRect(0, 0, WIDTH, HEIGHT);

        // Back button — rounding it off and adding that warm latte hover
        int btnArc = 15;
        boolean backHover = mouse.x >= 30 && mouse.x <= 130 && mouse.y >= 30 && mouse.y <= 70;
        g2D.setColor(backHover ? new Color(110, 75, 50) : new Color(50, 50, 50));
        g2D.fillRoundRect(30, 30, 100, 40, btnArc, btnArc);
        if (backHover) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(new Color(180, 140, 100, 120));
            g2D.drawRoundRect(30, 30, 100, 40, btnArc, btnArc);
            g2D.setStroke(new BasicStroke(1));
        }
        g2D.setFont(new Font("Courier New", Font.BOLD, 14));
        g2D.setColor(Color.WHITE);
        g2D.drawString("Back", 60, 55);

        // Main Menu button (next to Back)
        boolean mmHover = mouse.x >= 150 && mouse.x <= 270 && mouse.y >= 30 && mouse.y <= 70;
        g2D.setColor(mmHover ? new Color(110, 75, 50) : new Color(66, 133, 244));
        g2D.fillRoundRect(150, 30, 120, 40, btnArc, btnArc);
        if (mmHover) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(new Color(180, 140, 100, 120));
            g2D.drawRoundRect(150, 30, 120, 40, btnArc, btnArc);
            g2D.setStroke(new BasicStroke(1));
        }
        g2D.setFont(new Font("Courier New", Font.BOLD, 14));
        g2D.setColor(Color.WHITE);
        g2D.drawString("Main Menu", 165, 55);

        // title with game info
        g2D.setFont(new Font("Courier New", Font.BOLD, 22));
        g2D.setColor(Color.WHITE);
        String title = "Game #" + replaySystem.replayGameId + " \u2014 " + replaySystem.replayResult;
        if (!replaySystem.replayDate.isEmpty()) {
            title += "  (" + replaySystem.replayDate + ")";
        }
        FontMetrics fm = g2D.getFontMetrics();
        g2D.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 55);

        // draw the board with replay pieces
        board.draw(g2D, Theme.BOARD_THEMES[currentThemeIndex]);
        for (Piece p : replaySystem.replayPieces) {
            p.draw(g2D);
        }

        // board coordinates
        g2D.setFont(new Font("Courier New", Font.PLAIN, 14));
        for (int i = 0; i < 8; i++) {
            g2D.setColor(new Color(100, 100, 100));
            g2D.drawString("" + (char)('a' + i), i * Board.SQUARE_SIZE + 85, 790);
            g2D.drawString("" + (8 - i), 5, i * Board.SQUARE_SIZE + 30);
        }

        // --- SIDE PANEL ---
        int panelX = 800;
        int panelW = 300;

        g2D.setColor(new Color(45, 45, 45));
        g2D.fillRect(panelX, 0, panelW, HEIGHT);
        g2D.setColor(new Color(60, 60, 60));
        g2D.drawLine(panelX, 0, panelX, HEIGHT);

        g2D.setFont(new Font("Courier New", Font.BOLD, 16));
        g2D.setColor(Color.WHITE);
        g2D.drawString("Moves", panelX + 15, 75);

        g2D.setColor(new Color(55, 55, 55));
        g2D.drawLine(panelX + 15, 85, panelX + panelW - 15, 85);

        // fixing the replay log so it doesn't hide behind buttons — cap it before the nav bar
        g2D.setFont(new Font("Courier New", Font.PLAIN, 13));
        int movesStartY = 110;
        int moveSpacing = 22;
        int movesEndY = HEIGHT - 150; // stop before the prev/next buttons
        int maxMoveRows = (movesEndY - movesStartY) / moveSpacing;

        // initial position row (move 0)
        boolean initialActive = (replaySystem.replayMoveIndex == 0);
        if (initialActive) {
            g2D.setColor(new Color(50, 50, 70));
            g2D.fillRect(panelX + 15, movesStartY - 16, panelW - 30, moveSpacing);
        }
        g2D.setColor(initialActive ? new Color(255, 200, 100) : new Color(120, 120, 120));
        g2D.drawString("0", panelX + 18, movesStartY);
        g2D.setColor(initialActive ? new Color(255, 200, 100) : new Color(150, 150, 150));
        g2D.drawString("Initial position", panelX + 48, movesStartY);

        int totalPairs = (replaySystem.currentReplayMoves.size() + 1) / 2;
        int visiblePairs = Math.min(totalPairs - replaySystem.replayScrollOffset, maxMoveRows - 1);

        for (int j = 0; j < visiblePairs; j++) {
            int i = replaySystem.replayScrollOffset + j;
            int y = movesStartY + (j + 1) * moveSpacing;

            boolean whiteActive = (replaySystem.replayMoveIndex == i * 2 + 1);
            boolean blackActive = (replaySystem.replayMoveIndex == i * 2 + 2);

            if (whiteActive || blackActive) {
                g2D.setColor(new Color(50, 50, 70));
                g2D.fillRect(panelX + 15, y - 16, panelW - 30, moveSpacing);
            }

            g2D.setColor(new Color(120, 120, 120));
            g2D.drawString(String.format("%-3d", i + 1), panelX + 18, y);

            int whiteIdx = i * 2;
            g2D.setColor(whiteActive ? new Color(255, 200, 100) : new Color(200, 200, 200));
            if (whiteIdx < replaySystem.currentReplayMoves.size()) {
                g2D.drawString(replaySystem.currentReplayMoves.get(whiteIdx), panelX + 48, y);
            }

            g2D.setColor(blackActive ? new Color(255, 200, 100) : new Color(140, 140, 140));
            if (whiteIdx + 1 < replaySystem.currentReplayMoves.size()) {
                g2D.drawString(replaySystem.currentReplayMoves.get(whiteIdx + 1), panelX + 165, y);
            }
        }

        // tossing the old text buttons into the trash — now we got a cool latte-colored scrollbar
        int scrollbarX = panelX + panelW - 12;
        int scrollbarW = 6;
        int scrollTrackH = movesEndY - movesStartY;
        g2D.setColor(new Color(55, 40, 30));
        g2D.fillRoundRect(scrollbarX, movesStartY, scrollbarW, scrollTrackH, 3, 3);
        if (totalPairs > 0) {
            int maxOffset = Math.max(0, totalPairs - visiblePairs);
            if (maxOffset > 0) {
                int handleH = Math.max(20, scrollTrackH * visiblePairs / totalPairs);
                int handleY = movesStartY + (scrollTrackH - handleH) * replaySystem.replayScrollOffset / maxOffset;
                g2D.setColor(new Color(210, 190, 160));
                g2D.fillRoundRect(scrollbarX, handleY, scrollbarW, handleH, 3, 3);
                g2D.setColor(new Color(180, 155, 130));
                g2D.drawRoundRect(scrollbarX, handleY, scrollbarW, handleH, 3, 3);
            } else {
                int handleH = scrollTrackH;
                g2D.setColor(new Color(210, 190, 160));
                g2D.fillRoundRect(scrollbarX, movesStartY, scrollbarW, handleH, 3, 3);
            }
        }

        // move counter indicator at the bottom of the side panel
        g2D.setFont(new Font("Courier New", Font.BOLD, 14));
        g2D.setColor(new Color(180, 180, 180));
        String counter = "Move " + replaySystem.replayMoveIndex + " / " + replaySystem.currentReplayMoves.size();
        fm = g2D.getFontMetrics();
        g2D.drawString(counter, panelX + 15, HEIGHT - 80);

        // --- NAVIGATION BUTTONS with rounded corners and latte hover ---
        int arrowY = HEIGHT - 130;
        int btnW = 100;
        int btnH = 40;

        // Previous button
        boolean prevHover = mouse.x >= panelX + 30 && mouse.x <= panelX + 130 &&
                            mouse.y >= arrowY && mouse.y <= arrowY + btnH;
        boolean prevActive = replaySystem.replayMoveIndex > 0;
        g2D.setColor(prevHover && prevActive ? new Color(110, 75, 50) : (prevActive ? new Color(66, 133, 244) : new Color(60, 60, 60)));
        g2D.fillRoundRect(panelX + 30, arrowY, btnW, btnH, btnArc, btnArc);
        if (prevHover && prevActive) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(new Color(180, 140, 100, 120));
            g2D.drawRoundRect(panelX + 30, arrowY, btnW, btnH, btnArc, btnArc);
            g2D.setStroke(new BasicStroke(1));
        }
        g2D.setFont(new Font("Courier New", Font.BOLD, 20));
        g2D.setColor(prevActive ? Color.WHITE : new Color(100, 100, 100));
        int prevTW = g2D.getFontMetrics().stringWidth("\u25c0 Prev");
        g2D.drawString("\u25c0 Prev", panelX + 30 + (btnW - prevTW) / 2, arrowY + 27);

        // Next button
        boolean nextHover = mouse.x >= panelX + 160 && mouse.x <= panelX + 260 &&
                            mouse.y >= arrowY && mouse.y <= arrowY + btnH;
        boolean nextActive = replaySystem.replayMoveIndex < replaySystem.currentReplayMoves.size();
        g2D.setColor(nextHover && nextActive ? new Color(110, 75, 50) : (nextActive ? new Color(66, 133, 244) : new Color(60, 60, 60)));
        g2D.fillRoundRect(panelX + 160, arrowY, btnW, btnH, btnArc, btnArc);
        if (nextHover && nextActive) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(new Color(180, 140, 100, 120));
            g2D.drawRoundRect(panelX + 160, arrowY, btnW, btnH, btnArc, btnArc);
            g2D.setStroke(new BasicStroke(1));
        }
        g2D.setFont(new Font("Courier New", Font.BOLD, 20));
        g2D.setColor(nextActive ? Color.WHITE : new Color(100, 100, 100));
        int nextTW = g2D.getFontMetrics().stringWidth("Next \u25b6");
        g2D.drawString("Next \u25b6", panelX + 160 + (btnW - nextTW) / 2, arrowY + 27);
    }

    // drawing the game board and everything on it
    private void drawGame(Graphics2D g2D) {

        // BOARD
        board.draw(g2D, Theme.BOARD_THEMES[currentThemeIndex]); // calling board draw method with current theme

        // drawing the pixelated squares that the active piece can go (behind pieces so dots dont cover them)
        if (activeP != null && !promotion) {
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // pixelated look
            for (Point sq : validSquares) {
                boolean isCapture = false;
                for (Piece p : simPieces) {
                    if (p != activeP && p.col == sq.x && p.row == sq.y) {
                        isCapture = true; // there is a enemy piece on this square so its a capture
                        break;
                    }
                }
                if (!isCapture) { // if the square is empty we draw a small green pixel square
                    int px = sq.x * Board.SQUARE_SIZE + 40;
                    int py = sq.y * Board.SQUARE_SIZE + 40;
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2D.setColor(new Color(100, 190, 100));
                    g2D.fillRect(px, py, 20, 20);
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
        }

        // PIECES
        for (Piece p : simPieces) {
            p.draw(g2D); // calling piece draw method
        }

        // drawing red squares on enemy pieces that we can capture (on top of pieces so ring is visible)
        if (activeP != null && !promotion) {
            for (Point sq : validSquares) {
                boolean isCapture = false;
                for (Piece p : simPieces) {
                    if (p != activeP && p.col == sq.x && p.row == sq.y) {
                        isCapture = true;
                        break;
                    }
                }
                if (isCapture) { // red square around the enemy piece for showing we can capture it
                    int px = sq.x * Board.SQUARE_SIZE + 5;
                    int py = sq.y * Board.SQUARE_SIZE + 5;
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2D.setColor(new Color(220, 50, 50));
                    g2D.setStroke(new BasicStroke(4));
                    g2D.drawRect(px, py, Board.SQUARE_SIZE - 10, Board.SQUARE_SIZE - 10);
                    g2D.setStroke(new BasicStroke(1)); // setting it back to normal
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // restoring it
        }

        if(activeP != null) {
            if (canMove) {
                if (chessLogic.isIllegal(activeP) || chessLogic.opponentCanCaptureKing()) {
                    g2D.setColor(Color.gray);
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2D.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
                else {
                    g2D.setColor(Color.white);
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2D.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }

                // draw the active piece in the end so it won't be hidden by the board or the colored square
                activeP.draw(g2D);

        }

        // --- BOARD COORDINATES ---
        g2D.setFont(new Font("Courier New", Font.PLAIN, 14));
        for (int i = 0; i < 8; i++) {
            g2D.setColor(new Color(100, 100, 100));
            g2D.drawString("" + (char)('a' + i), i * Board.SQUARE_SIZE + 85, 790); // letters at the bottom of board
            g2D.drawString("" + (8 - i), 5, i * Board.SQUARE_SIZE + 30); // numbers at the left of board
        }

        // --- SIDE PANEL (the area on the right for showing info about game) ---
        int panelX = 800; // the board ends at 800 so side panel starts from there
        int panelW = 300; // we have 300px width for the side panel

        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2D.setColor(new Color(30, 30, 30)); // dark background for the panel
        g2D.fillRect(panelX, 0, panelW, HEIGHT);

        g2D.setColor(new Color(50, 50, 50));
        g2D.drawLine(panelX, 0, panelX, HEIGHT); // border between board and panel

        // Turn indicator showing whos playing with a pawn icon
        BufferedImage turnPawn = (currentColor == WHITE) ? whitePawnIcon : blackPawnIcon;
        drawPawnIcon(g2D, turnPawn, panelX + 18, 12, 42);

        g2D.setFont(new Font("Courier New", Font.BOLD, 20));
        g2D.setColor(Color.WHITE);
        String turnText = (currentColor == WHITE) ? "White's Turn" : "Black's Turn";
        g2D.drawString(turnText, panelX + 75, 48);

        g2D.setColor(new Color(55, 55, 55));
        g2D.drawLine(panelX + 15, 75, panelX + panelW - 15, 75); // line under turn indicator

        g2D.setFont(new Font("Courier New", Font.BOLD, 13));
        g2D.setColor(new Color(150, 150, 150));
        g2D.drawString("MOVES", panelX + 15, 100);

        // Column headers with pawn icons for understanding which move is which colors
        int legendY = 106;
        drawPawnIcon(g2D, whitePawnIcon, panelX + 18, legendY, 22);
        g2D.setFont(new Font("Courier New", Font.BOLD, 11));
        g2D.setColor(new Color(200, 200, 200));
        g2D.drawString("White", panelX + 62, legendY + 17);

        drawPawnIcon(g2D, blackPawnIcon, panelX + 163, legendY, 22);
        g2D.setColor(new Color(140, 140, 140));
        g2D.drawString("Black", panelX + 207, legendY + 17);

        // Moves list showing the moves that players made so far
        g2D.setFont(new Font("Courier New", Font.PLAIN, 13));
        int movesStartY = 140;
        int moveSpacing = 22;
        int maxVisible = (700 - movesStartY) / moveSpacing; // how many moves can we show at once

        int totalPairs = (moveLog.size() + 1) / 2; // counting move pairs (one white + one black)
        int startPair = Math.max(0, totalPairs - maxVisible); // start from latest if too many

        for (int i = startPair; i < totalPairs; i++) {
            int whiteIdx = i * 2;
            int y = movesStartY + (i - startPair) * moveSpacing;

            g2D.setColor(new Color(120, 120, 120));
            g2D.drawString(String.format("%-3d", i + 1), panelX + 18, y); // drawing move number

            g2D.setColor(Color.WHITE);
            if (whiteIdx < moveLog.size()) {
                g2D.drawString(moveLog.get(whiteIdx), panelX + 48, y); // drawing whites move
            }

            if (whiteIdx + 1 < moveLog.size()) {
                g2D.drawString(moveLog.get(whiteIdx + 1), panelX + 165, y); // drawing blacks move
            }
        }

        // Past games count at the very bottom
        g2D.setFont(new Font("Courier New", Font.PLAIN, 11));
        g2D.setColor(new Color(100, 100, 100));
        int pastGames = DatabaseManager.getTotalGames();
        g2D.drawString("Past Games: " + pastGames, panelX + 15, HEIGHT - 12);

        // Status bar at the bottom for check, checkmate or stalemate messages
        if (gameOver || stalemate || (checkingP != null && !promotion)) {
            int sbX = panelX + 10;
            int sbW = panelW - 20;
            g2D.setColor(new Color(45, 45, 45));
            g2D.fillRect(sbX, HEIGHT - 75, sbW, 65);
            g2D.setColor(new Color(55, 55, 55));
            g2D.drawRect(sbX, HEIGHT - 75, sbW, 65);

            String msg;
            Color msgColor;
            if (gameOver) {
                msg = (currentColor == WHITE) ? "White Wins!" : "Black Wins!";
                msgColor = new Color(76, 175, 80);
            } else if (stalemate) {
                msg = "Stalemate";
                msgColor = new Color(200, 200, 200);
            } else {
                msg = "Check!";
                msgColor = new Color(244, 67, 54);
            }

            // centering the status message like a pro
            g2D.setFont(new Font("Courier New", Font.BOLD, 18));
            int msgW = g2D.getFontMetrics().stringWidth(msg);
            int msgX = sbX + (sbW - msgW) / 2;
            g2D.setColor(msgColor);
            g2D.drawString(msg, msgX, HEIGHT - 42);

            // Two round buttons visible when the game is over — giving 'em the latte treatment
            if (gameOver || stalemate) {
                int btnArc = 15;
                int btnY = HEIGHT - 33;
                int btnH2 = 26;

                // New Game button
                int ngX = panelX + 48;
                int ngW = 96;
                boolean ngHover = mouse.x >= ngX && mouse.x <= ngX + ngW &&
                                  mouse.y >= btnY && mouse.y <= btnY + btnH2;
                g2D.setColor(ngHover ? new Color(110, 75, 50) : new Color(66, 133, 244));
                g2D.fillRoundRect(ngX, btnY, ngW, btnH2, btnArc, btnArc);
                if (ngHover) {
                    g2D.setStroke(new BasicStroke(2));
                    g2D.setColor(new Color(180, 140, 100, 120));
                    g2D.drawRoundRect(ngX, btnY, ngW, btnH2, btnArc, btnArc);
                    g2D.setStroke(new BasicStroke(1));
                }
                g2D.setColor(Color.WHITE);
                g2D.setFont(new Font("Courier New", Font.BOLD, 12));
                int ngTW = g2D.getFontMetrics().stringWidth("New Game");
                g2D.drawString("New Game", ngX + (ngW - ngTW) / 2, btnY + 18);

                // Main Menu button
                int mmX = panelX + 155;
                int mmW = 96;
                boolean mmHover = mouse.x >= mmX && mouse.x <= mmX + mmW &&
                                  mouse.y >= btnY && mouse.y <= btnY + btnH2;
                g2D.setColor(mmHover ? new Color(110, 75, 50) : new Color(80, 80, 80));
                g2D.fillRoundRect(mmX, btnY, mmW, btnH2, btnArc, btnArc);
                if (mmHover) {
                    g2D.setStroke(new BasicStroke(2));
                    g2D.setColor(new Color(180, 140, 100, 120));
                    g2D.drawRoundRect(mmX, btnY, mmW, btnH2, btnArc, btnArc);
                    g2D.setStroke(new BasicStroke(1));
                }
                g2D.setColor(Color.WHITE);
                int mmTW = g2D.getFontMetrics().stringWidth("Main Menu");
                g2D.drawString("Main Menu", mmX + (mmW - mmTW) / 2, btnY + 18);
            }
        }

        // Board overlay for game end showing the result on the board
        if (gameOver || stalemate) {
            g2D.setFont(new Font("Courier New", Font.BOLD, 70));
            g2D.setColor(new Color(0, 0, 0, 180));
            g2D.fillRect(0, 320, 800, 100);
            if (gameOver) {
                String winner = (currentColor == WHITE) ? "White Wins!" : "Black Wins!";
                g2D.setColor(new Color(76, 175, 80));
                int ww = g2D.getFontMetrics().stringWidth(winner);
                g2D.drawString(winner, (800 - ww) / 2, 393);
            } else {
                g2D.setColor(new Color(200, 200, 200));
                String stal = "Stalemate";
                int sw = g2D.getFontMetrics().stringWidth(stal);
                g2D.drawString(stal, (800 - sw) / 2, 393);
            }
        }

        // --- PROMOTION UI (drawn last so it sits on top of EVERYTHING including the side panel) ---
        if (promotion) {
            // full-screen dark overlay so the game freezes visually
            g2D.setColor(new Color(0, 0, 0, 200));
            g2D.fillRect(0, 0, WIDTH, HEIGHT);

            g2D.setFont(new Font("Courier New", Font.BOLD, 28));
            g2D.setColor(Color.WHITE);
            String promMsg = "Promote pawn to:";
            int pmW = g2D.getFontMetrics().stringWidth(promMsg);
            g2D.drawString(promMsg, (WIDTH - pmW) / 2, 320);

            for (Piece piece : promoPieces) {
                g2D.drawImage(piece.image, piece.getDrawX(), piece.getDrawY(),
                        piece.getWidth(), piece.getHeight(), null);
            }
        }
    }
}

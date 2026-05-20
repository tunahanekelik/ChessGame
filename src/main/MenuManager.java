package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

// all the non-game screen drawing and clicking lives here so GamePanel can stop being a monolith
public class MenuManager {

    private GamePanel gp;

    public MenuManager(GamePanel gp) {
        this.gp = gp;
    }

    // ==================== DRAWING ====================

    // drawing the main menu screen — coffee shop vibes edition ☕
    public void drawMenu(Graphics2D g2D) {

        // --- 1. RADIAL GRADIENT BACKGROUND ---
        int centreX = GamePanel.WIDTH / 2;
        int centreY = GamePanel.HEIGHT / 3;
        float radius = Math.max(GamePanel.WIDTH, GamePanel.HEIGHT) * 0.75f;
        RadialGradientPaint grad = new RadialGradientPaint(
            centreX, centreY, radius,
            new float[]{0f, 0.5f, 1f},
            new Color[]{new Color(80, 55, 40), new Color(50, 35, 25), new Color(30, 20, 15)}
        );
        g2D.setPaint(grad);
        g2D.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // --- 2. FAINT CHESSBOARD PATTERN (opacity 0.05) ---
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
        int tileSize = 40;
        for (int r = 0; r < GamePanel.HEIGHT; r += tileSize) {
            for (int c = 0; c < GamePanel.WIDTH; c += tileSize) {
                if (((r / tileSize) + (c / tileSize)) % 2 == 0) {
                    g2D.setColor(Color.WHITE);
                    g2D.fillRect(c, r, tileSize, tileSize);
                }
            }
        }
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // --- 3. TITLE ---
        g2D.setFont(new Font("Courier New", Font.BOLD, 72));
        g2D.setColor(new Color(245, 235, 220));
        String title = "CHESS";
        FontMetrics titleFm = g2D.getFontMetrics();
        int titleWidth = titleFm.stringWidth(title);
        int titleX = (GamePanel.WIDTH - titleWidth) / 2;
        int titleY = 165;

        g2D.setColor(new Color(20, 12, 8));
        g2D.drawString(title, titleX + 3, titleY + 3);
        g2D.setColor(new Color(245, 235, 220));
        g2D.drawString(title, titleX, titleY);

        // --- 4. PROJECT INFO SUBTITLES ---
        g2D.setFont(new Font("Courier New", Font.BOLD, 15));
        g2D.setColor(new Color(180, 155, 130));
        String[] infoLines = {
            "CEN302 Software Project Development",
            "Final Project",
            "Created by Tunahan Ferramuz Ekelik",
            "22040102034"
        };
        int infoStartY = 210;
        int infoSpacing = 22;
        for (int li = 0; li < infoLines.length; li++) {
            FontMetrics fm = g2D.getFontMetrics();
            int lx = (GamePanel.WIDTH - fm.stringWidth(infoLines[li])) / 2;
            g2D.drawString(infoLines[li], lx, infoStartY + li * infoSpacing);
        }

        // --- 5. FLOATING PAWN ICONS ---
        if (gp.whitePawnIcon != null) {
            float floatOffset = (float)(Math.sin(System.currentTimeMillis() * 0.002) * 8);
            
            // Calculating balanced, mathematically perfect symmetrical positions relative to the "CHESS" text bounds
            int targetHeight = 60;
            double scale = targetHeight / 70.0;
            int marginX = (int)(25 * scale); // 21 pixels
            int w = (int)(50 * scale);       // 42 pixels
            int gap = 60; // comfortable visual padding between text edges and pawn icons
            
            int whitePawnX = titleX - gap - marginX - w;
            int blackPawnX = titleX + titleWidth + gap - marginX;
            
            gp.drawPawnIcon(g2D, gp.whitePawnIcon, whitePawnX, (int)(110 + floatOffset), targetHeight);
            gp.drawPawnIcon(g2D, gp.blackPawnIcon, blackPawnX, (int)(110 - floatOffset), targetHeight);
        }

        // --- 6. MODERN ROUNDED BUTTONS with hover glow ---
        String[] labels = {"New Game", "Past Games", "Options"};
        int btnW = 260, btnH = 55;
        int btnX = (GamePanel.WIDTH - btnW) / 2;
        int startY = 380;
        int arc = 15;

        for (int i = 0; i < labels.length; i++) {
            int btnY = startY + i * 75;

            boolean hovered = gp.mouse.x >= btnX && gp.mouse.x <= btnX + btnW &&
                              gp.mouse.y >= btnY && gp.mouse.y <= btnY + btnH;

            g2D.setColor(new Color(15, 10, 8, 100));
            g2D.fillRoundRect(btnX + 2, btnY + 2, btnW, btnH, arc, arc);

            if (hovered) {
                g2D.setColor(new Color(110, 75, 50));
                g2D.fillRoundRect(btnX, btnY, btnW, btnH, arc, arc);
                g2D.setStroke(new BasicStroke(2));
                g2D.setColor(new Color(180, 140, 100, 120));
                g2D.drawRoundRect(btnX, btnY, btnW, btnH, arc, arc);
                g2D.setStroke(new BasicStroke(1));
            } else {
                g2D.setColor(new Color(70, 45, 30));
                g2D.fillRoundRect(btnX, btnY, btnW, btnH, arc, arc);
                g2D.setColor(new Color(90, 65, 45));
                g2D.drawRoundRect(btnX, btnY, btnW, btnH, arc, arc);
            }

            g2D.setFont(new Font("Courier New", Font.BOLD, 16));
            g2D.setColor(new Color(245, 235, 220));
            FontMetrics fm = g2D.getFontMetrics();
            int textX = btnX + (btnW - fm.stringWidth(labels[i])) / 2;
            int textY = btnY + (btnH + fm.getAscent()) / 2 - 3;
            g2D.drawString(labels[i], textX, textY);
        }

        // --- 7. VERSION ---
        g2D.setFont(new Font("Courier New", Font.BOLD, 11));
        g2D.setColor(new Color(100, 75, 55));
        g2D.drawString("v1.0", GamePanel.WIDTH - 50, GamePanel.HEIGHT - 15);

        // --- 8. RETRO FILM GRAIN ---
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
        g2D.setColor(Color.WHITE);
        for (int i = 0; i < 600; i++) {
            int gx = (int)(Math.random() * GamePanel.WIDTH);
            int gy = (int)(Math.random() * GamePanel.HEIGHT);
            int gs = 1 + (int)(Math.random() * 2);
            g2D.fillRect(gx, gy, gs, gs);
        }
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // drawing the past games viewer screen
    public void drawPastGames(Graphics2D g2D) {

        g2D.setColor(new Color(30, 30, 30));
        g2D.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Back button with caramel latte rounded style
        boolean backHover = gp.mouse.x >= 30 && gp.mouse.x <= 130 && gp.mouse.y >= 30 && gp.mouse.y <= 70;
        g2D.setColor(backHover ? new Color(110, 75, 50) : new Color(70, 45, 30));
        g2D.fillRoundRect(30, 30, 100, 40, 15, 15);
        if (backHover) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(new Color(180, 140, 100, 120));
            g2D.drawRoundRect(30, 30, 100, 40, 15, 15);
            g2D.setStroke(new BasicStroke(1));
        } else {
            g2D.setColor(new Color(90, 65, 45));
            g2D.drawRoundRect(30, 30, 100, 40, 15, 15);
        }
        g2D.setFont(new Font("Courier New", Font.BOLD, 14));
        g2D.setColor(new Color(245, 235, 220));
        g2D.drawString("Back", 60, 55);

        g2D.setFont(new Font("Courier New", Font.BOLD, 32));
        g2D.setColor(Color.WHITE);
        String title = "Past Games";
        FontMetrics fm = g2D.getFontMetrics();
        g2D.drawString(title, (GamePanel.WIDTH - fm.stringWidth(title)) / 2, 55);

        if (gp.pastGamesList.isEmpty()) {
            g2D.setFont(new Font("Courier New", Font.PLAIN, 18));
            g2D.setColor(new Color(120, 120, 120));
            String msg = "No past games yet.";
            fm = g2D.getFontMetrics();
            g2D.drawString(msg, (GamePanel.WIDTH - fm.stringWidth(msg)) / 2, 350);
        } else {
            g2D.setFont(new Font("Courier New", Font.BOLD, 14));
            g2D.setColor(new Color(150, 150, 150));
            int colX = 100;
            g2D.drawString("#", colX, 100);
            g2D.drawString("Date", colX + 60, 100);
            g2D.drawString("Result", colX + 300, 100);

            g2D.setColor(new Color(60, 60, 60));
            g2D.drawLine(colX, 110, colX + 500, 110);

            g2D.setFont(new Font("Courier New", Font.PLAIN, 14));
            int rowH = 40;
            int maxRows = (GamePanel.HEIGHT - 180) / rowH;
            int startIdx = gp.pastGamesScroll;

            for (int i = startIdx; i < gp.pastGamesList.size() && i < startIdx + maxRows; i++) {
                String[] game = gp.pastGamesList.get(i);
                int y = 140 + (i - startIdx) * rowH;

                if ((i - startIdx) % 2 == 0) {
                    g2D.setColor(new Color(40, 40, 40));
                    g2D.fillRect(colX - 10, y - 16, 520, rowH);
                }

                g2D.setColor(new Color(50, 50, 50));
                g2D.drawLine(colX - 10, y + rowH - 17, colX + 510, y + rowH - 17);

                g2D.setColor(new Color(180, 180, 180));
                g2D.drawString(game[0], colX, y);
                g2D.setColor(new Color(140, 140, 140));
                g2D.drawString(game[1], colX + 60, y);

                String res = game[2];
                if (res.equals("1-0")) g2D.setColor(new Color(76, 175, 80));
                else if (res.equals("0-1")) g2D.setColor(new Color(244, 67, 54));
                else g2D.setColor(new Color(200, 200, 200));
                g2D.drawString(res, colX + 300, y);
            }

            // no more clunky scroll buttons — mouse wheel does the job now
        }
    }

    // drawing the options screen with theme selectors
    public void drawOptions(Graphics2D g2D) {

        g2D.setColor(new Color(30, 30, 30));
        g2D.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean backHover = gp.mouse.x >= 30 && gp.mouse.x <= 130 && gp.mouse.y >= 30 && gp.mouse.y <= 70;
        g2D.setColor(backHover ? new Color(110, 75, 50) : new Color(70, 45, 30));
        g2D.fillRoundRect(30, 30, 100, 40, 15, 15);
        if (backHover) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(new Color(180, 140, 100, 120));
            g2D.drawRoundRect(30, 30, 100, 40, 15, 15);
            g2D.setStroke(new BasicStroke(1));
        } else {
            g2D.setColor(new Color(90, 65, 45));
            g2D.drawRoundRect(30, 30, 100, 40, 15, 15);
        }
        g2D.setFont(new Font("Courier New", Font.BOLD, 14));
        g2D.setColor(new Color(245, 235, 220));
        g2D.drawString("Back", 60, 55);

        g2D.setFont(new Font("Courier New", Font.BOLD, 32));
        g2D.setColor(Color.WHITE);
        String title = "Options";
        FontMetrics fm = g2D.getFontMetrics();
        g2D.drawString(title, (GamePanel.WIDTH - fm.stringWidth(title)) / 2, 55);

        // === BOARD THEME section ===
        g2D.setFont(new Font("Courier New", Font.BOLD, 18));
        g2D.setColor(Color.WHITE);
        g2D.drawString("Board Theme", 150, 150);

        int swatchSize = 40;
        int gap = 10;
        int startX = 150;
        int startY = 170;

        for (int i = 0; i < Theme.BOARD_THEMES.length; i++) {
            Theme t = Theme.BOARD_THEMES[i];
            int sx = startX + i * (swatchSize * 2 + gap);

            g2D.setColor(t.lightSquare);
            g2D.fillRect(sx, startY, swatchSize, swatchSize);
            g2D.setColor(t.darkSquare);
            g2D.fillRect(sx + swatchSize, startY, swatchSize, swatchSize);

            if (i == gp.currentThemeIndex) {
                g2D.setColor(new Color(66, 133, 244));
                g2D.setStroke(new BasicStroke(3));
                g2D.drawRect(sx - 2, startY - 2, swatchSize * 2 + 4, swatchSize + 4);
                g2D.setStroke(new BasicStroke(1));
            } else {
                g2D.setColor(new Color(80, 80, 80));
                g2D.drawRect(sx - 1, startY - 1, swatchSize * 2 + 2, swatchSize + 2);
            }

            g2D.setFont(new Font("Courier New", Font.PLAIN, 12));
            g2D.setColor(new Color(180, 180, 180));
            int nameW = g2D.getFontMetrics().stringWidth(t.name);
            g2D.drawString(t.name, sx + swatchSize - nameW / 2, startY + swatchSize + 18);
        }

        // === PIECE STYLE section ===
        g2D.setFont(new Font("Courier New", Font.BOLD, 18));
        g2D.setColor(Color.WHITE);
        g2D.drawString("Piece Style", 150, 300);

        g2D.setFont(new Font("Courier New", Font.PLAIN, 14));
        for (int i = 0; i < gp.pieceStyleNames.length; i++) {
            int y = 330 + i * 35;
            if (i == gp.currentPieceStyleIndex) {
                g2D.setColor(new Color(66, 133, 244));
                g2D.fillRect(150, y - 16, 250, 28);
                g2D.setColor(Color.WHITE);
            } else {
                g2D.setColor(new Color(150, 150, 150));
            }
            g2D.drawString(gp.pieceStyleNames[i], 160, y);
        }

        // === SOUND EFFECTS ===
        g2D.setFont(new Font("Courier New", Font.BOLD, 18));
        g2D.setColor(Color.WHITE);
        g2D.drawString("Sound Effects", 150, 450);
        g2D.setFont(new Font("Courier New", Font.PLAIN, 14));
        g2D.setColor(new Color(120, 120, 120));
        g2D.drawString("Coming soon", 160, 480);
    }

    // ==================== CLICK HANDLING ====================

    public void handleMenuClicks() {
        int btnW = 260, btnH = 55;
        int btnX = (GamePanel.WIDTH - btnW) / 2;
        int btnY = 380;

        if (!gp.mouse.pressed && gp.mouse.lastClicked) {
            gp.mouse.lastClicked = false;

            if (gp.mouse.x >= btnX && gp.mouse.x <= btnX + btnW &&
                gp.mouse.y >= btnY && gp.mouse.y <= btnY + btnH) {
                gp.startNewGame();
            }
            if (gp.mouse.x >= btnX && gp.mouse.x <= btnX + btnW &&
                gp.mouse.y >= btnY + 75 && gp.mouse.y <= btnY + 75 + btnH) {
                gp.pastGamesList = DatabaseManager.getRecentGames(100);
                gp.pastGamesScroll = 0;
                gp.currentState = GameState.PAST_GAMES;
            }
            if (gp.mouse.x >= btnX && gp.mouse.x <= btnX + btnW &&
                gp.mouse.y >= btnY + 150 && gp.mouse.y <= btnY + 150 + btnH) {
                gp.currentState = GameState.OPTIONS;
            }
        }
    }

    public void handlePastGamesClicks() {
        if (!gp.mouse.pressed && gp.mouse.lastClicked) {
            gp.mouse.lastClicked = false;

            if (gp.mouse.x >= 30 && gp.mouse.x <= 130 && gp.mouse.y >= 30 && gp.mouse.y <= 70) {
                gp.currentState = GameState.MENU;
            }

            if (!gp.pastGamesList.isEmpty() && gp.mouse.x >= 80 && gp.mouse.x <= 600 && gp.mouse.y >= 140 && gp.mouse.y <= 690) {
                int rowH = 40;
                int maxRows = (GamePanel.HEIGHT - 180) / rowH;
                int clickedRow = (gp.mouse.y - 140) / rowH;
                int gameIdx = gp.pastGamesScroll + clickedRow;
                if (gameIdx >= 0 && gameIdx < gp.pastGamesList.size()) {
                    String[] game = gp.pastGamesList.get(gameIdx);
                    int gameId = Integer.parseInt(game[0]);
                    gp.replaySystem.loadReplay(gameId, game[2], game[1]);
                }
            }
        }
    }

    public void handleOptionsClicks() {
        if (!gp.mouse.pressed && gp.mouse.lastClicked) {
            gp.mouse.lastClicked = false;

            if (gp.mouse.x >= 30 && gp.mouse.x <= 130 && gp.mouse.y >= 30 && gp.mouse.y <= 70) {
                gp.currentState = GameState.MENU;
            }

            int mx = gp.mouse.x;
            int my = gp.mouse.y;

            int swatchSize = 40;
            int gap = 10;
            int startX = 150;
            int startY = 170;

            for (int i = 0; i < Theme.BOARD_THEMES.length; i++) {
                int sx = startX + i * (swatchSize * 2 + gap);
                if (mx >= sx - 2 && mx <= sx + swatchSize * 2 + 2 &&
                    my >= startY - 2 && my <= startY + swatchSize + 22) {
                    gp.currentThemeIndex = i;
                }
            }

            for (int i = 0; i < gp.pieceStyleNames.length; i++) {
                int y = 330 + i * 35;
                if (mx >= 150 && mx <= 400 && my >= y - 16 && my <= y + 12) {
                    gp.currentPieceStyleIndex = i;
                }
            }
        }
    }
}

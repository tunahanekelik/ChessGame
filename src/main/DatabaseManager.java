package main;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:chess.db";
    private static Connection conn;

    public static void connect() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS games (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date_played TEXT DEFAULT (datetime('now', 'localtime'))," +
                    "result TEXT NOT NULL," +
                    "white_player TEXT DEFAULT 'Player'," +
                    "black_player TEXT DEFAULT 'Player'" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS moves (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "game_id INTEGER NOT NULL," +
                    "move_number INTEGER NOT NULL," +
                    "half INTEGER NOT NULL," +
                    "notation TEXT NOT NULL," +
                    "FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveGame(String result, ArrayList<String> moveLog) {
        if (conn == null) return;
        try {
            PreparedStatement gameStmt = conn.prepareStatement(
                    "INSERT INTO games (result) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            gameStmt.setString(1, result);
            gameStmt.executeUpdate();

            ResultSet rs = gameStmt.getGeneratedKeys();
            int gameId = rs.next() ? rs.getInt(1) : -1;
            rs.close();
            gameStmt.close();

            PreparedStatement moveStmt = conn.prepareStatement(
                    "INSERT INTO moves (game_id, move_number, half, notation) VALUES (?, ?, ?, ?)");

            for (int i = 0; i < moveLog.size(); i++) {
                moveStmt.setInt(1, gameId);
                moveStmt.setInt(2, (i / 2) + 1);
                moveStmt.setInt(3, i % 2);
                moveStmt.setString(4, moveLog.get(i));
                moveStmt.executeUpdate();
            }
            moveStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String[]> getRecentGames(int limit) {
        ArrayList<String[]> games = new ArrayList<>();
        if (conn == null) return games;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT id, date_played, result FROM games ORDER BY id DESC LIMIT " + limit);
            while (rs.next()) {
                games.add(new String[]{
                        rs.getString("id"),
                        rs.getString("date_played"),
                        rs.getString("result")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return games;
    }

    // get a single game by its id
    public static String[] getGameById(int gameId) {
        if (conn == null) return null;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, date_played, result FROM games WHERE id = ?");
            stmt.setInt(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String[] game = new String[]{
                        rs.getString("id"),
                        rs.getString("date_played"),
                        rs.getString("result")
                };
                rs.close();
                stmt.close();
                return game;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get all moves for a specific game, ordered by move_number and half (white then black)
    public static ArrayList<String> getMovesForGame(int gameId) {
        ArrayList<String> moves = new ArrayList<>();
        if (conn == null) return moves;
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT notation FROM moves WHERE game_id = ? ORDER BY move_number ASC, half ASC");
            stmt.setInt(1, gameId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                moves.add(rs.getString("notation"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moves;
    }

    public static int getTotalGames() {
        if (conn == null) return 0;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM games");
            int count = rs.getInt(1);
            rs.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

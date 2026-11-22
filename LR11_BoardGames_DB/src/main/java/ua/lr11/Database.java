package ua.lr11;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:board_games.db";

    public Database() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS board_games ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL CHECK(length(trim(name)) > 0), "
                    + "game_count INTEGER NOT NULL CHECK(game_count > 0), "
                    + "price REAL NOT NULL CHECK(price >= 0), "
                    + "game_type TEXT NOT NULL CHECK(game_type IN ('Шахи','Монополія')), "
                    + "extra TEXT NOT NULL)");
        }
        seedIfEmpty();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void seedIfEmpty() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM board_games")) {
            if (rs.next() && rs.getInt(1) == 0) {
                add("Шахи Classic", 1, 800, "Шахи", "Дерев'яна дошка");
                add("Монополія", 1, 1200, "Монополія", "6 гравців");
                add("Шахи Premium", 1, 2500, "Шахи", "Магнітна дошка");
                add("Монополія Junior", 1, 650, "Монополія", "4 гравці");
            }
        }
    }

    public List<Game> getAll(String filter, double maxPrice, boolean useFilter) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT id,name,game_count,price,game_type,extra FROM board_games"
                + (useFilter ? " WHERE price < ?" : "") + " ORDER BY id";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (useFilter) ps.setDouble(1, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) games.add(new Game(rs.getInt("id"), rs.getString("name"),
                        rs.getInt("game_count"), rs.getDouble("price"),
                        rs.getString("game_type"), rs.getString("extra")));
            }
        }
        return games;
    }

    public void add(String name, int count, double price, String type, String extra) throws SQLException {
        String sql = "INSERT INTO board_games(name,game_count,price,game_type,extra) VALUES(?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim()); ps.setInt(2, count); ps.setDouble(3, price);
            ps.setString(4, type); ps.setString(5, extra.trim()); ps.executeUpdate();
        }
    }

    public void update(int id, String name, int count, double price, String type, String extra) throws SQLException {
        String sql = "UPDATE board_games SET name=?,game_count=?,price=?,game_type=?,extra=? WHERE id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim()); ps.setInt(2, count); ps.setDouble(3, price);
            ps.setString(4, type); ps.setString(5, extra.trim()); ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM board_games WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }
}

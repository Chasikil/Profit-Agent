package com.restaurant.pos.db;

import com.restaurant.pos.domain.model.Shift;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShiftRepository {

    public List<Shift> findAll() {
        List<Shift> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, start_time, end_time, total_revenue, total_profit, closed FROM shift ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Shift findById(Long id) {
        if (id == null) return null;
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, start_time, end_time, total_revenue, total_profit, closed FROM shift WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Shift findActiveShift() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, start_time, end_time, total_revenue, total_profit, closed FROM shift WHERE closed = 0 ORDER BY id DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Shift s) {
        if (s == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (s.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO shift (start_time, end_time, total_revenue, total_profit, closed) VALUES (?,?,?,?,?)")) {
                    ps.setString(1, s.getStartTime() != null ? s.getStartTime().toString() : null);
                    ps.setString(2, s.getEndTime() != null ? s.getEndTime().toString() : null);
                    ps.setBigDecimal(3, s.getTotalRevenue());
                    ps.setBigDecimal(4, s.getTotalProfit());
                    ps.setInt(5, s.isClosed() ? 1 : 0);
                    ps.executeUpdate();
                    try (Statement keySt = c.createStatement();
                         ResultSet keys = keySt.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) {
                            s.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE shift SET start_time=?, end_time=?, total_revenue=?, total_profit=?, closed=? WHERE id=?")) {
                    ps.setString(1, s.getStartTime() != null ? s.getStartTime().toString() : null);
                    ps.setString(2, s.getEndTime() != null ? s.getEndTime().toString() : null);
                    ps.setBigDecimal(3, s.getTotalRevenue());
                    ps.setBigDecimal(4, s.getTotalProfit());
                    ps.setInt(5, s.isClosed() ? 1 : 0);
                    ps.setLong(6, s.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Shift mapRow(ResultSet rs) throws SQLException {
        Shift s = new Shift();
        s.setId(rs.getLong("id"));
        String st = rs.getString("start_time");
        if (st != null) s.setStartTime(LocalDateTime.parse(st));
        String et = rs.getString("end_time");
        if (et != null) s.setEndTime(LocalDateTime.parse(et));
        s.setTotalRevenue(BigDecimal.valueOf(rs.getDouble("total_revenue")));
        s.setTotalProfit(BigDecimal.valueOf(rs.getDouble("total_profit")));
        s.setClosed(rs.getInt("closed") == 1);
        return s;
    }
}

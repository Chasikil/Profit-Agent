package com.restaurant.pos.db;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.model.Employee;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {

    public List<Employee> findAll() {
        List<Employee> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, full_name, age, phone, address, role, active, hourly_rate, worked_hours, salary_balance, login, password_hash FROM employee");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Employee findById(Long id) {
        if (id == null) return null;
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, full_name, age, phone, address, role, active, hourly_rate, worked_hours, salary_balance, login, password_hash FROM employee WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Employee e) {
        if (e == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (e.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO employee (name, full_name, age, phone, address, role, active, hourly_rate, worked_hours, salary_balance, login, password_hash) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
                    setParams(ps, e);
                    ps.executeUpdate();
                    try (Statement keySt = c.createStatement();
                         ResultSet keys = keySt.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) {
                            e.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE employee SET name=?, full_name=?, age=?, phone=?, address=?, role=?, active=?, hourly_rate=?, worked_hours=?, salary_balance=?, login=?, password_hash=? WHERE id=?")) {
                    setParams(ps, e);
                    ps.setLong(13, e.getId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int count() {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM employee");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement ps, Employee e) throws SQLException {
        ps.setString(1, e.getName());
        ps.setString(2, e.getFullName());
        ps.setInt(3, e.getAge());
        ps.setString(4, e.getPhone());
        ps.setString(5, e.getAddress());
        ps.setString(6, e.getRole() != null ? e.getRole().name() : null);
        ps.setInt(7, e.isActive() ? 1 : 0);
        ps.setBigDecimal(8, e.getHourlyRate());
        ps.setDouble(9, e.getWorkedHours());
        ps.setBigDecimal(10, e.getSalaryBalance());
        ps.setString(11, e.getLogin());
        ps.setString(12, e.getPasswordHash());
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getLong("id"));
        e.setName(rs.getString("name"));
        e.setFullName(rs.getString("full_name"));
        e.setAge(rs.getInt("age"));
        e.setPhone(rs.getString("phone"));
        e.setAddress(rs.getString("address"));
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try { e.setRole(Role.valueOf(roleStr)); } catch (Exception ignored) {}
        }
        e.setActive(rs.getInt("active") == 1);
        e.setHourlyRate(toBigDecimal(rs.getDouble("hourly_rate")));
        e.setWorkedHours(rs.getDouble("worked_hours"));
        e.setSalaryBalance(toBigDecimal(rs.getDouble("salary_balance")));
        e.setLogin(rs.getString("login"));
        e.setPasswordHash(rs.getString("password_hash"));
        return e;
    }

    private static BigDecimal toBigDecimal(double v) {
        return BigDecimal.valueOf(v);
    }
}

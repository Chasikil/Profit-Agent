package com.restaurant.pos.db;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final OrderItemRepository orderItemRepo = new OrderItemRepository();
    private final DishRepository dishRepo = new DishRepository();
    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private final TableRepository tableRepo = new TableRepository();

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, waiter_id, shift_id, table_number, created_at, closed_at, status, cost_price, margin, total_cost, total_profit, paid, payment_method, amount_paid, change_amount FROM orders ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order o = mapRow(rs);
                orderItemRepo.loadItemsForOrder(o);
                resolveRefs(o);
                list.add(o);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Order> findByFilter(LocalDateTime dateFrom, LocalDateTime dateTo, Long waiterId, OrderStatus status) {
        List<Order> all = findAll();
        List<Order> out = new ArrayList<>();
        for (Order o : all) {
            if (dateFrom != null && o.getCreatedAt() != null && o.getCreatedAt().isBefore(dateFrom)) continue;
            if (dateTo != null && o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().isAfter(dateTo.toLocalDate())) continue;
            if (waiterId != null && !waiterId.equals(o.getWaiterId())) continue;
            if (status != null && o.getStatus() != status) continue;
            out.add(o);
        }
        return out;
    }

    public Order findById(Long id) {
        if (id == null) return null;
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, waiter_id, shift_id, table_number, created_at, closed_at, status, cost_price, margin, total_cost, total_profit, paid, payment_method, amount_paid, change_amount FROM orders WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Order o = mapRow(rs);
            orderItemRepo.loadItemsForOrder(o);
            resolveRefs(o);
            return o;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Order o) {
        if (o == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (o.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO orders (waiter_id, shift_id, table_number, created_at, closed_at, status, cost_price, margin, total_cost, total_profit, paid, payment_method, amount_paid, change_amount) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                    bindParams(ps, o);
                    ps.executeUpdate();
                    try (Statement keySt = c.createStatement();
                         ResultSet keys = keySt.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) {
                            o.setId(keys.getLong(1));
                        }
                    }
                }
                orderItemRepo.saveForOrder(o);
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET waiter_id=?, shift_id=?, table_number=?, created_at=?, closed_at=?, status=?, cost_price=?, margin=?, total_cost=?, total_profit=?, paid=?, payment_method=?, amount_paid=?, change_amount=? WHERE id=?")) {
                    bindParams(ps, o);
                    ps.setLong(15, o.getId());
                    ps.executeUpdate();
                }
                orderItemRepo.deleteByOrderId(o.getId());
                orderItemRepo.saveForOrder(o);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindParams(PreparedStatement ps, Order o) throws SQLException {
        ps.setObject(1, o.getWaiterId());
        ps.setObject(2, o.getShiftId());
        ps.setObject(3, o.getTable() != null ? o.getTable().getNumber() : null);
        ps.setString(4, o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
        ps.setString(5, o.getClosedAt() != null ? o.getClosedAt().toString() : null);
        ps.setString(6, o.getStatus() != null ? o.getStatus().name() : null);
        ps.setBigDecimal(7, o.getCostPrice());
        ps.setBigDecimal(8, o.getMargin());
        ps.setBigDecimal(9, o.getTotalCost());
        ps.setBigDecimal(10, o.getTotalProfit());
        ps.setInt(11, o.isPaid() ? 1 : 0);
        ps.setString(12, o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null);
        ps.setBigDecimal(13, o.getAmountPaid());
        ps.setBigDecimal(14, o.getChange());
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        if (rs.getObject("waiter_id") != null) o.setWaiterId(rs.getLong("waiter_id"));
        if (rs.getObject("shift_id") != null) o.setShiftId(rs.getLong("shift_id"));
        int tn = rs.getInt("table_number");
        if (!rs.wasNull()) {
            com.restaurant.pos.domain.model.Table t = new com.restaurant.pos.domain.model.Table(tn);
            o.setTable(t);
        }
        String s = rs.getString("created_at");
        if (s != null) o.setCreatedAt(LocalDateTime.parse(s));
        s = rs.getString("closed_at");
        if (s != null) o.setClosedAt(LocalDateTime.parse(s));
        s = rs.getString("status");
        if (s != null) {
            try { o.setStatus(OrderStatus.valueOf(s)); } catch (Exception ignored) {}
        }
        o.setCostPrice(toBD(rs.getDouble("cost_price")));
        o.setMargin(toBD(rs.getDouble("margin")));
        o.setTotalCost(toBD(rs.getDouble("total_cost")));
        o.setTotalProfit(toBD(rs.getDouble("total_profit")));
        o.setPaid(rs.getInt("paid") == 1);
        s = rs.getString("payment_method");
        if (s != null) {
            try { o.setPaymentMethod(PaymentMethod.valueOf(s)); } catch (Exception ignored) {}
        }
        o.setAmountPaid(toBD(rs.getDouble("amount_paid")));
        o.setChange(toBD(rs.getDouble("change_amount")));
        return o;
    }

    private void resolveRefs(Order o) {
        if (o.getWaiterId() != null)
            o.setWaiter(employeeRepo.findById(o.getWaiterId()));
    }

    private static BigDecimal toBD(double v) {
        return BigDecimal.valueOf(v);
    }
}

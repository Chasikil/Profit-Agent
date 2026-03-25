package com.restaurant.pos.db;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;

import java.sql.*;

public class OrderItemRepository {
    private final DishRepository dishRepo = new DishRepository();

    public void loadItemsForOrder(Order order) {
        if (order == null || order.getId() == null) return;
        order.getItems().clear();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT order_id, dish_id, quantity FROM order_item WHERE order_id = ?")) {
            ps.setLong(1, order.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                Long dishId = rs.getLong("dish_id");
                Dish dish = dishRepo.findById(dishId);
                item.setDish(dish);
                item.setQuantity(rs.getInt("quantity"));
                order.getItems().add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveForOrder(Order order) {
        if (order == null || order.getId() == null || order.getItems() == null) return;
        for (OrderItem item : order.getItems()) {
            if (item == null || item.getDish() == null) continue;
            try (Connection c = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = c.prepareStatement("INSERT INTO order_item (order_id, dish_id, quantity) VALUES (?,?,?)")) {
                ps.setLong(1, order.getId());
                ps.setLong(2, item.getDish().getId());
                ps.setInt(3, item.getQuantity());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteByOrderId(Long orderId) {
        if (orderId == null) return;
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM order_item WHERE order_id = ?")) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

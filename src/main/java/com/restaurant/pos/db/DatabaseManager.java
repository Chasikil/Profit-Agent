package com.restaurant.pos.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages SQLite database connection and schema initialization.
 */
public class DatabaseManager {

    private static final String DB_PATH = "restaurant_pos.db";
    private static final AtomicReference<DatabaseManager> INSTANCE = new AtomicReference<>();

    private final String url;

    public DatabaseManager() {
        this(DB_PATH);
    }

    public DatabaseManager(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        initSchema();
    }

    public static DatabaseManager getInstance() {
        if (INSTANCE.get() == null) {
            INSTANCE.set(new DatabaseManager());
        }
        return INSTANCE.get();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void initSchema() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // Products (Ingredients)
            st.execute("CREATE TABLE IF NOT EXISTS product (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, unit TEXT, quantity_in_stock REAL, cost_per_unit REAL, minimum_threshold REAL)");

            // Dishes (MenuItem)
            st.execute("CREATE TABLE IF NOT EXISTS dish (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, description TEXT, category TEXT, sale_price REAL, active INTEGER DEFAULT 1)");

            // TechCard
            st.execute("CREATE TABLE IF NOT EXISTS tech_card (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, dish_id INTEGER NOT NULL, dish_name TEXT, " +
                    "FOREIGN KEY(dish_id) REFERENCES dish(id))");
            st.execute("CREATE TABLE IF NOT EXISTS tech_card_item (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, tech_card_id INTEGER, product_id INTEGER, " +
                    "product_name TEXT, quantity_required REAL, " +
                    "FOREIGN KEY(tech_card_id) REFERENCES tech_card(id), FOREIGN KEY(product_id) REFERENCES product(id))");

            // Tables
            st.execute("CREATE TABLE IF NOT EXISTS restaurant_table (" +
                    "number INTEGER PRIMARY KEY, occupied INTEGER DEFAULT 0)");

            // Tables (new POS workflow table model)
            st.execute("CREATE TABLE IF NOT EXISTS tables (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "status TEXT NOT NULL)");

            // Employees
            st.execute("CREATE TABLE IF NOT EXISTS employee (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, full_name TEXT, age INTEGER, phone TEXT, " +
                    "address TEXT, role TEXT, active INTEGER DEFAULT 1, hourly_rate REAL, worked_hours REAL, " +
                    "salary_balance REAL, login TEXT, password_hash TEXT)");

            // Shifts (global restaurant shift)
            st.execute("CREATE TABLE IF NOT EXISTS shift (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, start_time TEXT, end_time TEXT, " +
                    "total_revenue REAL DEFAULT 0, total_profit REAL DEFAULT 0, closed INTEGER DEFAULT 0)");

            // Orders
            st.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, waiter_id INTEGER, shift_id INTEGER, table_number INTEGER, " +
                    "created_at TEXT, closed_at TEXT, status TEXT, cost_price REAL, margin REAL, total_cost REAL, total_profit REAL, " +
                    "paid INTEGER DEFAULT 0, payment_method TEXT, amount_paid REAL, change_amount REAL)");
            st.execute("CREATE TABLE IF NOT EXISTS order_item (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, dish_id INTEGER NOT NULL, " +
                    "quantity INTEGER NOT NULL, FOREIGN KEY(order_id) REFERENCES orders(id), FOREIGN KEY(dish_id) REFERENCES dish(id))");

            // Receipts
            st.execute("CREATE TABLE IF NOT EXISTS receipts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "table_number INTEGER NOT NULL, " +
                    "waiter_name TEXT NOT NULL, " +
                    "time TEXT NOT NULL, " +
                    "total REAL NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS receipt_item (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "receipt_id INTEGER NOT NULL, " +
                    "dish_name TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "FOREIGN KEY(receipt_id) REFERENCES receipts(id))");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}

package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.ui.controller.OrderHistoryController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryView extends VBox {

    private final OrderHistoryController controller;
    private final TableView<OrderHistoryRow> table;
    private final DatePicker dateFromPicker;
    private final DatePicker dateToPicker;
    private final ComboBox<String> waiterCombo;
    private final ComboBox<String> statusCombo;

    public OrderHistoryView(OrderHistoryController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        setSpacing(12);

        Label title = new Label("Order History");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox filters = new HBox(12);
        filters.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        dateFromPicker = new DatePicker();
        dateToPicker = new DatePicker();
        dateFromPicker.setPromptText("From");
        dateToPicker.setPromptText("To");
        waiterCombo = new ComboBox<>();
        waiterCombo.setPromptText("Waiter");
        waiterCombo.getItems().add("All");
        statusCombo = new ComboBox<>();
        statusCombo.setPromptText("Status");
        statusCombo.getItems().addAll("All", "OPEN", "PAID", "CANCELLED");
        Button filterBtn = new Button("Filter");
        filterBtn.setOnAction(e -> applyFilter());
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> clearFilter());

        filters.getChildren().addAll(new Label("From:"), dateFromPicker, new Label("To:"), dateToPicker,
                new Label("Waiter:"), waiterCombo, new Label("Status:"), statusCombo, filterBtn, clearBtn);

        table = new TableView<>();
        TableColumn<OrderHistoryRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().orderId));
        TableColumn<OrderHistoryRow, String> tableCol = new TableColumn<>("Table");
        tableCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().tableNum));
        TableColumn<OrderHistoryRow, String> waiterCol = new TableColumn<>("Waiter");
        waiterCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().waiter));
        TableColumn<OrderHistoryRow, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().total));
        TableColumn<OrderHistoryRow, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().profit));
        TableColumn<OrderHistoryRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().status));
        TableColumn<OrderHistoryRow, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().date));
        table.getColumns().addAll(idCol, tableCol, waiterCol, totalCol, profitCol, statusCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        loadWaiters();
        getChildren().addAll(title, filters, table);
        applyFilter();
    }

    private void loadWaiters() {
        waiterCombo.getItems().clear();
        waiterCombo.getItems().add("All");
        if (controller != null) {
            for (Employee e : controller.getAllWaiters()) {
                if (e != null) waiterCombo.getItems().add(e.getFullName() + " (ID:" + e.getId() + ")");
            }
        }
    }

    private void applyFilter() {
        if (controller == null) return;
        LocalDate from = dateFromPicker.getValue();
        LocalDate to = dateToPicker.getValue();
        Long waiterId = null;
        String w = waiterCombo.getValue();
        if (w != null && !w.equals("All") && w.contains("ID:")) {
            try {
                String idPart = w.substring(w.indexOf("ID:") + 3).replace(")", "").trim();
                waiterId = Long.parseLong(idPart);
            } catch (Exception ignored) {}
        }
        OrderStatus status = null;
        String s = statusCombo.getValue();
        if (s != null && !s.equals("All")) {
            try { status = OrderStatus.valueOf(s); } catch (Exception ignored) {}
        }
        List<Order> orders = controller.getOrders(from, to, waiterId, status);
        table.getItems().clear();
        DecimalFormat df = new DecimalFormat("#,##0.00 ₽");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Order o : orders) {
            String tableNum = o.getTable() != null ? String.valueOf(o.getTable().getNumber()) : "—";
            String waiter = controller.getWaiterName(o.getWaiterId());
            BigDecimal total = controller.getOrderTotal(o);
            BigDecimal profit = o.getTotalProfit() != null ? o.getTotalProfit() : BigDecimal.ZERO;
            String date = o.getCreatedAt() != null ? o.getCreatedAt().format(dtf) : "—";
            table.getItems().add(new OrderHistoryRow(o.getId(), tableNum, waiter,
                    df.format(total), df.format(profit), o.getStatus() != null ? o.getStatus().name() : "—", date));
        }
    }

    private void clearFilter() {
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        waiterCombo.setValue(null);
        statusCombo.setValue(null);
        applyFilter();
    }

    public void refresh() {
        applyFilter();
    }

    public static class OrderHistoryRow {
        public final Long orderId;
        public final String tableNum;
        public final String waiter;
        public final String total;
        public final String profit;
        public final String status;
        public final String date;

        public OrderHistoryRow(Long orderId, String tableNum, String waiter, String total, String profit, String status, String date) {
            this.orderId = orderId;
            this.tableNum = tableNum;
            this.waiter = waiter;
            this.total = total;
            this.profit = profit;
            this.status = status;
            this.date = date;
        }
    }
}

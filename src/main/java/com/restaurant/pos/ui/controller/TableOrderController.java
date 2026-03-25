package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.service.WaiterPosService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class TableOrderController {

    @FXML
    private Label tableLabel;
    @FXML
    private ListView<Dish> menuList;
    @FXML
    private TableView<OrderRow> orderTable;
    @FXML
    private Label totalLabel;
    @FXML
    private Button addDishButton;
    @FXML
    private Button closeOrderButton;
    @FXML
    private Button backButton;

    private final ObservableList<OrderRow> rows = FXCollections.observableArrayList();

    private WaiterPosService waiterPosService;
    private NavigationController navigationController;
    private int tableNumber;

    public void setWaiterPosService(WaiterPosService waiterPosService) {
        this.waiterPosService = waiterPosService;
    }

    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
        if (tableLabel != null) {
            tableLabel.setText("Стол № " + tableNumber);
        }
        loadOrder();
        loadMenu();
    }

    @FXML
    private void initialize() {
        setupOrderTable();
        orderTable.setItems(rows);

        if (menuList != null) {
            menuList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Dish selected = menuList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        onAddDish(selected);
                    }
                }
            });
        }
        if (addDishButton != null) {
            addDishButton.setOnAction(e -> {
                Dish selected = menuList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    onAddDish(selected);
                } else {
                    showError("Выберите блюдо из меню.");
                }
            });
        }
        if (closeOrderButton != null) {
            closeOrderButton.setOnAction(e -> closeOrder());
        }
        if (backButton != null) {
            backButton.setOnAction(e -> goBack());
        }
    }

    private void setupOrderTable() {
        if (orderTable == null) return;
        orderTable.getColumns().clear();

        TableColumn<OrderRow, String> dishCol = new TableColumn<>("Блюдо");
        dishCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dishName));

        TableColumn<OrderRow, Number> qtyCol = new TableColumn<>("Кол-во");
        qtyCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().quantity));

        TableColumn<OrderRow, String> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().price));

        TableColumn<OrderRow, String> sumCol = new TableColumn<>("Сумма");
        sumCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().sum));

        orderTable.getColumns().addAll(dishCol, qtyCol, priceCol, sumCol);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadMenu() {
        if (menuList == null || waiterPosService == null) return;
        List<Dish> dishes = waiterPosService.getMenuDishes();
        menuList.getItems().setAll(dishes);
    }

    private void loadOrder() {
        if (waiterPosService == null) return;
        Order order = waiterPosService.openTable(tableNumber);
        redraw(order);
    }

    private void onAddDish(Dish dish) {
        if (waiterPosService == null || dish == null) return;
        boolean ok = waiterPosService.addDish(tableNumber, dish);
        if (!ok) {
            showError("Недостаточно ингредиентов на складе");
            return;
        }
        Order order = waiterPosService.getOrder(tableNumber);
        redraw(order);
    }

    private void redraw(Order order) {
        rows.clear();
        BigDecimal total = BigDecimal.ZERO;
        NumberFormat fmt = new DecimalFormat("#,##0.00");
        if (order != null && order.getItems() != null) {
            for (OrderItem oi : order.getItems()) {
                if (oi == null || oi.getDish() == null) continue;
                BigDecimal price = oi.getDish().getSalePrice() != null ? oi.getDish().getSalePrice() : BigDecimal.ZERO;
                BigDecimal sum = price.multiply(BigDecimal.valueOf(oi.getQuantity()));
                total = total.add(sum);
                rows.add(new OrderRow(
                        oi.getDish().getName(),
                        oi.getQuantity(),
                        fmt.format(price.doubleValue()) + " ₽",
                        fmt.format(sum.doubleValue()) + " ₽"
                ));
            }
        }
        if (totalLabel != null) {
            totalLabel.setText("Итого: ₽ " + fmt.format(total.doubleValue()));
        }
    }

    private void closeOrder() {
        if (waiterPosService == null) return;
        var result = waiterPosService.closeOrder(tableNumber);
        if (!result.isSuccess()) {
            showError(result.getErrorMessage() != null ? result.getErrorMessage() : "Ошибка закрытия заказа");
            return;
        }
        showInfo("Заказ закрыт", "Заказ успешно закрыт.");
        // refresh dashboards/charts if needed
        if (navigationController != null) {
            navigationController.refreshDashboard();
            navigationController.showWaiterView();
        }
    }

    private void goBack() {
        if (navigationController != null) {
            navigationController.showWaiterView();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private static class OrderRow {
        final String dishName;
        final int quantity;
        final String price;
        final String sum;

        OrderRow(String dishName, int quantity, String price, String sum) {
            this.dishName = dishName;
            this.quantity = quantity;
            this.price = price;
            this.sum = sum;
        }
    }
}


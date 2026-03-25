package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.ui.controller.MenuController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class MenuView extends BorderPane {

    private final MenuController controller;
    private final TableView<Dish> tableView;
    private final Button refreshButton;
    private final Button toggleAvailabilityButton;

    public MenuView(MenuController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        getStyleClass().add("menu-view");

        tableView = createTable();

        HBox buttonsBar = new HBox(8);
        buttonsBar.setAlignment(Pos.CENTER_RIGHT);
        buttonsBar.setPadding(new Insets(8, 0, 0, 0));

        refreshButton = new Button("Обновить");
        toggleAvailabilityButton = new Button("Переключить доступность");

        refreshButton.setOnAction(e -> controller.refreshMenu());
        toggleAvailabilityButton.setOnAction(e -> {
            Dish selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Выберите блюдо для изменения доступности.");
                return;
            }
            controller.toggleAvailability(selected);
        });

        buttonsBar.getChildren().addAll(refreshButton, toggleAvailabilityButton);

        VBox centerBox = new VBox(8, tableView, buttonsBar);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        setCenter(centerBox);
    }

    private TableView<Dish> createTable() {
        TableView<Dish> table = new TableView<>();

        TableColumn<Dish, String> nameCol = new TableColumn<>("Блюдо");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
        TableColumn<Dish, String> priceCol = new TableColumn<>("Цена продажи");
        priceCol.setCellValueFactory(c -> {
            BigDecimal price = c.getValue().getSalePrice();
            String txt = price != null ? moneyFormat.format(price.doubleValue()) : "0.00";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });

        TableColumn<Dish, String> availableCol = new TableColumn<>("Доступно");
        availableCol.setCellValueFactory(c -> {
            String txt = c.getValue().isActive() ? "Да" : "Нет";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });

        table.getColumns().addAll(nameCol, priceCol, availableCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Set row factory to visually disable unavailable dishes
        table.setRowFactory(tv -> {
            TableRow<Dish> row = new TableRow<Dish>() {
                @Override
                protected void updateItem(Dish dish, boolean empty) {
                    super.updateItem(dish, empty);
                    if (empty || dish == null) {
                        setDisable(false);
                        setStyle("");
                    } else {
                        // Availability will be set by updateTableWithAvailability
                        setDisable(false);
                        setStyle("");
                    }
                }
            };
            return row;
        });
        
        return table;
    }

    public void updateTable(List<Dish> dishes) {
        tableView.getItems().setAll(dishes);
    }

    /**
     * Update table with dishes and their availability status.
     * Unavailable dishes will be visually disabled.
     * 
     * @param dishes list of dishes
     * @param availabilityMap map of dishId -> availability status
     */
    public void updateTableWithAvailability(List<Dish> dishes, java.util.Map<Long, Boolean> availabilityMap) {
        tableView.getItems().setAll(dishes);
        
        // Update row styles based on availability
        tableView.refresh();
        
        // Set row factory to visually disable unavailable dishes
        tableView.setRowFactory(tv -> {
            TableRow<Dish> row = new TableRow<Dish>() {
                @Override
                protected void updateItem(Dish dish, boolean empty) {
                    super.updateItem(dish, empty);
                    if (empty || dish == null) {
                        setDisable(false);
                        setStyle("");
                        getStyleClass().removeAll("unavailable-dish");
                    } else {
                        Boolean isAvailable = availabilityMap != null ? availabilityMap.get(dish.getId()) : null;
                        if (isAvailable != null && !isAvailable) {
                            // Visually disable unavailable dish
                            setDisable(true);
                            setStyle("-fx-opacity: 0.5;");
                            getStyleClass().add("unavailable-dish");
                        } else {
                            setDisable(false);
                            setStyle("");
                            getStyleClass().removeAll("unavailable-dish");
                        }
                    }
                }
            };
            return row;
        });
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }

    /**
     * Enable or disable editing buttons based on role.
     * Called by controller when role restrictions change.
     */
    public void setEditingEnabled(boolean enabled) {
        toggleAvailabilityButton.setDisable(!enabled);
        // refreshButton is always enabled (read-only operation)
    }
}


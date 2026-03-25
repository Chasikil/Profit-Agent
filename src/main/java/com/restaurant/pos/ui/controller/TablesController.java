package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.model.Table;
import com.restaurant.pos.service.WaiterPosService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.util.List;

public class TablesController {

    @FXML
    private TilePane tablesPane;

    private WaiterPosService waiterPosService;
    private NavigationController navigationController;

    public void setWaiterPosService(WaiterPosService waiterPosService) {
        this.waiterPosService = waiterPosService;
    }

    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    @FXML
    private void initialize() {
        // actual load happens after setters
    }

    public void loadTables() {
        if (tablesPane == null || waiterPosService == null) return;
        tablesPane.getChildren().clear();
        tablesPane.getStyleClass().add("tables-grid");
        List<Table> tables = waiterPosService.getTables();
        for (Table t : tables) {
            if (t == null) continue;
            Button btn = new Button("Table " + t.getNumber());
            btn.getStyleClass().add("table-card");
            int number = t.getNumber();
            boolean occupied = waiterPosService.hasActiveOrder(number);
            btn.getStyleClass().add(occupied ? "table-occupied" : "table-free");
            btn.setOnAction(e -> {
                try {
                    openTable(number);
                } catch (Exception ex) {
                    showError("Не удалось открыть стол: " + (ex.getMessage() != null ? ex.getMessage() : "ошибка"));
                }
            });
            tablesPane.getChildren().add(btn);
        }
    }

    private void openTable(int tableNumber) throws IOException {
        var url = getClass().getResource("/fxml/TableOrderView.fxml");
        if (url == null) {
            throw new IOException("FXML '/fxml/TableOrderView.fxml' не найден");
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        TableOrderController controller = loader.getController();
        controller.setWaiterPosService(waiterPosService);
        controller.setNavigationController(navigationController);
        controller.setTableNumber(tableNumber);

        if (navigationController != null) {
            navigationController.showWaiterNode(root, "Стол № " + tableNumber);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }
}


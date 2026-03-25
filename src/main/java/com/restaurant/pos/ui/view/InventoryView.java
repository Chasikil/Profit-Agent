package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.model.Product;
import com.restaurant.pos.ui.controller.InventoryController;
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

public class InventoryView extends BorderPane {

    private final InventoryController controller;
    private final TableView<Product> tableView;
    private final Button addButton;
    private final Button writeOffButton;
    private final Button refreshButton;

    public InventoryView(InventoryController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        getStyleClass().add("inventory-view");

        tableView = createTable();

        HBox buttonsBar = new HBox(8);
        buttonsBar.setAlignment(Pos.CENTER_RIGHT);
        buttonsBar.setPadding(new Insets(8, 0, 0, 0));

        addButton = new Button("Добавить");
        writeOffButton = new Button("Списать");
        refreshButton = new Button("Обновить");

        addButton.setOnAction(e -> openAddDialog());
        writeOffButton.setOnAction(e -> openWriteOffDialog());
        refreshButton.setOnAction(e -> controller.refresh());

        buttonsBar.getChildren().addAll(addButton, writeOffButton, refreshButton);

        VBox centerBox = new VBox(8, tableView, buttonsBar);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        setCenter(centerBox);
    }

    private TableView<Product> createTable() {
        TableView<Product> table = new TableView<>();

        TableColumn<Product, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));

        TableColumn<Product, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        NumberFormat quantityFormat = new DecimalFormat("#,##0.###");
        TableColumn<Product, String> qtyCol = new TableColumn<>("Количество");
        qtyCol.setCellValueFactory(c -> {
            BigDecimal q = c.getValue().getQuantityInStock();
            String txt = q != null ? quantityFormat.format(q.doubleValue()) : "0";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });

        NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
        TableColumn<Product, String> costCol = new TableColumn<>("Себестоимость");
        costCol.setCellValueFactory(c -> {
            BigDecimal v = c.getValue().getCostPerUnit();
            String txt = v != null ? moneyFormat.format(v.doubleValue()) : "0.00";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });

        table.getColumns().addAll(idCol, nameCol, qtyCol, costCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    public void updateTable(List<Product> products) {
        tableView.getItems().setAll(products);
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }

    /**
     * Enable or disable all inventory buttons based on access.
     * Called by controller when role restrictions change.
     */
    public void setButtonsEnabled(boolean enabled) {
        addButton.setDisable(!enabled);
        writeOffButton.setDisable(!enabled);
        refreshButton.setDisable(!enabled);
    }

    private void openAddDialog() {
        Dialog<AddProductData> dialog = new Dialog<>();
        dialog.setTitle("Добавить товар");

        Label nameLabel = new Label("Название:");
        TextField nameField = new TextField();

        Label unitLabel = new Label("Ед. изм.:");
        TextField unitField = new TextField("шт");

        Label qtyLabel = new Label("Количество:");
        TextField qtyField = new TextField();

        Label costLabel = new Label("Себестоимость за ед.:");
        TextField costField = new TextField();

        VBox content = new VBox(8,
                nameLabel, nameField,
                unitLabel, unitField,
                qtyLabel, qtyField,
                costLabel, costField
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                AddProductData data = new AddProductData();
                data.name = nameField.getText();
                data.unit = unitField.getText();
                data.quantity = parseBigDecimal(qtyField.getText());
                data.costPerUnit = parseBigDecimal(costField.getText());
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data.quantity == null || data.costPerUnit == null) {
                showError("Неверный формат количества или себестоимости.");
                return;
            }
            controller.onAddProduct(data.name, data.unit, data.quantity, data.costPerUnit);
        });
    }

    private void openWriteOffDialog() {
        Product selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите товар для списания.");
            return;
        }

        Dialog<WriteOffData> dialog = new Dialog<>();
        dialog.setTitle("Списать товар");

        Label qtyLabel = new Label("Количество для списания:");
        TextField qtyField = new TextField();

        Label reasonLabel = new Label("Причина:");
        TextField reasonField = new TextField("Списание из UI");

        VBox content = new VBox(8, qtyLabel, qtyField, reasonLabel, reasonField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                WriteOffData data = new WriteOffData();
                data.quantity = parseBigDecimal(qtyField.getText());
                data.reason = reasonField.getText();
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data.quantity == null) {
                showError("Неверный формат количества.");
                return;
            }
            controller.onWriteOffProduct(selected.getId(), data.quantity, data.reason);
        });
    }

    private BigDecimal parseBigDecimal(String text) {
        try {
            return new BigDecimal(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class AddProductData {
        String name;
        String unit;
        BigDecimal quantity;
        BigDecimal costPerUnit;
    }

    private static class WriteOffData {
        BigDecimal quantity;
        String reason;
    }
}


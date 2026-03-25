package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.model.Table;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Dialog to select a free table for a new order.
 */
public class TableSelectionDialog extends Dialog<Table> {

    public TableSelectionDialog(List<Table> freeTables) {
        setTitle("Выбор стола");
        setHeaderText("Выберите свободный стол для нового заказа.");

        getDialogPane().getButtonTypes().addAll(
                new ButtonType("Подтвердить", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        ComboBox<Table> combo = new ComboBox<>();
        combo.getItems().setAll(freeTables);
        combo.setPrefWidth(180);
        combo.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Table t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : "Стол #" + t.getNumber());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Table t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : "Стол #" + t.getNumber());
            }
        });
        combo.setPromptText("Выберите стол");

        VBox root = new VBox(8, new Label("Стол:"), combo);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.CENTER_LEFT);
        getDialogPane().setContent(root);

        setResultConverter(bt ->
                bt != null && bt.getButtonData() == ButtonBar.ButtonData.OK_DONE ? combo.getValue() : null
        );
    }

    public static Table selectTable(List<Table> freeTables) {
        if (freeTables == null || freeTables.isEmpty()) {
            return null;
        }
        return new TableSelectionDialog(freeTables).showAndWait().orElse(null);
    }
}

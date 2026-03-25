package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.ui.controller.SalaryController;
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

/**
 * JavaFX view for manager salary management.
 * Displays employees with salary information.
 * UI does not calculate salary - all calculations come from controller/services.
 */
public class SalaryView extends BorderPane {

    private final SalaryController controller;
    private final TableView<Employee> tableView;
    private final Button refreshButton;
    private final Button applyBonusButton;
    private final Button applyPenaltyButton;

    public SalaryView(SalaryController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        getStyleClass().add("salary-view");

        tableView = createTable();

        HBox buttonsBar = new HBox(8);
        buttonsBar.setAlignment(Pos.CENTER_RIGHT);
        buttonsBar.setPadding(new Insets(8, 0, 0, 0));

        refreshButton = new Button("Обновить");
        applyBonusButton = new Button("Применить бонус");
        applyPenaltyButton = new Button("Применить штраф");

        refreshButton.setOnAction(e -> controller.loadEmployees());
        applyBonusButton.setOnAction(e -> openBonusDialog());
        applyPenaltyButton.setOnAction(e -> openPenaltyDialog());

        buttonsBar.getChildren().addAll(refreshButton, applyBonusButton, applyPenaltyButton);

        VBox centerBox = new VBox(8, tableView, buttonsBar);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        setCenter(centerBox);
    }

    private TableView<Employee> createTable() {
        TableView<Employee> table = new TableView<>();

        TableColumn<Employee, String> nameCol = new TableColumn<>("ФИО");
        nameCol.setCellValueFactory(c -> {
            String name = c.getValue().getFullName();
            return new javafx.beans.property.SimpleStringProperty(name != null ? name : "");
        });
        nameCol.setPrefWidth(200);

        TableColumn<Employee, String> roleCol = new TableColumn<>("Роль");
        roleCol.setCellValueFactory(c -> {
            Role role = c.getValue().getRole();
            String roleText = role != null ? roleToString(role) : "";
            return new javafx.beans.property.SimpleStringProperty(roleText);
        });
        roleCol.setPrefWidth(120);

        NumberFormat hoursFormat = new DecimalFormat("#,##0.00");
        TableColumn<Employee, String> hoursCol = new TableColumn<>("Отработано часов");
        hoursCol.setCellValueFactory(c -> {
            double hours = c.getValue().getWorkedHours();
            String txt = hoursFormat.format(hours);
            return new javafx.beans.property.SimpleStringProperty(txt);
        });
        hoursCol.setPrefWidth(150);

        NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
        TableColumn<Employee, String> rateCol = new TableColumn<>("Ставка в час");
        rateCol.setCellValueFactory(c -> {
            BigDecimal rate = c.getValue().getHourlyRate();
            String txt = rate != null ? moneyFormat.format(rate.doubleValue()) + " ₽" : "0.00 ₽";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });
        rateCol.setPrefWidth(130);

        TableColumn<Employee, String> balanceCol = new TableColumn<>("Баланс зарплаты");
        balanceCol.setCellValueFactory(c -> {
            BigDecimal balance = c.getValue().getSalaryBalance();
            String txt = balance != null ? moneyFormat.format(balance.doubleValue()) + " ₽" : "0.00 ₽";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });
        balanceCol.setPrefWidth(150);

        table.getColumns().addAll(nameCol, roleCol, hoursCol, rateCol, balanceCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Нет сотрудников"));
        return table;
    }

    private String roleToString(Role role) {
        if (role == null) {
            return "";
        }
        switch (role) {
            case WAITER:
                return "Официант";
            case MANAGER:
                return "Менеджер";
            case CHEF:
                return "Повар";
            case DIRECTOR:
                return "Директор";
            default:
                return role.name();
        }
    }

    public void updateTable(List<Employee> employees) {
        tableView.getItems().setAll(employees);
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }

    private void openBonusDialog() {
        Employee selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите сотрудника для применения бонуса.");
            return;
        }

        Dialog<BonusPenaltyData> dialog = new Dialog<>();
        dialog.setTitle("Применить бонус");
        dialog.setHeaderText("Сотрудник: " + (selected.getFullName() != null ? selected.getFullName() : ""));

        Label amountLabel = new Label("Сумма бонуса:");
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");

        Label commentLabel = new Label("Причина:");
        TextField commentField = new TextField();
        commentField.setPromptText("Описание бонуса");

        VBox content = new VBox(8, amountLabel, amountField, commentLabel, commentField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                BonusPenaltyData data = new BonusPenaltyData();
                data.amount = parseBigDecimal(amountField.getText());
                data.comment = commentField.getText();
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data.amount == null || data.amount.signum() <= 0) {
                showError("Неверный формат суммы. Введите положительное число.");
                return;
            }
            controller.handleApplyBonus(selected, data.amount, data.comment);
        });
    }

    private void openPenaltyDialog() {
        Employee selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите сотрудника для применения штрафа.");
            return;
        }

        Dialog<BonusPenaltyData> dialog = new Dialog<>();
        dialog.setTitle("Применить штраф");
        dialog.setHeaderText("Сотрудник: " + (selected.getFullName() != null ? selected.getFullName() : ""));

        Label amountLabel = new Label("Сумма штрафа:");
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");

        Label commentLabel = new Label("Причина:");
        TextField commentField = new TextField();
        commentField.setPromptText("Описание штрафа");

        VBox content = new VBox(8, amountLabel, amountField, commentLabel, commentField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                BonusPenaltyData data = new BonusPenaltyData();
                data.amount = parseBigDecimal(amountField.getText());
                data.comment = commentField.getText();
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data.amount == null || data.amount.signum() <= 0) {
                showError("Неверный формат суммы. Введите положительное число.");
                return;
            }
            controller.handleApplyPenalty(selected, data.amount, data.comment);
        });
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class BonusPenaltyData {
        BigDecimal amount;
        String comment;
    }
}

package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.enums.FinanceCategory;
import com.restaurant.pos.domain.enums.FinanceOperationType;
import com.restaurant.pos.domain.model.FinanceOperation;
import com.restaurant.pos.service.FinanceService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Dialog for adding a new expense operation.
 * Fields:
 * - Amount
 * - Category (dropdown)
 * - Description
 */
public class AddExpenseDialog {

    private final FinanceService financeService;
    private final Consumer<FinanceOperation> onExpenseAdded;

    /**
     * Data class for expense form data.
     */
    private static class ExpenseData {
        BigDecimal amount;
        FinanceCategory category;
        String description;
    }

    public AddExpenseDialog(FinanceService financeService, Consumer<FinanceOperation> onExpenseAdded) {
        this.financeService = financeService;
        this.onExpenseAdded = onExpenseAdded;
    }

    /**
     * Show the add expense dialog.
     */
    public void show() {
        Dialog<ExpenseData> dialog = new Dialog<>();
        dialog.setTitle("Add Expense");
        dialog.setHeaderText("Enter expense details");

        // Amount field
        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");

        // Category dropdown
        Label categoryLabel = new Label("Category:");
        ComboBox<FinanceCategory> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(FinanceCategory.values());
        categoryComboBox.setValue(FinanceCategory.OTHER); // Default value
        categoryComboBox.setPrefWidth(200);

        // Description field
        Label descriptionLabel = new Label("Description:");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Enter description");

        VBox content = new VBox(8,
                amountLabel, amountField,
                categoryLabel, categoryComboBox,
                descriptionLabel, descriptionField
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Enable/disable OK button based on validation
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Validate form
        Runnable validateForm = () -> {
            boolean isValid = !amountField.getText().trim().isEmpty()
                    && parseBigDecimal(amountField.getText()) != null
                    && parseBigDecimal(amountField.getText()).signum() > 0
                    && categoryComboBox.getValue() != null
                    && !descriptionField.getText().trim().isEmpty();
            okButton.setDisable(!isValid);
        };

        amountField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                ExpenseData data = new ExpenseData();
                data.amount = parseBigDecimal(amountField.getText());
                data.category = categoryComboBox.getValue();
                data.description = descriptionField.getText().trim();
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data.amount == null || data.amount.signum() <= 0) {
                showError("Invalid amount. Please enter a positive number.");
                return;
            }

            if (data.category == null) {
                showError("Please select a category.");
                return;
            }

            if (data.description == null || data.description.trim().isEmpty()) {
                showError("Please enter a description.");
                return;
            }

            // Create FinanceOperation
            FinanceOperation expenseOperation = new FinanceOperation();
            expenseOperation.setType(FinanceOperationType.EXPENSE);
            expenseOperation.setCategory(data.category.name());
            expenseOperation.setAmount(data.amount);
            expenseOperation.setDescription(data.description);
            expenseOperation.setDateTime(LocalDateTime.now());
            expenseOperation.setRelatedOrderId(null); // Expenses are not related to orders
            expenseOperation.setCreatedBy(null); // Can be set if we have current user context

            // Add to FinanceService
            if (financeService != null) {
                financeService.addOperation(expenseOperation);
            }

            // Notify callback to refresh view
            if (onExpenseAdded != null) {
                onExpenseAdded.accept(expenseOperation);
            }
        });
    }

    /**
     * Parse BigDecimal from string.
     * 
     * @param text text to parse
     * @return BigDecimal or null if invalid
     */
    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Show error alert.
     * 
     * @param message error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

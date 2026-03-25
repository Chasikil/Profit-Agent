package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.model.FinanceOperation;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Finance view for displaying income and expense operations.
 * Layout:
 * - Top: Date range picker
 * - Center: Table of finance operations
 * - Bottom: Total income, Total expenses, Net profit
 */
public class FinanceView extends BorderPane {

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private final TableView<FinanceOperation> operationsTable;
    private final ObservableList<FinanceOperation> operationsData;
    
    private Label totalIncomeLabel;
    private Label totalExpensesLabel;
    private Label netProfitLabel;
    
    private BiConsumer<LocalDate, LocalDate> onDateRangeChanged;
    private java.util.function.Function<Long, String> userNameResolver;
    private com.restaurant.pos.service.FinanceService financeService;
    private com.restaurant.pos.service.AnalyticsService analyticsService;
    private Runnable onRefreshCallback;
    
    // Expenses chart
    private final PieChart expensesChart;
    private final VBox expensesChartEmptyState;
    
    // Income vs Expenses chart
    private final BarChart<String, Number> incomeExpensesChart;
    private final VBox incomeExpensesChartEmptyState;

    public FinanceView() {
        setPadding(new Insets(16));
        getStyleClass().add("finance-view");

        // Top: Date range picker and Add Expense button
        HBox topBox = new HBox(12);
        topBox.setAlignment(Pos.CENTER_LEFT);
        HBox dateRangeBox = createDateRangePicker();
        javafx.scene.control.Button addExpenseButton = new javafx.scene.control.Button("Add Expense");
        addExpenseButton.setOnAction(e -> openAddExpenseDialog());
        topBox.getChildren().addAll(dateRangeBox, addExpenseButton);
        setTop(topBox);
        BorderPane.setMargin(topBox, new Insets(0, 0, 16, 0));

        // Center: Operations table and charts
        operationsData = FXCollections.observableArrayList();
        operationsTable = createOperationsTable();
        expensesChart = createExpensesChart();
        incomeExpensesChart = createIncomeExpensesChart();
        
        // Create vertical layout for charts with containers
        incomeExpensesChartEmptyState = createChartEmptyState("No income or expense data available for the selected period");
        expensesChartEmptyState = createChartEmptyState("No expense data available for the selected period");
        
        VBox chartsBox = new VBox(16);
        VBox incomeExpensesContainer = wrapChartInContainerWithEmptyState(incomeExpensesChart, incomeExpensesChartEmptyState, "Total Income vs Total Expenses");
        VBox expensesContainer = wrapChartInContainerWithEmptyState(expensesChart, expensesChartEmptyState, "Expenses by Category");
        chartsBox.getChildren().addAll(incomeExpensesContainer, expensesContainer);
        
        // Create split pane to show table and charts side by side
        SplitPane centerPane = new SplitPane();
        centerPane.getItems().addAll(operationsTable, chartsBox);
        centerPane.setDividerPositions(0.5); // Table takes 50%, charts take 50%
        setCenter(centerPane);

        // Bottom: Summary totals
        HBox summaryBox = createSummaryBox();
        setBottom(summaryBox);
        BorderPane.setMargin(summaryBox, new Insets(16, 0, 0, 0));
    }
    
    /**
     * Wrap chart in a container with rounded corners and styling, including empty state.
     */
    private VBox wrapChartInContainerWithEmptyState(javafx.scene.Node chart, VBox emptyState, String title) {
        VBox container = new VBox(8);
        container.getStyleClass().add("chart-container");
        container.setPadding(new Insets(16));
        
        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("chart-title");
            container.getChildren().add(titleLabel);
        }
        
        // Use StackPane to overlay chart and empty state
        StackPane chartPane = new StackPane();
        chartPane.getChildren().addAll(chart, emptyState);
        chartPane.setMinHeight(200);
        VBox.setVgrow(chartPane, Priority.ALWAYS);
        
        container.getChildren().add(chartPane);
        
        return container;
    }
    
    /**
     * Create empty state placeholder for charts.
     */
    private VBox createChartEmptyState(String message) {
        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.getStyleClass().add("chart-empty-state");
        emptyState.setVisible(false); // Hidden by default
        
        Label iconLabel = new Label("📊");
        iconLabel.getStyleClass().add("empty-state-icon");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("empty-state-message");
        
        emptyState.getChildren().addAll(iconLabel, messageLabel);
        return emptyState;
    }
    
    /**
     * Create empty state placeholder for tables.
     */
    private VBox createTableEmptyState(String message) {
        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(32));
        emptyState.getStyleClass().add("table-empty-state");
        
        Label iconLabel = new Label("📋");
        iconLabel.getStyleClass().add("empty-state-icon");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("empty-state-message");
        
        emptyState.getChildren().addAll(iconLabel, messageLabel);
        return emptyState;
    }
    
    /**
     * Show or hide chart empty state.
     */
    private void showChartEmptyState(VBox emptyState, boolean show) {
        if (emptyState != null) {
            emptyState.setVisible(show);
            emptyState.setManaged(show);
        }
    }

    private HBox createDateRangePicker() {
        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_LEFT);

        Label startLabel = new Label("From:");
        startDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        startDatePicker.setPrefWidth(150);

        Label endLabel = new Label("To:");
        endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPrefWidth(150);

        // Add change listeners to trigger date range update
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && onDateRangeChanged != null) {
                onDateRangeChanged.accept(newVal, endDatePicker.getValue());
            }
            // Update charts when date changes
            updateIncomeExpensesChart();
            updateExpensesChart();
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && onDateRangeChanged != null) {
                onDateRangeChanged.accept(startDatePicker.getValue(), newVal);
            }
            // Update charts when date changes
            updateIncomeExpensesChart();
            updateExpensesChart();
        });

        container.getChildren().addAll(startLabel, startDatePicker, endLabel, endDatePicker);
        return container;
    }

    @SuppressWarnings("unchecked")
    private TableView<FinanceOperation> createOperationsTable() {
        TableView<FinanceOperation> table = new TableView<>();
        table.getStyleClass().add("finance-operations-table");
        table.setItems(operationsData);

        // Column: Date
        TableColumn<FinanceOperation, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(120);
        dateCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDateTime()));
        dateCol.setCellFactory(column -> new javafx.scene.control.TableCell<FinanceOperation, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        // Column: Type
        TableColumn<FinanceOperation, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        typeCol.setCellValueFactory(param -> {
            String type = param.getValue().getType() != null 
                ? param.getValue().getType().name() 
                : "";
            return new SimpleStringProperty(type);
        });

        // Column: Category
        TableColumn<FinanceOperation, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setPrefWidth(120);
        categoryCol.setCellValueFactory(param -> {
            String category = param.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category : "");
        });

        // Column: Amount
        TableColumn<FinanceOperation, BigDecimal> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(120);
        amountCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAmount()));
        amountCol.setCellFactory(column -> new javafx.scene.control.TableCell<FinanceOperation, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("₽ " + item.toString());
                }
            }
        });

        // Column: Description
        TableColumn<FinanceOperation, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setPrefWidth(250);
        descriptionCol.setCellValueFactory(param -> {
            String description = param.getValue().getDescription();
            return new SimpleStringProperty(description != null ? description : "");
        });

        // Column: Created by
        TableColumn<FinanceOperation, String> createdByCol = new TableColumn<>("Created by");
        createdByCol.setPrefWidth(150);
        createdByCol.setCellValueFactory(param -> {
            Long userId = param.getValue().getCreatedBy();
            if (userId == null) {
                return new SimpleStringProperty("System");
            }
            // Use name resolver if available, otherwise show user ID
            if (userNameResolver != null) {
                String userName = userNameResolver.apply(userId);
                return new SimpleStringProperty(userName != null ? userName : "User #" + userId);
            }
            return new SimpleStringProperty("User #" + userId);
        });

        table.getColumns().addAll(dateCol, typeCol, categoryCol, amountCol, descriptionCol, createdByCol);
        
        // Set placeholder with empty state styling
        VBox placeholder = createTableEmptyState("No operations to display");
        table.setPlaceholder(placeholder);

        // Add context menu for delete action
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteSelectedOperation());
        contextMenu.getItems().add(deleteItem);
        table.setContextMenu(contextMenu);

        // Also support Delete key
        table.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.DELETE) {
                deleteSelectedOperation();
            }
        });

        return table;
    }

    private HBox createSummaryBox() {
        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(12));
        container.getStyleClass().add("finance-summary-box");

        totalIncomeLabel = new Label("₽ 0.00");
        totalExpensesLabel = new Label("₽ 0.00");
        netProfitLabel = new Label("₽ 0.00");

        VBox incomeBox = createSummaryItem("Total Income", totalIncomeLabel);
        VBox expensesBox = createSummaryItem("Total Expenses", totalExpensesLabel);
        VBox profitBox = createSummaryItem("Net Profit", netProfitLabel);

        HBox.setHgrow(incomeBox, Priority.ALWAYS);
        HBox.setHgrow(expensesBox, Priority.ALWAYS);
        HBox.setHgrow(profitBox, Priority.ALWAYS);

        container.getChildren().addAll(incomeBox, expensesBox, profitBox);
        return container;
    }

    private VBox createSummaryItem(String title, Label valueLabel) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("finance-summary-item");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("finance-summary-title");

        valueLabel.getStyleClass().add("finance-summary-value");

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    /**
     * Set the date range change callback.
     * Called when start or end date is changed.
     * 
     * @param callback callback that receives (startDate, endDate)
     */
    public void setOnDateRangeChanged(BiConsumer<LocalDate, LocalDate> callback) {
        BiConsumer<LocalDate, LocalDate> wrappedCallback = (start, end) -> {
            if (callback != null) {
                callback.accept(start, end);
            }
            // Update charts when date range changes
            updateIncomeExpensesChart();
            updateExpensesChart();
        };
        this.onDateRangeChanged = wrappedCallback;
    }

    /**
     * Get the selected start date.
     * 
     * @return start date or null
     */
    public LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    /**
     * Get the selected end date.
     * 
     * @return end date or null
     */
    public LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    /**
     * Set the start date.
     * 
     * @param date start date
     */
    public void setStartDate(LocalDate date) {
        startDatePicker.setValue(date);
    }

    /**
     * Set the end date.
     * 
     * @param date end date
     */
    public void setEndDate(LocalDate date) {
        endDatePicker.setValue(date);
    }

    /**
     * Update the operations table with new data.
     * 
     * @param operations list of finance operations
     */
    public void setOperations(List<FinanceOperation> operations) {
        operationsData.clear();
        if (operations != null) {
            operationsData.addAll(operations);
        }
    }

    /**
     * Update the summary totals.
     * 
     * @param totalIncome total income amount
     * @param totalExpenses total expenses amount
     * @param netProfit net profit amount
     */
    public void setSummary(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal netProfit) {
        if (totalIncome != null) {
            totalIncomeLabel.setText("₽ " + totalIncome.toString());
        } else {
            totalIncomeLabel.setText("₽ 0.00");
        }

        if (totalExpenses != null) {
            totalExpensesLabel.setText("₽ " + totalExpenses.toString());
        } else {
            totalExpensesLabel.setText("₽ 0.00");
        }

        if (netProfit != null) {
            netProfitLabel.setText("₽ " + netProfit.toString());
        } else {
            netProfitLabel.setText("₽ 0.00");
        }
    }

    /**
     * Get the operations table for external access.
     * 
     * @return operations table
     */
    public TableView<FinanceOperation> getOperationsTable() {
        return operationsTable;
    }

    /**
     * Set a function to resolve user ID to user name.
     * If set, the "Created by" column will display user names instead of IDs.
     * 
     * @param resolver function that maps user ID to user name
     */
    public void setUserNameResolver(java.util.function.Function<Long, String> resolver) {
        this.userNameResolver = resolver;
        // Refresh table to apply changes
        operationsTable.refresh();
    }

    /**
     * Set FinanceService for adding expenses.
     * 
     * @param financeService finance service
     */
    public void setFinanceService(com.restaurant.pos.service.FinanceService financeService) {
        this.financeService = financeService;
        // Update income/expenses chart when service is set
        updateIncomeExpensesChart();
    }
    
    public void setAnalyticsService(com.restaurant.pos.service.AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        // Update charts when service is set
        updateIncomeExpensesChart();
        updateExpensesChart();
    }

    /**
     * Set callback to refresh the view after adding expense.
     * 
     * @param callback refresh callback
     */
    public void setOnRefresh(Runnable callback) {
        this.onRefreshCallback = () -> {
            if (callback != null) {
                callback.run();
            }
            // Also update charts when refresh is called
            updateIncomeExpensesChart();
            updateExpensesChart();
        };
    }

    /**
     * Open the add expense dialog.
     */
    private void openAddExpenseDialog() {
        AddExpenseDialog dialog = new AddExpenseDialog(financeService, expenseOperation -> {
            // Refresh view after expense is added
            if (onRefreshCallback != null) {
                onRefreshCallback.run();
            }
        });
        dialog.show();
    }

    /**
     * Delete the selected finance operation.
     * Rules:
     * - Only EXPENSE can be deleted manually
     * - INCOME linked to orders cannot be deleted
     */
    private void deleteSelectedOperation() {
        FinanceOperation selected = operationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an operation to delete.");
            return;
        }

        // Validation: Only EXPENSE can be deleted manually
        if (selected.getType() == null || selected.getType() != com.restaurant.pos.domain.enums.FinanceOperationType.EXPENSE) {
            showError("Only expense operations can be deleted manually.");
            return;
        }

        // Validation: INCOME linked to orders cannot be deleted
        // (This check is redundant since we already check for EXPENSE, but kept for clarity)
        if (selected.getType() == com.restaurant.pos.domain.enums.FinanceOperationType.INCOME 
                && selected.getRelatedOrderId() != null) {
            showError("Income operations linked to orders cannot be deleted.");
            return;
        }

        // Show confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Expense");
        confirmation.setHeaderText("Confirm Deletion");
        
        String description = selected.getDescription() != null ? selected.getDescription() : "Unknown";
        String amount = selected.getAmount() != null ? selected.getAmount().toString() : "0.00";
        confirmation.setContentText(
            "Are you sure you want to delete this expense operation?\n\n" +
            "Description: " + description + "\n" +
            "Amount: ₽ " + amount + "\n\n" +
            "This action cannot be undone."
        );

        confirmation.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Delete the operation
                if (financeService != null && selected.getId() != null) {
                    boolean deleted = financeService.deleteOperation(selected.getId());
                    if (deleted) {
                        // Refresh view after deletion
                        if (onRefreshCallback != null) {
                            onRefreshCallback.run();
                        }
                    } else {
                        showError("Failed to delete operation. Operation may not exist.");
                    }
                } else {
                    showError("Cannot delete operation: FinanceService not available or operation has no ID.");
                }
            }
        });
    }

    /**
     * Create income vs expenses bar chart.
     */
    private BarChart<String, Number> createIncomeExpensesChart() {
        // X-axis: Category (Income/Expenses)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Type");
        
        // Y-axis: Amount
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (₽)");
        yAxis.setAutoRanging(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(null); // Title will be in container
        chart.setLegendVisible(false);
        chart.setPrefHeight(250);
        chart.setMinHeight(200);
        chart.getStyleClass().add("income-expenses-chart");
        return chart;
    }

    /**
     * Create expenses pie chart.
     */
    private PieChart createExpensesChart() {
        PieChart chart = new PieChart();
        chart.setTitle(null); // Title will be in container
        chart.setPrefHeight(250);
        chart.setMinHeight(200);
        chart.getStyleClass().add("expenses-chart");
        return chart;
    }

    /**
     * Update income vs expenses chart with data for selected date range.
     */
    private void updateIncomeExpensesChart() {
        if (incomeExpensesChart == null || financeService == null) {
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            incomeExpensesChart.getData().clear();
            return;
        }

        try {
            // Convert LocalDate to LocalDateTime for FinanceService
            java.time.LocalDateTime startDateTime = startDate.atStartOfDay();
            java.time.LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // Get total income and expenses for the selected date range
            BigDecimal totalIncome = financeService.calculateTotalIncome(startDateTime, endDateTime);
            BigDecimal totalExpenses = financeService.calculateTotalExpenses(startDateTime, endDateTime);

            // Clear existing data
            incomeExpensesChart.getData().clear();

            // Check if there's any data
            boolean hasData = (totalIncome != null && totalIncome.signum() > 0) || 
                             (totalExpenses != null && totalExpenses.signum() > 0);

            if (hasData) {
                // Create series for income and expenses
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Amount");

                // Add income bar
                if (totalIncome != null) {
                    series.getData().add(new XYChart.Data<>("Income", totalIncome.doubleValue()));
                } else {
                    series.getData().add(new XYChart.Data<>("Income", 0));
                }

                // Add expenses bar
                if (totalExpenses != null) {
                    series.getData().add(new XYChart.Data<>("Expenses", totalExpenses.doubleValue()));
                } else {
                    series.getData().add(new XYChart.Data<>("Expenses", 0));
                }

                incomeExpensesChart.getData().add(series);
            }
            
            // Show/hide empty state based on data availability
            showChartEmptyState(incomeExpensesChartEmptyState, !hasData);
        } catch (Exception e) {
            // Silently fail - chart will just be empty
            incomeExpensesChart.getData().clear();
        }
    }

    /**
     * Public method to refresh all charts.
     * Can be called externally when data changes (e.g., when orders are created).
     */
    public void refreshCharts() {
        updateIncomeExpensesChart();
        updateExpensesChart();
    }

    /**
     * Update expenses chart with data for selected date range.
     */
    private void updateExpensesChart() {
        if (expensesChart == null || analyticsService == null) {
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            expensesChart.getData().clear();
            return;
        }

        try {
            // Get expenses per category for the selected date range
            java.util.Map<String, BigDecimal> expensesPerCategory = 
                analyticsService.getExpensesPerCategory(startDate, endDate);

            // Clear existing data
            expensesChart.getData().clear();

            boolean hasData = false;
            
            // Add data points for each category
            for (java.util.Map.Entry<String, BigDecimal> entry : expensesPerCategory.entrySet()) {
                String category = entry.getKey();
                BigDecimal amount = entry.getValue();
                
                if (amount != null && amount.signum() > 0) {
                    hasData = true;
                    PieChart.Data data = new PieChart.Data(category, amount.doubleValue());
                    expensesChart.getData().add(data);
                }
            }
            
            // Show/hide empty state based on data availability
            showChartEmptyState(expensesChartEmptyState, !hasData);
        } catch (Exception e) {
            // Silently fail - chart will just be empty
            expensesChart.getData().clear();
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

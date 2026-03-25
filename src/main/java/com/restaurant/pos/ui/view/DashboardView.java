package com.restaurant.pos.ui.view;

import com.restaurant.pos.ui.model.OperationRowDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardView extends VBox {

    private final Label revenueValueLabel;
    private final Label profitValueLabel;
    private final Label marginValueLabel;
    private final Label ordersCountLabel;
    private final Label ordersRevenueLabel;
    private final HBox kpiCardsContainer;
    private final TableView<OperationRowDTO> operationsTable;
    private final ObservableList<OperationRowDTO> operationsData;
    
    // Today's statistic cards
    private final Label todayRevenueLabel;
    private final Label todayOrdersLabel;
    private final Label averageOrderValueLabel;
    private final Label todayNetProfitLabel;
    private final Label shiftRevenueLabel;
    private final Label shiftProfitLabel;
    private final Label openTablesLabel;
    private final javafx.scene.control.Button closeShiftButton;
    private final HBox todayStatsContainer;
    
    // Revenue chart
    private final LineChart<String, Number> revenueChart;
    private final VBox revenueChartEmptyState;
    
    // Orders count chart
    private final BarChart<String, Number> ordersChart;
    private final VBox ordersChartEmptyState;

    // Low stock alert
    private final VBox lowStockAlertPanel;

    // Management Overview (ADMIN)
    private final VBox managementOverviewPanel;
    private final Label mgmtRevenueLabel;
    private final Label mgmtProfitLabel;
    private final Label mgmtBestSellingLabel;
    private final Label mgmtMostProfitableLabel;
    private final Label mgmtWeakestMarginLabel;
    private final Label mgmtBestWaiterLabel;
    private final Label mgmtIngredientsConsumedLabel;
    private final Label mgmtAvgOrderLabel;

    public DashboardView() {
        setPadding(new Insets(16));
        setSpacing(16);
        getStyleClass().add("dashboard-root");

        // Initialize KPI labels with default values
        revenueValueLabel = new Label("₽ 0");
        profitValueLabel = new Label("₽ 0");
        marginValueLabel = new Label("0%");
        ordersCountLabel = new Label("0");
        ordersRevenueLabel = new Label("₽ 0.00");

        // Initialize today's statistic labels
        todayRevenueLabel = new Label("₽ 0.00");
        todayOrdersLabel = new Label("0");
        averageOrderValueLabel = new Label("₽ 0.00");
        todayNetProfitLabel = new Label("₽ 0.00");
        shiftRevenueLabel = new Label("₽ 0.00");
        shiftProfitLabel = new Label("₽ 0.00");
        openTablesLabel = new Label("0 / 0");
        closeShiftButton = new javafx.scene.control.Button("Close Shift");

        // Create KPI cards container
        kpiCardsContainer = createKpiCardsContainer();
        // Create today's statistics container
        todayStatsContainer = createTodayStatsContainer();
        VBox ordersSummaryBox = createOrdersSummaryBox();

        // Create revenue chart with container
        revenueChart = createRevenueChart();
        revenueChartEmptyState = createChartEmptyState("No revenue data available for the selected period");
        VBox revenueChartContainer = wrapChartInContainerWithEmptyState(revenueChart, revenueChartEmptyState, "Revenue per Day (Last 7 Days)");
        
        // Create orders count chart with container
        ordersChart = createOrdersChart();
        ordersChartEmptyState = createChartEmptyState("No orders data available for the selected period");
        VBox ordersChartContainer = wrapChartInContainerWithEmptyState(ordersChart, ordersChartEmptyState, "Orders Count per Day (Last 7 Days)");

        // Create operations table
        operationsData = FXCollections.observableArrayList();
        operationsTable = createOperationsTable();

        // Low stock alert panel
        lowStockAlertPanel = createLowStockAlertPanel();

        // Management Overview panel
        managementOverviewPanel = createManagementOverviewPanel();
        mgmtRevenueLabel = new Label("—");
        mgmtProfitLabel = new Label("—");
        mgmtBestSellingLabel = new Label("—");
        mgmtMostProfitableLabel = new Label("—");
        mgmtWeakestMarginLabel = new Label("—");
        mgmtBestWaiterLabel = new Label("—");
        mgmtIngredientsConsumedLabel = new Label("—");
        mgmtAvgOrderLabel = new Label("—");
        populateManagementOverviewPanel();

        getChildren().addAll(lowStockAlertPanel, managementOverviewPanel, todayStatsContainer, kpiCardsContainer, revenueChartContainer, ordersChartContainer, ordersSummaryBox, operationsTable);
        VBox.setVgrow(revenueChartContainer, Priority.ALWAYS);
        VBox.setVgrow(ordersChartContainer, Priority.ALWAYS);
        VBox.setVgrow(operationsTable, Priority.ALWAYS);
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
        chartPane.setMinHeight(250);
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

    private HBox createTodayStatsContainer() {
        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("today-stats-container");

        VBox revenueCard = createStatCard("Today's Revenue", todayRevenueLabel);
        VBox ordersCard = createStatCard("Orders Today", todayOrdersLabel);
        VBox avgOrderCard = createStatCard("Average Order Value", averageOrderValueLabel);
        VBox profitCard = createStatCard("Net Profit Today", todayNetProfitLabel);
        VBox shiftRevenueCard = createStatCard("Current Shift Revenue", shiftRevenueLabel);
        VBox shiftProfitCard = createStatCard("Current Shift Profit", shiftProfitLabel);
        VBox openTablesCard = createStatCard("Open Tables", openTablesLabel);

        HBox shiftRow = new HBox(12);
        shiftRow.getChildren().addAll(shiftRevenueCard, shiftProfitCard, openTablesCard, closeShiftButton);

        container.getChildren().addAll(revenueCard, ordersCard, avgOrderCard, profitCard);
        container.getChildren().add(shiftRow);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        HBox.setHgrow(ordersCard, Priority.ALWAYS);
        HBox.setHgrow(avgOrderCard, Priority.ALWAYS);
        HBox.setHgrow(profitCard, Priority.ALWAYS);

        return container;
    }

    private HBox createKpiCardsContainer() {
        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_LEFT);

        VBox revenueCard = createKpiCard("Выручка", revenueValueLabel);
        VBox profitCard = createKpiCard("Чистая прибыль", profitValueLabel);
        VBox marginCard = createKpiCard("Средняя маржа", marginValueLabel);

        container.getChildren().addAll(revenueCard, profitCard, marginCard);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        HBox.setHgrow(profitCard, Priority.ALWAYS);
        HBox.setHgrow(marginCard, Priority.ALWAYS);

        return container;
    }

    private VBox createStatCard(String title, Label valueLabel) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("stat-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-title");

        valueLabel.getStyleClass().add("stat-card-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Create revenue line chart for last 7 days.
     */
    private LineChart<String, Number> createRevenueChart() {
        // X-axis: Date (formatted as day/month)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        
        // Y-axis: Revenue
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue (₽)");
        yAxis.setAutoRanging(true);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(null); // Title will be in container
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);
        chart.setMinHeight(250);
        chart.getStyleClass().add("revenue-chart");

        return chart;
    }

    /**
     * Update revenue chart with data for last 7 days.
     * 
     * @param revenueData map of date to revenue amount
     */
    public void setRevenueChartData(Map<LocalDate, BigDecimal> revenueData) {
        if (revenueChart == null || revenueData == null) {
            showChartEmptyState(revenueChartEmptyState, true);
            return;
        }

        // Clear existing data
        revenueChart.getData().clear();

        // Create series for revenue
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // Last 7 days including today

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM");
        boolean hasData = false;

        // Add data points for each day
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            BigDecimal revenue = revenueData.getOrDefault(date, BigDecimal.ZERO);
            
            if (revenue.signum() > 0) {
                hasData = true;
            }
            
            // X-axis: formatted date (dd.MM), Y-axis: revenue amount
            String dateLabel = date.format(dateFormatter);
            revenueSeries.getData().add(new XYChart.Data<>(dateLabel, revenue.doubleValue()));
        }

        revenueChart.getData().add(revenueSeries);
        
        // Show/hide empty state based on data availability
        showChartEmptyState(revenueChartEmptyState, !hasData);
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
     * Create orders count bar chart for last 7 days.
     */
    private BarChart<String, Number> createOrdersChart() {
        // X-axis: Date (formatted as day/month)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        
        // Y-axis: Orders count
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Orders Count");
        yAxis.setAutoRanging(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(null); // Title will be in container
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);
        chart.setMinHeight(250);
        chart.getStyleClass().add("orders-chart");

        return chart;
    }

    /**
     * Update orders chart with data for last 7 days.
     * 
     * @param ordersData map of date to orders count
     */
    public void setOrdersChartData(Map<LocalDate, Integer> ordersData) {
        if (ordersChart == null || ordersData == null) {
            showChartEmptyState(ordersChartEmptyState, true);
            return;
        }

        // Clear existing data
        ordersChart.getData().clear();

        // Create series for orders count
        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Orders");

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // Last 7 days including today

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM");
        boolean hasData = false;

        // Add data points for each day
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            Integer ordersCount = ordersData.getOrDefault(date, 0);
            
            if (ordersCount > 0) {
                hasData = true;
            }
            
            // X-axis: formatted date (dd.MM), Y-axis: orders count
            String dateLabel = date.format(dateFormatter);
            ordersSeries.getData().add(new XYChart.Data<>(dateLabel, ordersCount));
        }

        ordersChart.getData().add(ordersSeries);
        
        // Show/hide empty state based on data availability
        showChartEmptyState(ordersChartEmptyState, !hasData);
    }

    private VBox createKpiCard(String title, Label valueLabel) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("kpi-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kpi-title");

        valueLabel.getStyleClass().add("kpi-value");

        Label periodLabel = new Label("за период");
        periodLabel.getStyleClass().add("kpi-period");

        card.getChildren().addAll(titleLabel, valueLabel, periodLabel);
        return card;
    }

    /**
     * Summary box for orders: shows total orders count and total revenue (mocked).
     * Values are provided from controller based on in-memory orders list.
     */
    private VBox createOrdersSummaryBox() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8, 16, 8, 16));
        box.getStyleClass().add("orders-summary-box");

        Label title = new Label("Сводка по заказам");
        title.getStyleClass().add("orders-summary-title");

        HBox countRow = new HBox(8);
        countRow.setAlignment(Pos.CENTER_LEFT);
        Label countLabel = new Label("Всего заказов:");
        countLabel.getStyleClass().add("orders-summary-label");
        ordersCountLabel.getStyleClass().add("orders-summary-value");
        countRow.getChildren().addAll(countLabel, ordersCountLabel);

        HBox revenueRow = new HBox(8);
        revenueRow.setAlignment(Pos.CENTER_LEFT);
        Label revenueLabel = new Label("Выручка (mock):");
        revenueLabel.getStyleClass().add("orders-summary-label");
        ordersRevenueLabel.getStyleClass().add("orders-summary-value");
        revenueRow.getChildren().addAll(revenueLabel, ordersRevenueLabel);

        box.getChildren().addAll(title, countRow, revenueRow);
        return box;
    }

    @SuppressWarnings("unchecked")
    private TableView<OperationRowDTO> createOperationsTable() {
        TableView<OperationRowDTO> tableView = new TableView<>();
        tableView.getStyleClass().add("operations-table");
        tableView.setItems(operationsData);

        // Column: Время
        TableColumn<OperationRowDTO, LocalDateTime> timeCol = new TableColumn<>("Время");
        timeCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getTime()));
        timeCol.setCellFactory(column -> new javafx.scene.control.TableCell<OperationRowDTO, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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

        // Column: Тип операции
        TableColumn<OperationRowDTO, String> typeCol = new TableColumn<>("Тип операции");
        typeCol.setCellValueFactory(param -> {
            String type = param.getValue().getOperationType();
            return new SimpleStringProperty(type != null ? type : "");
        });

        // Column: Сумма
        TableColumn<OperationRowDTO, BigDecimal> amountCol = new TableColumn<>("Сумма");
        amountCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAmount()));
        amountCol.setCellFactory(column -> new javafx.scene.control.TableCell<OperationRowDTO, BigDecimal>() {
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

        // Column: Сотрудник
        TableColumn<OperationRowDTO, String> employeeCol = new TableColumn<>("Сотрудник");
        employeeCol.setCellValueFactory(param -> {
            String employee = param.getValue().getEmployee();
            return new SimpleStringProperty(employee != null ? employee : "");
        });

        tableView.getColumns().addAll(timeCol, typeCol, amountCol, employeeCol);

        // Set placeholder
        Label placeholder = new Label("Нет операций для отображения");
        tableView.setPlaceholder(placeholder);

        return tableView;
    }

    /**
     * Устанавливает значения KPI метрик.
     * 
     * @param revenue выручка (форматированная строка, например "₽ 1 234.56")
     * @param netProfit чистая прибыль (форматированная строка)
     * @param margin средняя маржа (форматированная строка, например "15.5%")
     */
    public void setKpiValues(String revenue, String netProfit, String margin) {
        if (revenue != null) {
            revenueValueLabel.setText(revenue);
        }
        if (netProfit != null) {
            profitValueLabel.setText(netProfit);
        }
        if (margin != null) {
            marginValueLabel.setText(margin);
        }
    }

    /**
     * Устанавливает сводку по заказам (количество и выручка).
     * Значения рассчитываются по in-memory списку заказов в контроллере/сервисах.
     *
     * @param totalOrders общее количество заказов
     * @param totalRevenue выручка (форматированная строка, например "₽ 1 234.56")
     */
    public void setOrderSummary(int totalOrders, String totalRevenue) {
        ordersCountLabel.setText(String.valueOf(totalOrders));
        if (totalRevenue != null) {
            ordersRevenueLabel.setText(totalRevenue);
        }
    }

    /**
     * Устанавливает список операций для отображения в таблице.
     * 
     * @param operations список операций
     */
    public void setOperations(List<OperationRowDTO> operations) {
        operationsData.clear();
        if (operations != null) {
            operationsData.addAll(operations);
        }
    }

    /**
     * Отображает сообщение об ошибке пользователю.
     * 
     * @param message текст ошибки
     */
    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Update today's statistics.
     * 
     * @param revenue today's revenue
     * @param ordersCount today's orders count
     * @param averageOrderValue average order value today
     * @param netProfit today's net profit
     */
    public void setShiftStats(BigDecimal revenue, BigDecimal profit) {
        if (shiftRevenueLabel != null) shiftRevenueLabel.setText(revenue != null ? formatCurrency(revenue) : "₽ 0.00");
        if (shiftProfitLabel != null) shiftProfitLabel.setText(profit != null ? formatCurrency(profit) : "₽ 0.00");
    }

    public void setOpenTables(int free, int total) {
        if (openTablesLabel != null) openTablesLabel.setText(free + " free / " + total + " total");
    }

    public void setOnCloseShift(java.lang.Runnable handler) {
        if (closeShiftButton != null) closeShiftButton.setOnAction(e -> {
            if (handler != null) handler.run();
        });
    }

    public void setLowStockIngredients(java.util.List<com.restaurant.pos.domain.model.Product> lowStock) {
        if (lowStockAlertPanel == null) return;
        lowStockAlertPanel.getChildren().clear();
        if (lowStock == null || lowStock.isEmpty()) {
            lowStockAlertPanel.setVisible(false);
            return;
        }
        lowStockAlertPanel.setVisible(true);
        Label title = new Label("⚠ Low Stock Alert");
        title.getStyleClass().add("alert-title");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        lowStockAlertPanel.getChildren().add(title);
        for (com.restaurant.pos.domain.model.Product p : lowStock) {
            if (p == null) continue;
            String name = p.getName() != null ? p.getName() : "Unknown";
            String qty = p.getQuantityInStock() != null ? p.getQuantityInStock().toString() : "0";
            String unit = p.getUnit() != null ? p.getUnit() : "";
            Label item = new Label(String.format("• %s: %s %s", name, qty, unit));
            item.setStyle("-fx-text-fill: #d32f2f;");
            lowStockAlertPanel.getChildren().add(item);
        }
    }

    private VBox createLowStockAlertPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color: #ffebee; -fx-border-color: #d32f2f; -fx-border-width: 2; -fx-border-radius: 4;");
        panel.setVisible(false);
        return panel;
    }

    private VBox createManagementOverviewPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #1976d2; -fx-border-width: 2; -fx-border-radius: 4;");
        Label title = new Label("Management Overview");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        panel.getChildren().add(title);
        return panel;
    }

    private void populateManagementOverviewPanel() {
        if (managementOverviewPanel == null || managementOverviewPanel.getChildren().size() <= 1) return;
        managementOverviewPanel.getChildren().clear();
        Label title = new Label("Management Overview");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        managementOverviewPanel.getChildren().add(title);
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(6);
        grid.add(new Label("Revenue:"), 0, 0);
        grid.add(mgmtRevenueLabel, 1, 0);
        grid.add(new Label("Profit:"), 0, 1);
        grid.add(mgmtProfitLabel, 1, 1);
        grid.add(new Label("Best selling dish:"), 0, 2);
        grid.add(mgmtBestSellingLabel, 1, 2);
        grid.add(new Label("Most profitable dish:"), 0, 3);
        grid.add(mgmtMostProfitableLabel, 1, 3);
        grid.add(new Label("Weakest margin dish:"), 0, 4);
        grid.add(mgmtWeakestMarginLabel, 1, 4);
        grid.add(new Label("Best waiter (by profit):"), 0, 5);
        grid.add(mgmtBestWaiterLabel, 1, 5);
        grid.add(new Label("Total ingredients consumed:"), 0, 6);
        grid.add(mgmtIngredientsConsumedLabel, 1, 6);
        grid.add(new Label("Average order value:"), 0, 7);
        grid.add(mgmtAvgOrderLabel, 1, 7);
        managementOverviewPanel.getChildren().add(grid);
    }

    public void setManagementSummary(com.restaurant.pos.service.BIAnalyticsService.ManagementSummary s) {
        if (s == null) return;
        if (mgmtRevenueLabel != null) mgmtRevenueLabel.setText(formatCurrency(s.revenue()));
        if (mgmtProfitLabel != null) mgmtProfitLabel.setText(formatCurrency(s.profit()));
        if (mgmtBestSellingLabel != null) mgmtBestSellingLabel.setText(s.bestSellingDish() != null ? s.bestSellingDish() : "—");
        if (mgmtMostProfitableLabel != null) mgmtMostProfitableLabel.setText(s.mostProfitableDish() != null ? s.mostProfitableDish() : "—");
        if (mgmtWeakestMarginLabel != null) mgmtWeakestMarginLabel.setText(s.weakestMarginDish() != null ? s.weakestMarginDish() : "—");
        if (mgmtBestWaiterLabel != null) mgmtBestWaiterLabel.setText(s.bestWaiterByProfit() != null ? s.bestWaiterByProfit() : "—");
        if (mgmtIngredientsConsumedLabel != null) mgmtIngredientsConsumedLabel.setText(s.totalIngredientsConsumed() != null ? formatCurrency(s.totalIngredientsConsumed()) : "—");
        if (mgmtAvgOrderLabel != null) mgmtAvgOrderLabel.setText(s.averageOrderValue() != null ? formatCurrency(s.averageOrderValue()) : "—");
    }

    public void setTodayStats(BigDecimal revenue, int ordersCount, BigDecimal averageOrderValue, BigDecimal netProfit) {
        if (revenue != null) {
            todayRevenueLabel.setText(formatCurrency(revenue));
        } else {
            todayRevenueLabel.setText("₽ 0.00");
        }

        todayOrdersLabel.setText(String.valueOf(ordersCount));

        if (averageOrderValue != null) {
            averageOrderValueLabel.setText(formatCurrency(averageOrderValue));
        } else {
            averageOrderValueLabel.setText("₽ 0.00");
        }

        if (netProfit != null) {
            todayNetProfitLabel.setText(formatCurrency(netProfit));
        } else {
            todayNetProfitLabel.setText("₽ 0.00");
        }
    }

    /**
     * Format currency value.
     * 
     * @param value amount to format
     * @return formatted string
     */
    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "₽ 0.00";
        }
        java.text.NumberFormat formatter = new java.text.DecimalFormat("#,##0.00");
        return "₽ " + formatter.format(value);
    }
}


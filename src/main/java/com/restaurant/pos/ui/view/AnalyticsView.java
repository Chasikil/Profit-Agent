package com.restaurant.pos.ui.view;

import com.restaurant.pos.service.AnalyticsService;
import com.restaurant.pos.service.BIAnalyticsService;
import com.restaurant.pos.ui.controller.AnalyticsController;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Analytics view for ADMIN.
 * Period-based: margin analysis, waiter KPI, Excel export.
 */
public class AnalyticsView extends VBox {

    private static final NumberFormat MONEY = new DecimalFormat("#,##0.00 ₽");

    private final AnalyticsController controller;
    private final ComboBox<AnalyticsController.PeriodType> periodCombo;
    private final DatePicker customStartPicker;
    private final DatePicker customEndPicker;
    private final Label totalRevenueLabel;
    private final Label totalCostLabel;
    private final Label totalProfitLabel;
    private final Label marginLabel;
    private final Label mostProfitableLabel;
    private final Label leastProfitableLabel;
    private final Label topSellingLabel;
    private final Label highestMarginLabel;
    private final Label lowestMarginLabel;
    private final TableView<DishMarginRow> dishTable;
    private final TableView<WaiterKPIRow> waiterTable;
    private final LineChart<String, Number> dailyRevenueChart;
    private final BarChart<String, Number> monthlyRevenueChart;
    private final LineChart<String, Number> profitTrendChart;
    private final Button exportButton;
    private AnalyticsService analyticsService;

    public AnalyticsView(AnalyticsController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        setSpacing(16);
        getStyleClass().add("analytics-view");

        Label titleLabel = new Label("Profit Analytics");
        titleLabel.getStyleClass().add("panel-title");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Period filter
        periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll(
                AnalyticsController.PeriodType.TODAY,
                AnalyticsController.PeriodType.THIS_WEEK,
                AnalyticsController.PeriodType.THIS_MONTH,
                AnalyticsController.PeriodType.CUSTOM
        );
        periodCombo.setValue(AnalyticsController.PeriodType.THIS_MONTH);
        periodCombo.valueProperty().addListener((o, ov, nv) -> refresh());
        customStartPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        customEndPicker = new DatePicker(LocalDate.now());
        customStartPicker.valueProperty().addListener((o, ov, nv) -> refresh());
        customEndPicker.valueProperty().addListener((o, ov, nv) -> refresh());
        HBox periodBox = new HBox(10);
        periodBox.getChildren().addAll(
                new Label("Period:"),
                periodCombo,
                new Label("From:"),
                customStartPicker,
                new Label("To:"),
                customEndPicker
        );
        customStartPicker.setVisible(false);
        customEndPicker.setVisible(false);
        periodCombo.setOnAction(e -> updateCustomPickersVisibility());

        totalRevenueLabel = new Label("Total Revenue: —");
        totalCostLabel = new Label("Total Cost: —");
        totalProfitLabel = new Label("Total Profit: —");
        marginLabel = new Label("Margin %: —");
        mostProfitableLabel = new Label("Most profitable dish: —");
        leastProfitableLabel = new Label("Least profitable dish: —");
        topSellingLabel = new Label("Top selling dish: —");
        highestMarginLabel = new Label("Highest margin dish: —");
        lowestMarginLabel = new Label("Lowest margin dish: —");

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(24);
        summaryGrid.setVgap(8);
        summaryGrid.add(totalRevenueLabel, 0, 0);
        summaryGrid.add(totalCostLabel, 0, 1);
        summaryGrid.add(totalProfitLabel, 0, 2);
        summaryGrid.add(marginLabel, 0, 3);
        summaryGrid.add(mostProfitableLabel, 0, 4);
        summaryGrid.add(leastProfitableLabel, 0, 5);
        summaryGrid.add(topSellingLabel, 0, 6);
        summaryGrid.add(highestMarginLabel, 0, 7);
        summaryGrid.add(lowestMarginLabel, 0, 8);

        Label dishTitle = new Label("Dish Margin Analysis");
        dishTitle.getStyleClass().add("panel-title");
        dishTable = createDishTable();

        Label waiterTitle = new Label("Waiter Performance");
        waiterTitle.getStyleClass().add("panel-title");
        waiterTable = createWaiterKPITable();

        exportButton = new Button("Export to Excel");
        exportButton.setOnAction(e -> handleExport());

        dailyRevenueChart = createDailyRevenueChart();
        monthlyRevenueChart = createMonthlyRevenueChart();
        profitTrendChart = createProfitTrendChart();

        VBox chartsBox = new VBox(16);
        chartsBox.getChildren().addAll(
                new Label("Daily Revenue (Last 7 Days)"),
                dailyRevenueChart,
                new Label("Monthly Revenue"),
                monthlyRevenueChart,
                new Label("Profit Trend (Last 7 Days)"),
                profitTrendChart
        );

        VBox content = new VBox(12,
                titleLabel,
                periodBox,
                summaryGrid,
                dishTitle,
                dishTable,
                waiterTitle,
                waiterTable,
                exportButton,
                chartsBox
        );
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(0));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(scroll);
        refresh();
    }

    private void updateCustomPickersVisibility() {
        boolean custom = periodCombo.getValue() == AnalyticsController.PeriodType.CUSTOM;
        customStartPicker.setVisible(custom);
        customEndPicker.setVisible(custom);
    }

    private TableView<DishMarginRow> createDishTable() {
        TableView<DishMarginRow> table = new TableView<>();
        TableColumn<DishMarginRow, String> nameCol = new TableColumn<>("Dish");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().dishName));
        TableColumn<DishMarginRow, Number> soldCol = new TableColumn<>("Sold");
        soldCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().timesSold));
        TableColumn<DishMarginRow, String> revCol = new TableColumn<>("Revenue");
        revCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().revenue));
        TableColumn<DishMarginRow, String> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().cost));
        TableColumn<DishMarginRow, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().profit));
        TableColumn<DishMarginRow, String> marginCol = new TableColumn<>("Margin %");
        marginCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().marginPct));
        table.getColumns().addAll(nameCol, soldCol, revCol, costCol, profitCol, marginCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);
        return table;
    }

    private TableView<WaiterKPIRow> createWaiterKPITable() {
        TableView<WaiterKPIRow> table = new TableView<>();
        TableColumn<WaiterKPIRow, String> nameCol = new TableColumn<>("Waiter");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().waiterName));
        TableColumn<WaiterKPIRow, Number> ordersCol = new TableColumn<>("Orders");
        ordersCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().ordersHandled));
        TableColumn<WaiterKPIRow, String> revCol = new TableColumn<>("Revenue");
        revCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().totalRevenue));
        TableColumn<WaiterKPIRow, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().totalProfit));
        TableColumn<WaiterKPIRow, String> avgOrderCol = new TableColumn<>("Avg Order");
        avgOrderCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().avgOrderValue));
        TableColumn<WaiterKPIRow, String> avgProfitCol = new TableColumn<>("Avg Profit/Order");
        avgProfitCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().avgProfitPerOrder));
        table.getColumns().addAll(nameCol, ordersCol, revCol, profitCol, avgOrderCol, avgProfitCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(180);
        return table;
    }

    private LocalDate[] getCurrentPeriod() {
        LocalDate[] range = controller.getPeriodRange(
                periodCombo.getValue(),
                customStartPicker.getValue(),
                customEndPicker.getValue()
        );
        return range != null && range.length >= 2 ? range : new LocalDate[]{ LocalDate.now(), LocalDate.now() };
    }

    public void refresh() {
        if (controller == null) return;
        LocalDate[] range = getCurrentPeriod();
        LocalDate start = range[0];
        LocalDate end = range[1];

        refreshCharts();

        BIAnalyticsService.PeriodSummary summary = controller.getPeriodSummary(start, end);
        if (summary != null) {
            totalRevenueLabel.setText("Total Revenue: " + format(summary.revenue()));
            totalCostLabel.setText("Total Cost: " + format(summary.cost()));
            totalProfitLabel.setText("Total Profit: " + format(summary.profit()));
            marginLabel.setText("Margin %: " + formatPct(summary.marginPercent()));
        }

        List<BIAnalyticsService.DishMarginStats> dishStats = controller.getDishMarginStats(start, end);
        dishTable.getItems().clear();
        for (BIAnalyticsService.DishMarginStats d : dishStats) {
            dishTable.getItems().add(new DishMarginRow(
                    d.dishName(),
                    d.timesSold(),
                    format(d.revenue()),
                    format(d.totalCost()),
                    format(d.grossProfit()),
                    formatPct(d.marginPercent())
            ));
        }

        String mostProf = "—", leastProf = "—", topSell = "—", highMarg = "—", lowMarg = "—";
        if (!dishStats.isEmpty()) {
            var most = dishStats.stream().max(Comparator.comparing(BIAnalyticsService.DishMarginStats::grossProfit)).orElse(null);
            var least = dishStats.stream().min(Comparator.comparing(BIAnalyticsService.DishMarginStats::grossProfit)).orElse(null);
            var top = dishStats.stream().max(Comparator.comparingInt(BIAnalyticsService.DishMarginStats::timesSold)).orElse(null);
            var high = dishStats.stream().filter(x -> x.revenue().signum() > 0).max(Comparator.comparing(BIAnalyticsService.DishMarginStats::marginPercent)).orElse(null);
            var low = dishStats.stream().filter(x -> x.revenue().signum() > 0).min(Comparator.comparing(BIAnalyticsService.DishMarginStats::marginPercent)).orElse(null);
            if (most != null) mostProf = most.dishName() + " (" + format(most.grossProfit()) + ")";
            if (least != null) leastProf = least.dishName() + " (" + format(least.grossProfit()) + ")";
            if (top != null) topSell = top.dishName() + " (" + top.timesSold() + " sold)";
            if (high != null) highMarg = high.dishName() + " (" + formatPct(high.marginPercent()) + ")";
            if (low != null) lowMarg = low.dishName() + " (" + formatPct(low.marginPercent()) + ")";
        }
        mostProfitableLabel.setText("Most profitable dish: " + mostProf);
        leastProfitableLabel.setText("Least profitable dish: " + leastProf);
        topSellingLabel.setText("Top selling dish: " + topSell);
        highestMarginLabel.setText("Highest margin dish: " + highMarg);
        lowestMarginLabel.setText("Lowest margin dish: " + lowMarg);

        waiterTable.getItems().clear();
        List<BIAnalyticsService.WaiterKPI> waiterKPI = controller.getWaiterKPI(start, end);
        for (BIAnalyticsService.WaiterKPI w : waiterKPI) {
            waiterTable.getItems().add(new WaiterKPIRow(
                    w.waiterName(),
                    w.ordersHandled(),
                    format(w.totalRevenue()),
                    format(w.totalProfit()),
                    format(w.averageOrderValue()),
                    format(w.averageProfitPerOrder())
            ));
        }
    }

    private void handleExport() {
        LocalDate[] range = getCurrentPeriod();
        String path = controller.exportToExcel(range[0], range[1]);
        if (path != null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Export");
            a.setHeaderText(null);
            a.setContentText("Report exported to:\n" + path);
            a.showAndWait();
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Export Failed");
            a.setHeaderText(null);
            a.setContentText("Could not export report.");
            a.showAndWait();
        }
    }

    public void setAnalyticsService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        refreshCharts();
    }

    private LineChart<String, Number> createDailyRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setPrefHeight(250);
        chart.setLegendVisible(false);
        return chart;
    }

    private BarChart<String, Number> createMonthlyRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefHeight(250);
        chart.setLegendVisible(false);
        return chart;
    }

    private LineChart<String, Number> createProfitTrendChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setPrefHeight(250);
        chart.setLegendVisible(false);
        return chart;
    }

    private void refreshCharts() {
        if (analyticsService == null) return;
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        var dailyRevenue = analyticsService.getRevenuePerDay(weekAgo, today);
        dailyRevenueChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekAgo.plusDays(i);
            BigDecimal rev = dailyRevenue.getOrDefault(date, BigDecimal.ZERO);
            series.getData().add(new XYChart.Data<>(date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM")), rev.doubleValue()));
        }
        dailyRevenueChart.getData().add(series);

        var profit = analyticsService.getProfitPerDay(weekAgo, today);
        profitTrendChart.getData().clear();
        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekAgo.plusDays(i);
            BigDecimal prof = profit.getOrDefault(date, BigDecimal.ZERO);
            profitSeries.getData().add(new XYChart.Data<>(date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM")), prof.doubleValue()));
        }
        profitTrendChart.getData().add(profitSeries);

        LocalDate monthStart = today.withDayOfMonth(1);
        var monthly = analyticsService.getRevenuePerDay(monthStart, today);
        monthlyRevenueChart.getData().clear();
        XYChart.Series<String, Number> monthlySeries = new XYChart.Series<>();
        monthly.forEach((date, rev) -> monthlySeries.getData().add(new XYChart.Data<>(date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM")), rev.doubleValue())));
        monthlyRevenueChart.getData().add(monthlySeries);
    }

    private static String format(BigDecimal v) {
        if (v == null) return "0.00 ₽";
        return MONEY.format(v.doubleValue());
    }

    private static String formatPct(BigDecimal v) {
        if (v == null) return "0%";
        return v.setScale(2, java.math.RoundingMode.HALF_UP) + "%";
    }

    private static class DishMarginRow {
        final String dishName;
        final int timesSold;
        final String revenue, cost, profit, marginPct;

        DishMarginRow(String dishName, int timesSold, String revenue, String cost, String profit, String marginPct) {
            this.dishName = dishName;
            this.timesSold = timesSold;
            this.revenue = revenue;
            this.cost = cost;
            this.profit = profit;
            this.marginPct = marginPct;
        }
    }

    private static class WaiterKPIRow {
        final String waiterName;
        final int ordersHandled;
        final String totalRevenue, totalProfit, avgOrderValue, avgProfitPerOrder;

        WaiterKPIRow(String waiterName, int ordersHandled, String totalRevenue, String totalProfit, String avgOrderValue, String avgProfitPerOrder) {
            this.waiterName = waiterName;
            this.ordersHandled = ordersHandled;
            this.totalRevenue = totalRevenue;
            this.totalProfit = totalProfit;
            this.avgOrderValue = avgOrderValue;
            this.avgProfitPerOrder = avgProfitPerOrder;
        }
    }
}

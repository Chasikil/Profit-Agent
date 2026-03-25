package com.restaurant.pos.ui.view;

import com.restaurant.pos.ui.controller.DirectorDashboardController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * JavaFX view for director dashboard.
 * Displays financial summary blocks: Revenue, Cost, Profit, Margin, Payroll.
 * UI does not calculate - all data comes from controller/reports.
 */
public class DirectorDashboardView extends BorderPane {

    private final Label revenueValueLabel;
    private final Label costValueLabel;
    private final Label profitValueLabel;
    private final Label marginValueLabel;
    private final Label payrollValueLabel;
    private final Button refreshButton;

    public DirectorDashboardView(DirectorDashboardController controller) {
        setPadding(new Insets(16));
        getStyleClass().add("director-dashboard-view");

        // Initialize labels
        revenueValueLabel = new Label("₽ 0.00");
        costValueLabel = new Label("₽ 0.00");
        profitValueLabel = new Label("₽ 0.00");
        marginValueLabel = new Label("0.00%");
        payrollValueLabel = new Label("₽ 0.00");

        // Create summary cards grid
        GridPane cardsGrid = createSummaryCards();

        // Create refresh button
        HBox buttonBar = new HBox(8);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(8, 0, 0, 0));
        refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> controller.refreshReports());
        buttonBar.getChildren().add(refreshButton);

        // Layout
        VBox centerBox = new VBox(16, cardsGrid, buttonBar);
        setCenter(centerBox);
    }

    private GridPane createSummaryCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        // Column constraints: 5 columns, each 20% width
        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(20.0);
            grid.getColumnConstraints().add(col);
        }

        // Create summary cards with labels
        VBox revenueCard = createStatCard("Выручка", revenueValueLabel);
        VBox costCard = createStatCard("Себестоимость", costValueLabel);
        VBox profitCard = createStatCard("Прибыль", profitValueLabel);
        VBox marginCard = createStatCard("Маржа", marginValueLabel);
        VBox payrollCard = createStatCard("Зарплаты", payrollValueLabel);

        // Add cards to grid
        grid.add(revenueCard, 0, 0);
        grid.add(costCard, 1, 0);
        grid.add(profitCard, 2, 0);
        grid.add(marginCard, 3, 0);
        grid.add(payrollCard, 4, 0);

        return grid;
    }

    private VBox createStatCard(String title, Label valueLabel) {
        VBox card = new VBox();
        card.setPadding(new Insets(16));
        card.setSpacing(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("stat-card");
        card.setPrefHeight(120);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        valueLabel.getStyleClass().add("stat-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Update sales data (revenue, cost, profit, margin).
     * Called by controller with calculated values.
     */
    public void updateSalesData(BigDecimal revenue, BigDecimal cost, BigDecimal profit, BigDecimal marginPercent) {
        NumberFormat currencyFormat = new DecimalFormat("#,##0.00");
        NumberFormat percentFormat = new DecimalFormat("#,##0.00");

        if (revenue != null) {
            revenueValueLabel.setText("₽ " + currencyFormat.format(revenue.doubleValue()));
        } else {
            revenueValueLabel.setText("₽ 0.00");
        }

        if (cost != null) {
            costValueLabel.setText("₽ " + currencyFormat.format(cost.doubleValue()));
        } else {
            costValueLabel.setText("₽ 0.00");
        }

        if (profit != null) {
            profitValueLabel.setText("₽ " + currencyFormat.format(profit.doubleValue()));
        } else {
            profitValueLabel.setText("₽ 0.00");
        }

        if (marginPercent != null) {
            marginValueLabel.setText(percentFormat.format(marginPercent.doubleValue()) + "%");
        } else {
            marginValueLabel.setText("0.00%");
        }
    }

    /**
     * Update payroll data.
     * Called by controller with calculated value.
     */
    public void updatePayrollData(BigDecimal payroll) {
        NumberFormat currencyFormat = new DecimalFormat("#,##0.00");

        if (payroll != null) {
            payrollValueLabel.setText("₽ " + currencyFormat.format(payroll.doubleValue()));
        } else {
            payrollValueLabel.setText("₽ 0.00");
        }
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Ошибка");
        alert.showAndWait();
    }
}

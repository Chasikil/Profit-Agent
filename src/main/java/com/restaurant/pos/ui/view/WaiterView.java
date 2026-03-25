package com.restaurant.pos.ui.view;

import com.restaurant.pos.ui.controller.WaiterController;
import com.restaurant.pos.ui.model.DishDTO;
import com.restaurant.pos.ui.model.OrderDTO;
import com.restaurant.pos.ui.model.OrderItemDTO;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Waiter view for order management.
 * Layout:
 * - Left: Menu list (only available dishes)
 * - Center: Current order items table
 * - Right: Order summary with buttons
 * 
 * No admin functions.
 */
public class WaiterView extends BorderPane {

    private final WaiterController controller;

    private ListView<DishDTO> menuListView;
    private TableView<OrderItemDTO> orderItemsTable;
    private Label totalLabel;
    private Button addDishButton;
    private Button increaseQuantityButton;
    private Button decreaseQuantityButton;
    private Button submitOrderButton;
    private Button cancelOrderButton;
    private TilePane tablesPane;
    private Label currentTableLabel;

    public WaiterView(WaiterController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        getStyleClass().add("waiter-view");

        initializeLayout();
    }

    private void initializeLayout() {
        setTop(createTablesPanel());
        VBox leftPanel = createLeftPanel();
        VBox centerPanel = createCenterPanel();
        VBox rightPanel = createRightPanel();

        setLeft(leftPanel);
        setCenter(centerPanel);
        setRight(rightPanel);

        BorderPane.setMargin(leftPanel, new Insets(0, 8, 0, 0));
        BorderPane.setMargin(centerPanel, new Insets(0, 8, 0, 8));
        BorderPane.setMargin(rightPanel, new Insets(0, 0, 0, 8));
    }

    private VBox createTablesPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(0, 0, 12, 0));
        Label title = new Label("Tables");
        title.setFont(new Font(16));
        title.getStyleClass().add("panel-title");

        currentTableLabel = new Label("Select a table to start");
        currentTableLabel.setStyle("-fx-font-weight: bold;");

        tablesPane = new TilePane();
        tablesPane.setHgap(10);
        tablesPane.setVgap(10);
        tablesPane.setPrefColumns(3);

        box.getChildren().addAll(title, currentTableLabel, tablesPane);
        return box;
    }

    public void updateTables(List<com.restaurant.pos.domain.model.Table> tables) {
        if (tablesPane == null) return;
        tablesPane.getChildren().clear();
        if (tables == null) return;
        for (com.restaurant.pos.domain.model.Table t : tables) {
            if (t == null) continue;
            Button b = new Button("Table " + t.getNumber());
            b.setPrefWidth(140);
            b.setPrefHeight(60);
            b.getStyleClass().add("table-card");
            if (t.isOccupied()) {
                b.setStyle("-fx-background-color: #ffe0e0; -fx-border-color: #d32f2f;");
            } else {
                b.setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #2e7d32;");
            }
            int number = t.getNumber();
            b.setOnAction(e -> controller.openTable(number));
            tablesPane.getChildren().add(b);
        }
    }

    public void setCurrentTable(int tableNumber) {
        if (currentTableLabel != null) {
            currentTableLabel.setText("Current table: Table " + tableNumber);
        }
    }

    /**
     * LEFT: Menu list showing only available dishes.
     */
    private VBox createLeftPanel() {
        VBox panel = new VBox(8);
        panel.setPrefWidth(250);
        panel.getStyleClass().add("menu-panel");

        Label titleLabel = new Label("Menu");
        titleLabel.setFont(new Font(16));
        titleLabel.getStyleClass().add("panel-title");

        menuListView = new ListView<>();
        menuListView.setPrefHeight(600);
        menuListView.getStyleClass().add("menu-list");
        menuListView.setCellFactory(listView -> new ListCell<DishDTO>() {
            @Override
            protected void updateItem(DishDTO dish, boolean empty) {
                super.updateItem(dish, empty);
                if (empty || dish == null) {
                    setText(null);
                } else {
                    NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
                    String price = dish.getPrice() != null 
                            ? moneyFormat.format(dish.getPrice().doubleValue()) 
                            : "0.00";
                    setText(dish.getName() + " - ₽" + price);
                }
            }
        });

        // Double-click to add dish
        menuListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                DishDTO selected = menuListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleAddDish(selected);
                }
            }
        });

        panel.getChildren().addAll(titleLabel, menuListView);
        return panel;
    }

    /**
     * CENTER: Current order items table.
     */
    @SuppressWarnings("unchecked")
    private VBox createCenterPanel() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("order-panel");

        Label titleLabel = new Label("Current Order");
        titleLabel.setFont(new Font(16));
        titleLabel.getStyleClass().add("panel-title");

        orderItemsTable = new TableView<>();
        orderItemsTable.setPrefHeight(600);
        orderItemsTable.getStyleClass().add("order-items-table");

        // Dish name column
        TableColumn<OrderItemDTO, String> nameCol = new TableColumn<>("Dish");
        nameCol.setCellValueFactory(c -> c.getValue().dishNameProperty());
        nameCol.setPrefWidth(200);

        // Quantity column
        TableColumn<OrderItemDTO, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        quantityCol.setPrefWidth(80);

        // Price per unit column
        TableColumn<OrderItemDTO, String> priceCol = new TableColumn<>("Price per Unit");
        priceCol.setCellValueFactory(c -> {
            NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
            BigDecimal price = c.getValue().getPrice();
            String formatted = price != null ? moneyFormat.format(price.doubleValue()) : "0.00";
            return new javafx.beans.property.SimpleStringProperty(formatted + " ₽");
        });
        priceCol.setPrefWidth(120);

        // Total price per item column
        TableColumn<OrderItemDTO, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(c -> {
            NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
            BigDecimal total = c.getValue().getTotal();
            String formatted = total != null ? moneyFormat.format(total.doubleValue()) : "0.00";
            return new javafx.beans.property.SimpleStringProperty(formatted + " ₽");
        });
        totalCol.setPrefWidth(100);

        orderItemsTable.getColumns().addAll(nameCol, quantityCol, priceCol, totalCol);
        orderItemsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        panel.getChildren().addAll(titleLabel, orderItemsTable);
        VBox.setVgrow(orderItemsTable, Priority.ALWAYS);
        return panel;
    }

    /**
     * RIGHT: Order summary and action buttons.
     */
    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(250);
        panel.getStyleClass().add("summary-panel");

        Label titleLabel = new Label("Order Summary");
        titleLabel.setFont(new Font(16));
        titleLabel.getStyleClass().add("panel-title");

        // Total label
        totalLabel = new Label("Total: ₽ 0.00");
        totalLabel.setFont(new Font(18));
        totalLabel.getStyleClass().add("total-label");

        addDishButton = new Button("Add Dish");
        addDishButton.setPrefWidth(Double.MAX_VALUE);
        addDishButton.getStyleClass().add("action-button");
        addDishButton.setOnAction(e -> {
            DishDTO selected = menuListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleAddDish(selected);
            } else {
                showError("Please select a dish from the menu.");
            }
        });

        increaseQuantityButton = new Button("Increase Quantity");
        increaseQuantityButton.setPrefWidth(Double.MAX_VALUE);
        increaseQuantityButton.getStyleClass().add("action-button");
        increaseQuantityButton.setOnAction(e -> {
            OrderItemDTO selected = orderItemsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleIncreaseQuantity(selected);
            } else {
                showError("Please select an item from the order.");
            }
        });

        decreaseQuantityButton = new Button("Decrease Quantity");
        decreaseQuantityButton.setPrefWidth(Double.MAX_VALUE);
        decreaseQuantityButton.getStyleClass().add("action-button");
        decreaseQuantityButton.setOnAction(e -> {
            OrderItemDTO selected = orderItemsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDecreaseQuantity(selected);
            } else {
                showError("Please select an item from the order.");
            }
        });

        // Spacer
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        submitOrderButton = new Button("Submit Order");
        submitOrderButton.setPrefWidth(Double.MAX_VALUE);
        submitOrderButton.getStyleClass().add("primary-button");
        submitOrderButton.setOnAction(e -> handleSubmitOrder());

        cancelOrderButton = new Button("Cancel Order");
        cancelOrderButton.setPrefWidth(Double.MAX_VALUE);
        cancelOrderButton.getStyleClass().add("secondary-button");
        cancelOrderButton.setOnAction(e -> handleCancelOrder());

        panel.getChildren().addAll(
                titleLabel,
                totalLabel,
                addDishButton,
                increaseQuantityButton,
                decreaseQuantityButton,
                spacer,
                submitOrderButton,
                cancelOrderButton
        );

        return panel;
    }

    // ===================== Public API for controller =====================

    /**
     * Update menu list with available dishes.
     * 
     * @param dishes list of available dishes
     */
    public void updateMenu(List<DishDTO> dishes) {
        menuListView.getItems().setAll(dishes);
    }

    /**
     * Update order display.
     * Sets up automatic total calculation when items change.
     * 
     * @param order order DTO with items and total
     */
    public void updateOrder(OrderDTO order) {
        if (order == null || order.getItems() == null) {
            // Clear previous listeners
            orderItemsTable.getItems().forEach(item -> {
                if (item != null) {
                    item.totalProperty().removeListener(totalChangeListener);
                }
            });
            orderItemsTable.getItems().clear();
            totalLabel.setText("Total: ₽ 0.00");
            submitOrderButton.setDisable(true);
            return;
        }

        // Clear previous listeners
        orderItemsTable.getItems().forEach(item -> {
            if (item != null) {
                item.totalProperty().removeListener(totalChangeListener);
            }
        });

        // Set new items
        orderItemsTable.getItems().setAll(order.getItems());

        // Add listeners to all items for auto-update of total
        orderItemsTable.getItems().forEach(item -> {
            if (item != null) {
                item.totalProperty().addListener(totalChangeListener);
            }
        });

        // Calculate initial total
        updateTotalOrderPrice();

        // Enable submit button if order has items
        submitOrderButton.setDisable(order.getItems().isEmpty());
    }

    /**
     * Change listener for auto-updating total when any item total changes.
     */
    private final javafx.beans.value.ChangeListener<BigDecimal> totalChangeListener = 
            (observable, oldValue, newValue) -> updateTotalOrderPrice();

    /**
     * Auto-update total order price when any item changes.
     */
    private void updateTotalOrderPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDTO item : orderItemsTable.getItems()) {
            if (item != null && item.getTotal() != null) {
                total = total.add(item.getTotal());
            }
        }

        NumberFormat moneyFormat = new DecimalFormat("#,##0.00");
        String totalText = moneyFormat.format(total.doubleValue());
        totalLabel.setText("Total: ₽ " + totalText);
    }

    /**
     * Show error message.
     * 
     * @param message error message
     */
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, javafx.scene.control.ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    /**
     * Show success message.
     * 
     * @param message success message
     */
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, javafx.scene.control.ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Success");
        alert.showAndWait();
    }

    // ===================== Event handlers =====================

    private void handleAddDish(DishDTO dish) {
        if (dish == null || dish.getId() == null) {
            showError("Invalid dish selected.");
            return;
        }

        boolean success = controller.addDish(dish.getId(), 1);
        if (!success) {
            showError("Failed to add dish to order. Make sure an order is open.");
        }
        // Controller should refresh the view via updateOrder()
    }

    private void handleIncreaseQuantity(OrderItemDTO item) {
        if (item == null || item.getDishId() == null) {
            showError("Invalid item selected.");
            return;
        }

        int newQuantity = item.getQuantity() + 1;
        boolean success = controller.changeQuantity(item.getDishId(), newQuantity);
        if (!success) {
            showError("Failed to increase quantity.");
        }
        // Controller should refresh the view via updateOrder()
    }

    private void handleDecreaseQuantity(OrderItemDTO item) {
        if (item == null || item.getDishId() == null) {
            showError("Invalid item selected.");
            return;
        }

        int newQuantity = item.getQuantity() - 1;
        boolean success = controller.changeQuantity(item.getDishId(), newQuantity);
        if (!success) {
            showError("Failed to decrease quantity.");
        }
        // Controller should refresh the view via updateOrder()
    }

    private void handleSubmitOrder() {
        boolean success = controller.submitOrder();
        if (success) {
            showSuccess("Order submitted successfully!");
            // Clear UI - order is already cleared in controller
            updateOrder(null);
        }
        // Errors are already shown by controller via view.showError()
    }

    private void handleCancelOrder() {
        boolean success = controller.cancelOrder();
        if (success) {
            // UI is already cleared by controller via resetUIState()
            // Optionally show confirmation message
        } else {
            // No order to cancel, but UI is already reset
            // Optionally show info message
        }
    }
}

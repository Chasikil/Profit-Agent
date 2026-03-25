package com.restaurant.pos.ui.view;

import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.ui.controller.OrderController;
import com.restaurant.pos.ui.model.DishDTO;
import com.restaurant.pos.ui.model.OrderDTO;
import com.restaurant.pos.ui.model.OrderItemDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * POS-style cashier screen for waiter:
 * LEFT  - menu items list,
 * CENTER- current order items table,
 * BOTTOM- order total and Submit/Cancel buttons.
 *
 * View does not calculate totals; it formats values provided by controller.
 */
public class OrderView extends BorderPane {

    private final OrderController controller;

    private ListView<DishDTO> menuListView;
    private TableView<OrderItemDTO> orderItemsTable;

    private TextField searchField;
    private Label totalLabel;
    private Button cancelButton;

    // Payment
    private ComboBox<PaymentMethod> paymentMethodCombo;
    private TextField amountPaidField;
    private Label changeLabel;
    private Button payAndCloseButton;

    // Shift management UI
    private Label shiftStatusLabel;
    private Button openShiftButton;
    private Button closeShiftButton;

    // Selected waiter display
    private Label waiterNameLabel;
    private Label waiterWarningLabel;

    // Optional UI toggle for grouping orders by waiter
    private CheckBox groupByWaiterCheckBox;

    public OrderView(OrderController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        getStyleClass().add("order-view");

        initializeLayout();
    }

    private void initializeLayout() {
        VBox leftPanel = createLeftPanel();
        VBox centerPanel = createCenterPanel();
        HBox bottomBar = createBottomBar();
        HBox topBar = createTopBar();

        setTop(topBar);
        setLeft(leftPanel);
        setCenter(centerPanel);
        setBottom(bottomBar);

        BorderPane.setMargin(centerPanel, new Insets(0, 0, 8, 8));
        BorderPane.setMargin(leftPanel, new Insets(0, 8, 8, 0));
        BorderPane.setMargin(topBar, new Insets(0, 0, 8, 0));
    }

    /**
     * TOP: shift status, selected waiter and management buttons.
     */
    private HBox createTopBar() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("shift-bar");

        shiftStatusLabel = new Label("Смена: ЗАКРЫТА");
        shiftStatusLabel.setFont(new Font(14));
        shiftStatusLabel.getStyleClass().add("shift-status-label");

        waiterNameLabel = new Label("Официант: не выбран");
        waiterNameLabel.setFont(new Font(14));
        waiterNameLabel.getStyleClass().add("waiter-name-label");

        waiterWarningLabel = new Label("— Выберите официанта для нового заказа");
        waiterWarningLabel.getStyleClass().add("waiter-warning");
        waiterWarningLabel.setVisible(true);

        Button newOrderButton = new Button("Новый заказ");
        newOrderButton.getStyleClass().add("shift-button");
        newOrderButton.setOnAction(e -> controller.handleOpenOrder());

        openShiftButton = new Button("Открыть смену");
        openShiftButton.getStyleClass().add("shift-button");
        openShiftButton.setOnAction(e -> controller.openShift());

        closeShiftButton = new Button("Закрыть смену");
        closeShiftButton.getStyleClass().add("shift-button");
        closeShiftButton.setOnAction(e -> controller.closeShift());
        closeShiftButton.setDisable(true);

        bar.getChildren().addAll(shiftStatusLabel, waiterNameLabel, waiterWarningLabel, newOrderButton, openShiftButton, closeShiftButton);
        return bar;
    }

    /**
     * LEFT: menu items list + optional search field.
     */
    private VBox createLeftPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(8));
        panel.getStyleClass().add("menu-panel");

        Label title = new Label("Меню");
        title.setFont(new Font(16));
        title.getStyleClass().add("panel-title");

        // Optional search field (UI only, filtering can be added later)
        searchField = new TextField();
        searchField.setPromptText("Поиск по меню...");

        menuListView = new ListView<>();
        menuListView.setCellFactory(param -> new DishListCell());
        menuListView.setPlaceholder(new Label("Меню пусто"));
        menuListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        menuListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                DishDTO selected = menuListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    controller.handleAddDish(selected);
                }
            }
        });

        Button addDishButton = new Button("Добавить в заказ");
        addDishButton.getStyleClass().add("primary-button");
        addDishButton.setOnAction(e -> {
            DishDTO selected = menuListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.handleAddDish(selected);
            }
        });

        VBox.setVgrow(menuListView, Priority.ALWAYS);
        panel.getChildren().addAll(title, searchField, menuListView, addDishButton);
        return panel;
    }

    /**
     * CENTER: current order items table.
     */
    @SuppressWarnings("unchecked")
    private VBox createCenterPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(8));
        panel.getStyleClass().add("order-panel");

        Label title = new Label("Текущий заказ");
        title.setFont(new Font(16));
        title.getStyleClass().add("panel-title");

        orderItemsTable = new TableView<>();
        orderItemsTable.setPlaceholder(new Label("Заказ пуст"));

        TableColumn<OrderItemDTO, String> nameCol = new TableColumn<>("Блюдо");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("dishName"));
        nameCol.setPrefWidth(200);

        TableColumn<OrderItemDTO, Integer> qtyCol = new TableColumn<>("Кол-во");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        TableColumn<OrderItemDTO, BigDecimal> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<OrderItemDTO, BigDecimal> totalCol = new TableColumn<>("Сумма");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(120);

        orderItemsTable.getColumns().addAll(nameCol, qtyCol, priceCol, totalCol);
        orderItemsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        VBox.setVgrow(orderItemsTable, Priority.ALWAYS);
        panel.getChildren().addAll(title, orderItemsTable);
        return panel;
    }

    /**
     * BOTTOM: order total, payment section, and actions (Pay & Close / Cancel).
     */
    private HBox createBottomBar() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(8, 8, 8, 8));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("order-bottom-bar");

        totalLabel = new Label("Итого: ₽ 0.00");
        totalLabel.setFont(new Font(18));
        totalLabel.getStyleClass().add("total-label");

        Label methodLabel = new Label("Способ:");
        paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().setAll(PaymentMethod.CASH, PaymentMethod.CARD);
        paymentMethodCombo.setValue(PaymentMethod.CASH);
        paymentMethodCombo.setPrefWidth(120);
        paymentMethodCombo.setOnAction(e -> onPaymentMethodChanged());

        Label amountLabel = new Label("Внесено (₽):");
        amountPaidField = new TextField();
        amountPaidField.setPromptText("0.00");
        amountPaidField.setPrefWidth(100);
        amountPaidField.setDisable(false);

        changeLabel = new Label("Сдача: —");
        changeLabel.getStyleClass().add("change-label");

        groupByWaiterCheckBox = new CheckBox("Группировать по официанту");
        groupByWaiterCheckBox.getStyleClass().add("group-by-waiter-toggle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        payAndCloseButton = new Button("Оплатить и закрыть заказ");
        payAndCloseButton.getStyleClass().add("primary-button");
        payAndCloseButton.setOnAction(e -> onPayAndClose());
        payAndCloseButton.setDisable(true);

        cancelButton = new Button("Отменить");
        cancelButton.setOnAction(e -> controller.cancelOrder());

        bar.getChildren().addAll(totalLabel, methodLabel, paymentMethodCombo, amountLabel, amountPaidField, changeLabel,
                groupByWaiterCheckBox, spacer, cancelButton, payAndCloseButton);
        onPaymentMethodChanged();
        return bar;
    }

    private void onPaymentMethodChanged() {
        boolean cash = paymentMethodCombo.getValue() == PaymentMethod.CASH;
        amountPaidField.setDisable(!cash);
        amountPaidField.clear();
        changeLabel.setText("Сдача: —");
    }

    private void onPayAndClose() {
        PaymentMethod method = paymentMethodCombo.getValue();
        if (method == null) {
            controller.showError("Выберите способ оплаты.");
            return;
        }
        BigDecimal amount = null;
        if (method == PaymentMethod.CASH) {
            String text = amountPaidField.getText();
            if (text != null && !text.isBlank()) {
                try {
                    amount = new BigDecimal(text.trim().replace(',', '.'));
                } catch (NumberFormatException e) {
                    controller.showError("Введите корректную сумму наличными.");
                    return;
                }
            }
        }
        controller.payAndCloseOrder(method, amount);
    }

    // ===================== Public API for controller =====================

    public void updateMenu(List<DishDTO> dishes) {
        menuListView.getItems().setAll(dishes);
    }

    public void updateOrder(OrderDTO order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            orderItemsTable.getItems().clear();
            totalLabel.setText("Итого: ₽ 0.00");
            return;
        }

        orderItemsTable.getItems().setAll(order.getItems());
        NumberFormat format = new DecimalFormat("#,##0.00");
        BigDecimal total = order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO;
        String totalText = "Итого: ₽ " + format.format(total.doubleValue());
        totalLabel.setText(totalText);
    }

    public void setPayAndCloseEnabled(boolean enabled) {
        if (payAndCloseButton != null) {
            payAndCloseButton.setDisable(!enabled);
        }
    }

    /**
     * Update waiter name display in the top bar.
     * View only formats text; controller decides which waiter is selected.
     */
    public void setWaiterName(String waiterName) {
        String text;
        if (waiterName == null || waiterName.isBlank()) {
            text = "Официант: не выбран";
            if (waiterWarningLabel != null) {
                waiterWarningLabel.setVisible(true);
            }
        } else {
            text = "Официант: " + waiterName;
            if (waiterWarningLabel != null) {
                waiterWarningLabel.setVisible(false);
            }
        }
        waiterNameLabel.setText(text);
    }

    /**
     * Returns current state of the "group by waiter" toggle.
     * Grouping logic must be implemented in controller/services.
     */
    public boolean isGroupByWaiterSelected() {
        return groupByWaiterCheckBox != null && groupByWaiterCheckBox.isSelected();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show payment confirmation and clear payment fields.
     */
    public void showPaymentConfirmation(BigDecimal amountPaid, BigDecimal change) {
        NumberFormat format = new DecimalFormat("#,##0.00");
        String amountStr = amountPaid != null ? format.format(amountPaid.doubleValue()) : "0.00";
        String changeStr = change != null ? format.format(change.doubleValue()) : "0.00";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Оплата проведена");
        alert.setHeaderText("Заказ успешно оплачен и закрыт.");
        alert.setContentText("Внесено: ₽ " + amountStr + "\nСдача: ₽ " + changeStr);
        alert.showAndWait();
    }

    public void clearPaymentFields() {
        if (paymentMethodCombo != null) {
            paymentMethodCombo.setValue(PaymentMethod.CASH);
        }
        if (amountPaidField != null) {
            amountPaidField.clear();
        }
        if (changeLabel != null) {
            changeLabel.setText("Сдача: —");
        }
    }

    /**
     * Enable or disable all order buttons based on access.
     * Called by controller when role/shift restrictions change.
     */
    public void setButtonsEnabled(boolean enabled) {
        if (cancelButton != null) {
            cancelButton.setDisable(!enabled);
        }
    }

    /**
     * Show access restriction message (non-blocking).
     */
    public void showAccessRestriction(String message) {
        // Show as info alert, not error
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ограничение доступа");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Update shift status display and button states.
     * @param isOpen true if shift is OPEN, false if CLOSED
     */
    public void updateShiftStatus(boolean isOpen) {
        if (isOpen) {
            shiftStatusLabel.setText("Смена: ОТКРЫТА");
            shiftStatusLabel.getStyleClass().removeAll("shift-closed");
            shiftStatusLabel.getStyleClass().add("shift-open");
            openShiftButton.setDisable(true);
            closeShiftButton.setDisable(false);
        } else {
            shiftStatusLabel.setText("Смена: ЗАКРЫТА");
            shiftStatusLabel.getStyleClass().removeAll("shift-open");
            shiftStatusLabel.getStyleClass().add("shift-closed");
            openShiftButton.setDisable(false);
            closeShiftButton.setDisable(true);
        }
    }

    // ===================== Cells =====================

    private static class DishListCell extends ListCell<DishDTO> {

        @Override
        protected void updateItem(DishDTO dish, boolean empty) {
            super.updateItem(dish, empty);
            if (empty || dish == null) {
                setGraphic(null);
                return;
            }
            HBox cell = new HBox(10);
            cell.setPadding(new Insets(5));
            cell.setAlignment(Pos.CENTER_LEFT);

            VBox info = new VBox(2);
            Label nameLabel = new Label(dish.getName());
            nameLabel.getStyleClass().add("dish-name");
            Label priceLabel = new Label("₽ " + formatPrice(dish.getPrice()));
            priceLabel.getStyleClass().add("dish-price");
            info.getChildren().addAll(nameLabel, priceLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            cell.getChildren().addAll(info, spacer);
            setGraphic(cell);
        }

        private String formatPrice(BigDecimal price) {
            if (price == null) {
                return "0.00";
            }
            NumberFormat format = new DecimalFormat("#,##0.00");
            return format.format(price.doubleValue());
        }
    }
}

package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Menu;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.domain.model.Table;
import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.model.Session;
import com.restaurant.pos.model.AppContext;
import com.restaurant.pos.model.InMemoryOrderStore;
import com.restaurant.pos.model.UiOrder;
import com.restaurant.pos.model.UiOrderItem;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.service.TableService;
import com.restaurant.pos.service.SessionContextService;
import com.restaurant.pos.service.SessionService;
import com.restaurant.pos.ui.context.SessionContext;
import com.restaurant.pos.ui.model.DishDTO;
import com.restaurant.pos.ui.model.OrderDTO;
import com.restaurant.pos.ui.model.OrderItemDTO;
import com.restaurant.pos.ui.view.OrderView;
import com.restaurant.pos.ui.view.TableSelectionDialog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for waiter order operations and in-memory order store.
 * Manages current Order, delegates business logic to services, and updates OrderView.
 */
public class OrderController {

    private final OrderService orderService;
    private final MenuService menuService;
    private final InventoryService inventoryService;
    private final TableService tableService;
    private final SessionService sessionService;
    private final SessionContext sessionContext;
    private final SessionContextService sessionContextService;
    private final InMemoryOrderStore orderStore;
    private final EmployeeStorageService employeeStorageService;
    private final AppContext appContext;

    private OrderView orderView;
    private Order currentOrder;

    public OrderController(OrderService orderService,
                           MenuService menuService,
                           InventoryService inventoryService,
                           TableService tableService,
                           SessionService sessionService,
                           SessionContext sessionContext,
                           SessionContextService sessionContextService,
                           InMemoryOrderStore orderStore,
                           EmployeeStorageService employeeStorageService,
                           AppContext appContext) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.inventoryService = inventoryService;
        this.tableService = tableService;
        this.sessionService = sessionService;
        this.sessionContext = sessionContext;
        this.sessionContextService = sessionContextService;
        this.orderStore = orderStore;
        this.employeeStorageService = employeeStorageService != null ? employeeStorageService : null;
        this.appContext = appContext != null ? appContext : null;
    }

    public OrderView getView() {
        if (orderView == null) {
            // OrderView has overload that can accept InMemoryOrderStore, but controller remains decoupled from JavaFX.
            orderView = new OrderView(this);
        }
        refreshMenu();
        refreshOrder();
        updateUIState();
        return orderView;
    }

    // ============ Public API (as requested) ============

    /** Start new order; requires waiter and table selection. */
    public void startNewOrder() {
        Employee waiter = resolveWaiterForOrder();
        if (waiter == null) {
            return;
        }
        if (tableService == null) {
            showError("Сервис столов недоступен.");
            return;
        }
        List<Table> freeTables = tableService.getFreeTables();
        if (freeTables == null || freeTables.isEmpty()) {
            showError("Нет свободных столов.");
            return;
        }
        Table table = TableSelectionDialog.selectTable(freeTables);
        if (table == null) {
            return;
        }
        currentOrder = orderService.createOrder(waiter);
        if (currentOrder != null) {
            currentOrder.setStatus(OrderStatus.OPEN);
            if (waiter.getId() != null) {
                currentOrder.setWaiterId(waiter.getId());
            }
            currentOrder.setTable(table);
            tableService.markOccupied(table.getNumber());
            Long shiftId = getCurrentShiftId();
            if (shiftId != null) {
                currentOrder.setShiftId(shiftId);
            }
            if (orderView != null) {
                orderView.setWaiterName(waiter.getName());
            }
        }
        refreshOrder();
    }

    /** Add menu item (dish) to current order. */
    public void addItem(DishDTO menuItem) {
        if (menuItem == null) {
            showError("Не выбрано блюдо для добавления.");
            return;
        }

        ensureOrderExists();
        if (currentOrder == null) {
            return;
        }

        Dish dish = findDishById(menuItem.getId());
        if (dish == null) {
            showError("Блюдо недоступно или деактивировано.");
            return;
        }

        if (inventoryService != null) {
            try {
                inventoryService.checkAndWriteOffForDish(dish, 1, "Order #" + currentOrder.getId());
            } catch (RuntimeException e) {
                showError(e.getMessage());
                return;
            }
        }
        orderService.addDish(currentOrder, dish, 1);
        refreshOrder();
    }

    /** Remove selected order item from current order. */
    public void removeItem(OrderItemDTO orderItem) {
        if (orderItem == null) {
            showError("Не выбрана позиция заказа для удаления.");
            return;
        }

        if (currentOrder == null) {
            showError("Нет открытого заказа.");
            return;
        }

        Dish dish = findDishById(orderItem.getDishId());
        if (dish == null) {
            showError("Блюдо недоступно или деактивировано.");
            return;
        }

        if (inventoryService != null) {
            inventoryService.restoreForDish(dish, 1, "Order item removed");
        }
        orderService.removeDish(currentOrder, dish);
        refreshOrder();
    }

    /**
     * Submit current order:
     * - Close order via OrderService (inventory checks/write-off)
     * - Map domain Order to UiOrder
     * - Store UiOrder in in-memory order store for dashboard
     */
    public void submitOrder() {
        if (currentOrder == null) {
            showError("Нет открытого заказа.");
            return;
        }

        OrderService.CloseOrderResult result = orderService.closeOrder(currentOrder);
        if (!result.isSuccess()) {
            showError(result.getErrorMessage());
            return;
        }

        // Map to UiOrder and store in in-memory order store
        if (orderStore != null) {
            UiOrder uiOrder = toUiOrder(currentOrder);
            orderStore.addOrder(uiOrder);
        }

        currentOrder = null;
        if (orderView != null) {
            orderView.setWaiterName(null);
        }
        refreshOrder();

        // Notify dashboard to refresh statistics
        notifyDashboardRefresh();
    }
    
    private DashboardController dashboardController;
    
    /**
     * Set dashboard controller for auto-refresh on order submission.
     * 
     * @param dashboardController dashboard controller
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    private NavigationController navigationController;
    
    /**
     * Set navigation controller for refreshing FinanceView when orders are created.
     * 
     * @param navigationController navigation controller
     */
    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
    
    /**
     * Notify dashboard to refresh statistics.
     */
    private void notifyDashboardRefresh() {
        if (dashboardController != null) {
            dashboardController.refresh();
        }
        // Also refresh FinanceView charts (orders create INCOME operations)
        if (navigationController != null) {
            navigationController.refreshDashboard();
        }
    }

    /**
     * Pay and close current order. Validates waiter, non-empty order, and payment amount.
     * For CASH, amountPaid must be >= order total. After success: confirmation, clear UI.
     */
    public void payAndCloseOrder(PaymentMethod method, BigDecimal amountPaid) {
        if (currentOrder == null) {
            showError("Нет открытого заказа.");
            return;
        }
        if (currentOrder.getWaiter() == null) {
            showError("Выберите официанта перед оплатой.");
            return;
        }
        if (currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            showError("Заказ пуст. Добавьте блюда перед оплатой.");
            return;
        }
        BigDecimal total = toOrderDTO(currentOrder).getTotal();
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        if (method == PaymentMethod.CASH) {
            if (amountPaid == null || amountPaid.compareTo(total) < 0) {
                showError("Недостаточно наличных. Внесённая сумма должна быть не меньше суммы заказа (₽ " + total.setScale(2, RoundingMode.HALF_UP) + ").");
                return;
            }
        } else {
            amountPaid = total;
        }
        try {
            orderService.payOrder(currentOrder, method, amountPaid);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }
        OrderService.CloseOrderResult result = orderService.closeOrder(currentOrder);
        if (!result.isSuccess()) {
            showError(result.getErrorMessage());
            return;
        }
        if (orderStore != null) {
            UiOrder uiOrder = toUiOrder(currentOrder);
            orderStore.addOrder(uiOrder);
        }
        if (currentOrder.getTable() != null && tableService != null) {
            tableService.markFree(currentOrder.getTable().getNumber());
        }
        if (orderView != null) {
            orderView.showPaymentConfirmation(currentOrder.getAmountPaid(), currentOrder.getChange());
            orderView.clearPaymentFields();
        }
        currentOrder = null;
        if (orderView != null) {
            orderView.setWaiterName(null);
        }
        refreshOrder();
        notifyDashboardRefresh();
    }

    /** Cancel current order; restore all ingredients and free table. */
    public void cancelOrder() {
        if (currentOrder != null) {
            if (inventoryService != null) {
                for (OrderItem item : currentOrder.getItems()) {
                    if (item != null && item.getDish() != null && item.getQuantity() > 0) {
                        inventoryService.restoreForDish(item.getDish(), item.getQuantity(), "Order canceled");
                    }
                }
            }
            if (currentOrder.getTable() != null && tableService != null) {
                tableService.markFree(currentOrder.getTable().getNumber());
            }
            orderService.cancelOrder(currentOrder);
        }
        currentOrder = null;
        if (orderView != null) {
            orderView.setWaiterName(null);
        }
        refreshOrder();
    }

    /** Open shift for current employee. */
    public void openShift() {
        if (sessionContextService != null) {
            boolean success = sessionContextService.openShift();
            if (success) {
                updateUIState();
            } else {
                showError("Не удалось открыть смену. Убедитесь, что вы вошли в систему.");
            }
        } else {
            showError("Сервис сессии недоступен.");
        }
    }

    /** Close shift for current employee. */
    public void closeShift() {
        if (sessionContextService != null) {
            boolean success = sessionContextService.closeShift();
            if (success) {
                updateUIState();
            } else {
                showError("Не удалось закрыть смену. Убедитесь, что смена открыта.");
            }
        } else {
            showError("Сервис сессии недоступен.");
        }
    }

    // Backwards-compatible methods for existing OrderView wiring

    public void handleOpenOrder() {
        startNewOrder();
    }

    public void handleAddDish(DishDTO dishDTO) {
        addItem(dishDTO);
    }

    public void handleRemoveDish(OrderItemDTO orderItemDTO) {
        removeItem(orderItemDTO);
    }

    public void handleCloseOrder() {
        submitOrder();
    }

    // ===================== Internal helpers =====================

    private void refreshMenu() {
        if (orderView == null) {
            return;
        }
        Menu activeMenu = menuService.getActiveMenu();
        List<DishDTO> dtos = new ArrayList<>();
        for (Dish dish : activeMenu.getDishes()) {
            if (dish.isActive()) {
                dtos.add(toDishDTO(dish));
            }
        }
        orderView.updateMenu(dtos);
    }

    private void refreshOrder() {
        if (orderView == null) {
            return;
        }
        if (currentOrder == null) {
                orderView.updateOrder(null);
            Employee current = appContext != null ? appContext.getCurrentEmployee() : null;
            if (current != null && current.getRole() == Role.WAITER) {
                orderView.setWaiterName(current.getName());
            } else {
                orderView.setWaiterName(null);
            }
            updateUIState();
            return;
        }
        OrderDTO dto = toOrderDTO(currentOrder);
        orderView.updateOrder(dto);
        if (currentOrder.getWaiter() != null) {
            orderView.setWaiterName(currentOrder.getWaiter().getName());
        }
        updateUIState();
    }

    /**
     * Update UI state based on role and shift restrictions.
     */
    private void updateUIState() {
        if (orderView == null) {
            return;
        }
        boolean hasAccess = checkAccessSilent();
        orderView.setButtonsEnabled(hasAccess);
        boolean canPay = hasAccess && currentOrder != null
                && currentOrder.getItems() != null
                && !currentOrder.getItems().isEmpty();
        orderView.setPayAndCloseEnabled(canPay);
        
        // Update shift status display
        boolean isShiftOpen = false;
        if (sessionContextService != null) {
            isShiftOpen = sessionContextService.isShiftOpen();
        } else if (sessionContext != null) {
            isShiftOpen = sessionContext.getShiftStatus() == SessionContext.ShiftStatus.OPEN;
        }
        orderView.updateShiftStatus(isShiftOpen);
        
        if (!hasAccess) {
            String errorMessage = getAccessErrorMessage();
            if (errorMessage != null && !errorMessage.isEmpty()) {
                orderView.showAccessRestriction(errorMessage);
            }
        }
    }

    private boolean checkAccessSilent() {
        // Allow access when session is waiter with open shift, or when using admin flow (waiter selected via UI context)
        if (sessionContextService != null) {
            com.restaurant.pos.domain.enums.Role role = sessionContextService.getCurrentRole();
            if (role == com.restaurant.pos.domain.enums.Role.WAITER) {
                com.restaurant.pos.domain.enums.ShiftStatus shiftStatus = sessionContextService.getShiftStatus();
                if (shiftStatus == com.restaurant.pos.domain.enums.ShiftStatus.OPEN) {
                    return true;
                }
            }
        }
        if (sessionContext != null) {
            SessionContext.Role role = sessionContext.getRole();
            if (role == SessionContext.Role.WAITER) {
                SessionContext.ShiftStatus shiftStatus = sessionContext.getShiftStatus();
                if (shiftStatus == SessionContext.ShiftStatus.OPEN) {
                    return true;
                }
            }
        }
        // Admin/cashier flow: no session required; waiter is selected via dialog
        return true;
    }

    private String getAccessErrorMessage() {
        if (sessionContextService != null) {
            com.restaurant.pos.domain.enums.Role role = sessionContextService.getCurrentRole();
            if (role != com.restaurant.pos.domain.enums.Role.WAITER) {
                return "Только официант может работать с заказами.";
            }
            com.restaurant.pos.domain.enums.ShiftStatus shiftStatus = sessionContextService.getShiftStatus();
            if (shiftStatus != com.restaurant.pos.domain.enums.ShiftStatus.OPEN) {
                return "Смена должна быть открыта для работы с заказами.";
            }
        } else if (sessionContext != null) {
            SessionContext.Role role = sessionContext.getRole();
            if (role != SessionContext.Role.WAITER) {
                return "Только официант может работать с заказами.";
            }
            SessionContext.ShiftStatus shiftStatus = sessionContext.getShiftStatus();
            if (shiftStatus != SessionContext.ShiftStatus.OPEN) {
                return "Смена должна быть открыта для работы с заказами.";
            }
        }
        return "Сессия не активна. Выполните вход и откройте смену.";
    }

    private void ensureOrderExists() {
        if (currentOrder != null && currentOrder.getStatus() == OrderStatus.OPEN) {
            return;
        }
        Employee waiter = resolveWaiterForOrder();
        if (waiter == null) {
            return;
        }
        if (tableService == null) {
            showError("Сервис столов недоступен.");
            return;
        }
        List<Table> freeTables = tableService.getFreeTables();
        if (freeTables == null || freeTables.isEmpty()) {
            showError("Нет свободных столов.");
            return;
        }
        Table table = TableSelectionDialog.selectTable(freeTables);
        if (table == null) {
            return;
        }
        currentOrder = orderService.createOrder(waiter);
        if (currentOrder != null) {
            currentOrder.setStatus(OrderStatus.OPEN);
            if (waiter.getId() != null) {
                currentOrder.setWaiterId(waiter.getId());
            }
            currentOrder.setTable(table);
            tableService.markOccupied(table.getNumber());
            Long shiftId = getCurrentShiftId();
            if (shiftId != null) {
                currentOrder.setShiftId(shiftId);
            }
            if (orderView != null) {
                orderView.setWaiterName(waiter.getName());
            }
        }
    }

    /**
     * Resolve waiter for new order: use AppContext.currentEmployee if set and valid,
     * else session employee. No waiter selection dialog.
     */
    private Employee resolveWaiterForOrder() {
        Employee waiter = getCurrentEmployee();
        if (waiter != null && waiter.getId() != null) {
            return waiter;
        }
        showError("Текущий официант не определён. Выполните вход.");
        return null;
    }

    private Dish findDishById(Long dishId) {
        if (dishId == null) {
            return null;
        }
        Menu activeMenu = menuService.getActiveMenu();
        Optional<Dish> dishOpt = activeMenu.getDishes().stream()
                .filter(d -> d.getId() != null && d.getId().equals(dishId) && d.isActive())
                .findFirst();
        return dishOpt.orElse(null);
    }

    private Employee getCurrentEmployee() {
        if (appContext != null) {
            Employee current = appContext.getCurrentEmployee();
            if (current != null && current.getId() != null && current.isActive()
                    && current.getRole() == Role.WAITER) {
                return current;
            }
        }
        Session session = sessionService != null ? sessionService.getCurrentSession() : null;
        return session != null ? session.getEmployee() : null;
    }

    /**
     * Get current shift ID from session.
     * @return shift ID or null if no shift is open
     */
    private Long getCurrentShiftId() {
        if (sessionService != null && sessionService.isSessionActive()) {
            Session session = sessionService.getCurrentSession();
            if (session != null && session.getShift() != null) {
                return session.getShift().getId();
            }
        }
        return null;
    }

    /** Show error dialog; callable from view. */
    public void showError(String message) {
        if (orderView != null) {
            orderView.showError(message);
        }
    }

    private DishDTO toDishDTO(Dish dish) {
        DishDTO dto = new DishDTO();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setPrice(dish.getSalePrice());
        return dto;
    }

    private OrderDTO toOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getDish() == null) {
                continue;
            }
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setDishId(item.getDish().getId());
            itemDTO.setDishName(item.getDish().getName());
            itemDTO.setQuantity(item.getQuantity());
            BigDecimal price = item.getDish().getSalePrice() != null
                    ? item.getDish().getSalePrice()
                    : BigDecimal.ZERO;
            itemDTO.setPrice(price);
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            itemDTO.setTotal(itemTotal);
            total = total.add(itemTotal);
            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);
        dto.setTotal(total);
        return dto;
    }

    private UiOrder toUiOrder(Order order) {
        UiOrder uiOrder = new UiOrder();
        uiOrder.setId(order.getId());
        LocalDateTime createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();
        uiOrder.setCreatedAt(createdAt);

        // Link order to current shift if available
        Session session = sessionService != null ? sessionService.getCurrentSession() : null;
        if (session != null && session.getShift() != null) {
            uiOrder.setShiftId(session.getShift().getId());
        }

        List<UiOrderItem> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            if (item.getDish() == null) {
                continue;
            }
            UiOrderItem uiItem = new UiOrderItem();
            uiItem.setMenuItemName(item.getDish().getName());
            uiItem.setQuantity(item.getQuantity());
            BigDecimal price = item.getDish().getSalePrice() != null
                    ? item.getDish().getSalePrice()
                    : BigDecimal.ZERO;
            uiItem.setPrice(price);
            items.add(uiItem);
        }
        uiOrder.setItems(items);
        return uiOrder;
    }
}

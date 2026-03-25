package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Menu;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.domain.model.Table;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.TableService;
import com.restaurant.pos.service.SessionService;
import com.restaurant.pos.ui.view.WaiterView;
import com.restaurant.pos.ui.model.DishDTO;
import com.restaurant.pos.ui.model.OrderDTO;
import com.restaurant.pos.ui.model.OrderItemDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Controller for waiter order operations.
 * Handles order creation, dish management, and order submission.
 */
public class WaiterController {

    private final OrderService orderService;
    private final MenuService menuService;
    private final InventoryService inventoryService;
    private final OrderPricingService orderPricingService;
    private final TableService tableService;
    private final EmployeeStorageService employeeStorageService;
    private final SessionService sessionService;

    private Order currentOrder;
    private WaiterView view;

    public WaiterController(OrderService orderService,
                            MenuService menuService,
                            InventoryService inventoryService,
                            OrderPricingService orderPricingService,
                            TableService tableService,
                            EmployeeStorageService employeeStorageService,
                            SessionService sessionService) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.inventoryService = inventoryService;
        this.orderPricingService = orderPricingService;
        this.tableService = tableService;
        this.employeeStorageService = employeeStorageService;
        this.sessionService = sessionService;
    }

    /**
     * Set the view for error display.
     * 
     * @param view waiter view
     */
    public void setView(WaiterView view) {
        this.view = view;
        refreshTables();
        refreshMenu();
    }

    public void refreshTables() {
        if (view == null || tableService == null) return;
        view.updateTables(tableService.getAllTables());
    }

    public void openTable(int tableNumber) {
        if (tableService == null) {
            showError("Сервис столов недоступен.");
            return;
        }
        Employee waiter = getCurrentWaiter();
        if (waiter == null || waiter.getId() == null) {
            showError("Текущий официант не определён. Выполните вход.");
            return;
        }

        Table table = tableService.getAllTables().stream()
                .filter(t -> t != null && t.getNumber() == tableNumber)
                .findFirst()
                .orElse(null);
        if (table == null) {
            showError("Стол не найден.");
            return;
        }

        if (table.isOccupied()) {
            // Try to load existing OPEN order for this table
            Order existing = findOpenOrderByTable(tableNumber);
            if (existing != null) {
                currentOrder = existing;
                if (view != null) {
                    view.setCurrentTable(tableNumber);
                    view.updateOrder(toOrderDTO(currentOrder));
                }
                refreshMenu();
                return;
            }
        }

        // No order exists: create new OPEN order
        currentOrder = orderService.createOrder(waiter);
        if (currentOrder != null) {
            currentOrder.setStatus(OrderStatus.OPEN);
            currentOrder.setWaiterId(waiter.getId());
            currentOrder.setTable(table);
            tableService.markOccupied(tableNumber);
            // persist table assignment
            orderService.saveOrder(currentOrder);
            refreshTables();
            if (view != null) {
                view.setCurrentTable(tableNumber);
                view.updateOrder(toOrderDTO(currentOrder));
            }
            refreshMenu();
        }
    }

    private Employee getCurrentWaiter() {
        if (sessionService != null && sessionService.isSessionActive()) {
            com.restaurant.pos.domain.model.Session s = sessionService.getCurrentSession();
            if (s != null && s.getEmployee() != null) {
                return s.getEmployee();
            }
        }
        return null;
    }

    private int getCurrentQuantity(Long dishId) {
        if (currentOrder == null || dishId == null) return 0;
        return currentOrder.getItems().stream()
                .filter(item -> item.getDish() != null && dishId.equals(item.getDish().getId()))
                .findFirst()
                .map(OrderItem::getQuantity)
                .orElse(0);
    }

    // createNewOrder() removed: table selection is the first step (POS flow) via openTable()

    /**
     * Add dish to current order.
     * 
     * @param dishId dish ID to add
     * @param quantity quantity to add (default 1)
     * @return true if added successfully
     */
    public boolean addDish(Long dishId, int quantity) {
        if (dishId == null || quantity <= 0) {
            return false;
        }

        if (currentOrder == null) {
            return false;
        }

        Dish dish = findDishById(dishId);
        if (dish == null || !dish.isActive()) {
            return false;
        }

        if (inventoryService != null && !inventoryService.hasIngredientsForDish(dish, quantity)) {
            if (view != null) view.showError("Недостаточно ингредиентов на складе");
            return false;
        }
        orderService.addDish(currentOrder, dish, quantity);
        return true;
    }

    /**
     * Change quantity of dish in current order.
     * 
     * @param dishId dish ID
     * @param newQuantity new quantity (if 0 or negative, removes dish)
     * @return true if changed successfully
     */
    public boolean changeQuantity(Long dishId, int newQuantity) {
        if (dishId == null || currentOrder == null) {
            return false;
        }

        Dish dish = findDishById(dishId);
        if (dish == null) {
            return false;
        }

        if (newQuantity <= 0) {
            int toRestore = getCurrentQuantity(dishId);
            if (inventoryService != null && toRestore > 0) {
                inventoryService.restoreForDish(dish, toRestore, "Order item removed");
            }
            orderService.removeDish(currentOrder, dish);
            return true;
        }

        // Find existing order item
        Optional<OrderItem> existingItem = currentOrder.getItems().stream()
                .filter(item -> item.getDish() != null 
                        && item.getDish().getId() != null 
                        && item.getDish().getId().equals(dishId))
                .findFirst();

        if (existingItem.isPresent()) {
            int oldQty = existingItem.get().getQuantity();
            if (newQuantity > oldQty) {
                int addQty = newQuantity - oldQty;
                if (inventoryService != null && !inventoryService.hasIngredientsForDish(dish, addQty)) {
                    if (view != null) view.showError("Недостаточно ингредиентов на складе");
                    return false;
                }
            } else if (newQuantity < oldQty) {
                // no inventory restore here; write-off happens on submit
            }
            orderService.removeDish(currentOrder, dish);
            orderService.addDish(currentOrder, dish, newQuantity);
            return true;
        } else {
            if (inventoryService != null && !inventoryService.hasIngredientsForDish(dish, newQuantity)) {
                if (view != null) view.showError("Недостаточно ингредиентов на складе");
                return false;
            }
            orderService.addDish(currentOrder, dish, newQuantity);
            return true;
        }
    }

    /**
     * Submit current order.
     * Flow:
     * 1. Validate order not empty
     * 2. Check inventory availability (via OrderService.closeOrder)
     * 3. Write off inventory (via OrderService.closeOrder)
     * 4. Save order (via OrderService.closeOrder - order is already saved)
     * 5. Add INCOME finance operation (via OrderService.closeOrder)
     * 6. Clear UI
     * 
     * Handles errors with alert.
     * 
     * @return true if submitted successfully, false otherwise
     */
    public boolean submitOrder() {
        // 1. Validate order not empty
        if (currentOrder == null) {
            showError("No order to submit. Please create an order first.");
            return false;
        }

        if (currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            showError("Cannot submit empty order. Please add items to the order.");
            return false;
        }

        // Pre-check stock for the whole order before payment
        if (inventoryService != null && !inventoryService.hasIngredientsForOrder(currentOrder)) {
            showError("Недостаточно ингредиентов на складе");
            return false;
        }

        // Pay order (CARD) then close (close will write off inventory)
        BigDecimal total = orderPricingService != null ? orderPricingService.calculateTotal(currentOrder) : BigDecimal.ZERO;
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        try {
            orderService.payOrder(currentOrder, PaymentMethod.CARD, total);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return false;
        }
        OrderService.CloseOrderResult result = orderService.closeOrder(currentOrder);
        if (!result.isSuccess()) {
            String errorMessage = result.getErrorMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Failed to submit order. Please try again.";
            }
            showError(errorMessage);
            return false;
        }

        // Order is closed/paid, keep table occupied (restaurant flow). If you want auto-free, move to payment/settlement screen.
        currentOrder = null;

        // Notify dashboard to refresh statistics
        notifyDashboardRefresh();
        refreshTables();
        
        return true;
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
     * Show error message via view.
     * 
     * @param message error message
     */
    private void showError(String message) {
        if (view != null) {
            view.showError(message);
        }
    }

    /**
     * Cancel current order without submitting.
     * Logic:
     * - Clear current order
     * - Reset UI state
     * - No inventory changes
     * 
     * @return true if canceled successfully
     */
    public boolean cancelOrder() {
        if (currentOrder == null) {
            resetUIState();
            return false;
        }

        // No inventory restore here; write-off happens only on submit/close
        if (currentOrder.getTable() != null && tableService != null) {
            tableService.markFree(currentOrder.getTable().getNumber());
            refreshTables();
        }
        orderService.cancelOrder(currentOrder);
        currentOrder = null;
        resetUIState();
        return true;
    }

    private Order findOpenOrderByTable(int tableNumber) {
        if (orderService == null) return null;
        for (Order o : orderService.getAllOrders()) {
            if (o == null) continue;
            if (o.getStatus() != OrderStatus.OPEN) continue;
            if (o.getTable() != null && o.getTable().getNumber() == tableNumber) {
                return o;
            }
        }
        return null;
    }

    private void refreshMenu() {
        if (view == null || menuService == null) return;
        Menu activeMenu = menuService.getActiveMenu();
        if (activeMenu == null || activeMenu.getDishes() == null) return;
        List<DishDTO> dtos = activeMenu.getDishes().stream()
                .filter(d -> d != null && d.isActive())
                .map(this::toDishDTO)
                .toList();
        view.updateMenu(dtos);
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
        List<OrderItemDTO> itemDTOs = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item == null || item.getDish() == null) continue;
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setDishId(item.getDish().getId());
                itemDTO.setDishName(item.getDish().getName());
                itemDTO.setQuantity(item.getQuantity());
                BigDecimal price = item.getDish().getSalePrice() != null ? item.getDish().getSalePrice() : BigDecimal.ZERO;
                itemDTO.setPrice(price);
                itemDTO.setTotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
                itemDTOs.add(itemDTO);
                total = total.add(itemDTO.getTotal());
            }
        }
        dto.setItems(itemDTOs);
        dto.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        return dto;
    }

    /**
     * Reset UI state after order cancellation or clearing.
     */
    private void resetUIState() {
        if (view != null) {
            // Clear order display
            view.updateOrder(null);
        }
    }

    /**
     * Get current order.
     * 
     * @return current order or null if no order is open
     */
    public Order getCurrentOrder() {
        return currentOrder;
    }

    // ===================== Internal helpers =====================

    /**
     * Find dish by ID from active menu.
     * 
     * @param dishId dish ID
     * @return dish or null if not found
     */
    private Dish findDishById(Long dishId) {
        if (dishId == null || menuService == null) {
            return null;
        }
        Menu activeMenu = menuService.getActiveMenu();
        if (activeMenu == null || activeMenu.getDishes() == null) {
            return null;
        }
        return activeMenu.getDishes().stream()
                .filter(d -> d.getId() != null && d.getId().equals(dishId))
                .findFirst()
                .orElse(null);
    }
}

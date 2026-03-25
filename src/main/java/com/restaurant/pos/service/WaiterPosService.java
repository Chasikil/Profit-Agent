package com.restaurant.pos.service;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.*;
import com.restaurant.pos.db.ReceiptRepository;
import com.restaurant.pos.db.TableRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Waiter POS workflow service: active orders by table, stock check and write-off, receipt creation.
 */
public class WaiterPosService {

    private final Map<Integer, Order> activeOrders = new HashMap<>();

    private final OrderService orderService;
    private final MenuService menuService;
    private final InventoryService inventoryService;
    private final OrderPricingService orderPricingService;
    private final TableService tableService;
    private final SessionService sessionService;
    private final TableRepository tableRepository;
    private final ReceiptRepository receiptRepository;

    public WaiterPosService(OrderService orderService,
                            MenuService menuService,
                            InventoryService inventoryService,
                            OrderPricingService orderPricingService,
                            TableService tableService,
                            SessionService sessionService,
                            TableRepository tableRepository,
                            ReceiptRepository receiptRepository) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.inventoryService = inventoryService;
        this.orderPricingService = orderPricingService;
        this.tableService = tableService;
        this.sessionService = sessionService;
        this.tableRepository = tableRepository;
        this.receiptRepository = receiptRepository;
    }

    public List<Table> getTables() {
        if (tableService != null) {
            tableService.ensureMinimumTables(5);
        }
        return tableService != null ? tableService.getAllTables() : Collections.emptyList();
    }

    public boolean hasActiveOrder(int tableNumber) {
        if (tableNumber <= 0) {
            return false;
        }
        // Стол считается занятым, только если в текущей сессии
        // в карте activeOrders есть открытый заказ для этого стола.
        return activeOrders.containsKey(tableNumber);
    }

    public List<Dish> getMenuDishes() {
        if (menuService == null) return Collections.emptyList();
        Menu m = menuService.getActiveMenu();
        if (m == null || m.getDishes() == null) return Collections.emptyList();
        List<Dish> out = new ArrayList<>();
        for (Dish d : m.getDishes()) {
            if (d != null && d.isActive()) out.add(d);
        }
        return out;
    }

    public Order openTable(int tableNumber) {
        // existing in-memory
        Order cached = activeOrders.get(tableNumber);
        if (cached != null && cached.getStatus() == OrderStatus.OPEN) {
            return cached;
        }

        // load from DB/orders if exists
        Order existing = findOpenOrderByTable(tableNumber);
        if (existing != null) {
            activeOrders.put(tableNumber, existing);
            return existing;
        }

        Employee waiter = getCurrentWaiter();
        if (waiter == null || waiter.getId() == null) {
            throw new IllegalStateException("Текущий официант не определён. Выполните вход.");
        }
        Table table = findTable(tableNumber);
        if (table == null) {
            table = new Table(tableNumber);
        }
        // mark occupied in memory + db
        if (tableService != null) tableService.markOccupied(tableNumber);
        if (tableRepository != null) tableRepository.updateOccupied(tableNumber, true);

        Order order = orderService.createOrder(waiter);
        order.setStatus(OrderStatus.OPEN);
        order.setWaiterId(waiter.getId());
        order.setTable(table);
        orderService.saveOrder(order);
        activeOrders.put(tableNumber, order);
        return order;
    }

    public boolean addDish(int tableNumber, Dish dish) {
        if (dish == null || dish.getId() == null) return false;
        Order order = openTable(tableNumber);
        if (inventoryService != null && !inventoryService.hasIngredientsForDish(dish, 1)) {
            return false;
        }
        orderService.addDish(order, dish, 1);
        orderService.saveOrder(order);
        return true;
    }

    public Order getOrder(int tableNumber) {
        return activeOrders.get(tableNumber);
    }

    public OrderService.CloseOrderResult closeOrder(int tableNumber) {
        Order order = activeOrders.get(tableNumber);
        if (order == null) {
            return OrderService.CloseOrderResult.failure("Заказ не найден.");
        }
        if (inventoryService != null && !inventoryService.hasIngredientsForOrder(order)) {
            return OrderService.CloseOrderResult.failure("Недостаточно ингредиентов на складе");
        }

        BigDecimal total = orderPricingService != null ? orderPricingService.calculateTotal(order) : BigDecimal.ZERO;
        if (total == null) total = BigDecimal.ZERO;
        try {
            orderService.payOrder(order, PaymentMethod.CARD, total);
        } catch (IllegalArgumentException e) {
            return OrderService.CloseOrderResult.failure(e.getMessage());
        }

        OrderService.CloseOrderResult result = orderService.closeOrder(order);
        if (!result.isSuccess()) {
            return result;
        }

        // create and persist receipt
        saveReceipt(order, total);

        // free table
        if (tableService != null) tableService.markFree(tableNumber);
        if (tableRepository != null) tableRepository.updateOccupied(tableNumber, false);

        activeOrders.remove(tableNumber);
        return result;
    }

    private void saveReceipt(Order order, BigDecimal total) {
        if (receiptRepository == null || order == null) return;
        Receipt r = new Receipt();
        r.setTableNumber(order.getTable() != null ? order.getTable().getNumber() : 0);
        Employee w = order.getWaiter();
        String waiterName = w != null ? w.getName() : (order.getWaiterId() != null ? ("Waiter #" + order.getWaiterId()) : "Waiter");
        r.setWaiterName(waiterName);
        r.setTime(LocalDateTime.now());
        r.setTotal(total);
        List<ReceiptItem> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem oi : order.getItems()) {
                if (oi == null || oi.getDish() == null) continue;
                BigDecimal price = oi.getDish().getSalePrice() != null ? oi.getDish().getSalePrice() : BigDecimal.ZERO;
                items.add(new ReceiptItem(oi.getDish().getName(), oi.getQuantity(), price));
            }
        }
        r.setItems(items);
        receiptRepository.save(r);
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

    private Table findTable(int tableNumber) {
        if (tableService == null) return null;
        for (Table t : tableService.getAllTables()) {
            if (t != null && t.getNumber() == tableNumber) return t;
        }
        return null;
    }

    private Employee getCurrentWaiter() {
        if (sessionService != null && sessionService.isSessionActive()) {
            com.restaurant.pos.domain.model.Session s = sessionService.getCurrentSession();
            if (s != null) return s.getEmployee();
        }
        return null;
    }
}


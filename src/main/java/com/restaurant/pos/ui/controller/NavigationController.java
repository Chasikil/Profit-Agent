package com.restaurant.pos.ui.controller;

import com.restaurant.pos.model.AppContext;
import com.restaurant.pos.model.UserRole;
import com.restaurant.pos.ui.components.Sidebar;
import com.restaurant.pos.ui.context.SessionContext;
import com.restaurant.pos.ui.view.DashboardView;
import com.restaurant.pos.ui.view.EmployeesView;
import com.restaurant.pos.ui.view.MainWindow;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import com.restaurant.pos.service.WaiterPosService;

public class NavigationController {

    private final AppContext appContext;

    private MainWindow mainWindow;
    private DashboardController dashboardController;
    private DirectorDashboardController directorDashboardController;
    private OrderController orderController;
    private WaiterController waiterController;
    private MenuController menuController;
    private SalaryController salaryController;
    private InventoryController inventoryController;
    private com.restaurant.pos.ui.controller.AnalyticsController analyticsController;
    private com.restaurant.pos.ui.controller.OrderHistoryController orderHistoryController;
    private SessionContext sessionContext;
    private WaiterPosService waiterPosService;

    // Simple UI-only views
    private EmployeesView employeesView;

    // Services for views
    private com.restaurant.pos.service.AnalyticsService analyticsService;
    
    /**
     * Refresh Dashboard data and charts after order is closed.
     */
    public void refreshDashboard() {
        if (dashboardController != null) {
            dashboardController.refresh();
        }
    }

    public NavigationController(AppContext appContext) {
        this.appContext = appContext;
    }

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        if (sessionContext != null && mainWindow != null) {
            mainWindow.getSidebar().setSessionContext(sessionContext);
        }
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        if (mainWindow != null && sessionContext != null) {
            mainWindow.getSidebar().setSessionContext(sessionContext);
        }
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    public void setWaiterController(WaiterController waiterController) {
        this.waiterController = waiterController;
    }

    public void setWaiterPosService(WaiterPosService waiterPosService) {
        this.waiterPosService = waiterPosService;
    }

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    public void setSalaryController(SalaryController salaryController) {
        this.salaryController = salaryController;
    }

    public void setInventoryController(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }

    public void setAnalyticsController(com.restaurant.pos.ui.controller.AnalyticsController analyticsController) {
        this.analyticsController = analyticsController;
    }

    public void setOrderHistoryController(com.restaurant.pos.ui.controller.OrderHistoryController orderHistoryController) {
        this.orderHistoryController = orderHistoryController;
    }

    public void setDirectorDashboardController(DirectorDashboardController directorDashboardController) {
        this.directorDashboardController = directorDashboardController;
    }

    public void onSidebarSectionSelected(Sidebar.Section section) {
        if (mainWindow == null) {
            return;
        }

        UserRole role = appContext != null ? appContext.getCurrentRole() : UserRole.ADMIN;

        // WAITER: always route to WaiterView (Orders). Other sections are hidden in Sidebar.
        if (role == UserRole.WAITER) {
            showWaiterView();
            return;
        }

        // ADMIN (and others) can access all views
        switch (section) {
            case DASHBOARD -> showDashboard();
            case ORDERS -> showOrders();
            case MENU -> showMenu();
            case INVENTORY -> showInventory();
            case EMPLOYEES -> showEmployees();
            case ORDER_HISTORY -> showOrderHistory();
            default -> showDashboard();
        }
    }

    public void showDashboard() {
        if (mainWindow == null) {
            return;
        }
        // Block WAITER access
        if (!checkAdminAccess()) {
            return;
        }
        // Show DirectorDashboardView for DIRECTOR role
        if (sessionContext != null && sessionContext.getRole() == SessionContext.Role.DIRECTOR) {
            if (directorDashboardController != null) {
                mainWindow.setHeaderTitle("Финансовая панель директора");
                mainWindow.showView(directorDashboardController.getView());
            } else {
                showPlaceholder("Director Dashboard");
            }
        } else {
            // Show regular DashboardView for other roles
            if (dashboardController != null) {
                DashboardView dashboardView = dashboardController.getView();
                mainWindow.setHeaderTitle("Dashboard");
                mainWindow.showView(dashboardView);
            } else {
                DashboardView dashboardView = new DashboardView();
                mainWindow.setHeaderTitle("Dashboard");
                mainWindow.showView(dashboardView);
            }
        }
    }

    public void showOrders() {
        if (mainWindow == null) {
            return;
        }
        // ADMIN uses OrderController, WAITER uses WaiterController
        UserRole role = appContext != null ? appContext.getCurrentRole() : UserRole.ADMIN;
        if (role == UserRole.WAITER) {
            showWaiterView();
        } else {
            // ADMIN uses OrderController
            if (orderController != null) {
                mainWindow.setHeaderTitle("Заказы");
                mainWindow.showView(orderController.getView());
            } else {
                showPlaceholder("Orders");
            }
        }
    }

    /**
     * Show WaiterView for WAITER role.
     */
    public void showWaiterView() {
        if (mainWindow == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TablesView.fxml"));
            Parent root = loader.load();
            TablesController controller = loader.getController();
            controller.setWaiterPosService(waiterPosService);
            controller.setNavigationController(this);
            controller.loadTables();
            mainWindow.setHeaderTitle("Tables");
            mainWindow.showView(root);
        } catch (Exception e) {
            showPlaceholder("Tables");
        }
    }

    public void showWaiterNode(Parent node, String headerTitle) {
        if (mainWindow == null || node == null) return;
        mainWindow.setHeaderTitle(headerTitle != null ? headerTitle : "Orders");
        mainWindow.showView(node);
    }

    public void showMenu() {
        if (mainWindow == null) {
            return;
        }
        // Block WAITER access
        if (!checkAdminAccess()) {
            return;
        }
        if (menuController != null) {
            mainWindow.setHeaderTitle("Меню");
            mainWindow.showView(menuController.getView());
        } else {
            showPlaceholder("Menu");
        }
    }

    public void showInventory() {
        if (mainWindow == null) {
            return;
        }
        if (!checkAdminAccess()) {
            return;
        }
        if (inventoryController != null) {
            mainWindow.setHeaderTitle("Склад");
            mainWindow.showView(inventoryController.getView());
        } else {
            showPlaceholder("Inventory");
        }
    }

    public void showEmployees() {
        if (mainWindow == null) {
            return;
        }
        // Block WAITER access
        if (!checkAdminAccess()) {
            return;
        }
        if (employeesView == null) {
            employeesView = new EmployeesView();
        }
        mainWindow.setHeaderTitle("Employees");
        mainWindow.showView(employeesView);
    }

    public void showOrderHistory() {
        if (mainWindow == null) return;
        if (!checkAdminAccess()) return;
        if (orderHistoryController != null) {
            mainWindow.setHeaderTitle("Order History");
            mainWindow.showView(new com.restaurant.pos.ui.view.OrderHistoryView(orderHistoryController));
        } else {
            showPlaceholder("Order History");
        }
    }
    
    public void setAnalyticsService(com.restaurant.pos.service.AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Check if current user has ADMIN access.
     * 
     * @return true if ADMIN, false if WAITER
     */
    private boolean checkAdminAccess() {
        UserRole role = appContext != null ? appContext.getCurrentRole() : UserRole.ADMIN;
        // For non-admin roles simply block access silently; Sidebar already hides admin sections for WAITER.
        return role == UserRole.ADMIN;
    }

    /**
     * Show access denied message.
     * 
     * @param title alert title
     * @param message alert message
     */
    private void showAccessDenied(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void showPlaceholder(String title) {
        mainWindow.setHeaderTitle(title);
        StackPane placeholder = new StackPane();
        placeholder.getStyleClass().add("placeholder-view");
        Label label = new Label(title + " view (UI mock)");
        placeholder.getChildren().add(label);
        mainWindow.showView(placeholder);
    }
}


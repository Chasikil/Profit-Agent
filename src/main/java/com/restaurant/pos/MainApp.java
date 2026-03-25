package com.restaurant.pos;

import com.restaurant.pos.service.AnalyticsService;
import com.restaurant.pos.service.AuthService;
import com.restaurant.pos.service.CostCalculationService;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.FinanceReportService;
import com.restaurant.pos.service.FinanceService;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.KPIService;
import com.restaurant.pos.service.DashboardService;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.NetProfitService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.service.ReportService;
import com.restaurant.pos.service.SalaryReportService;
import com.restaurant.pos.service.SalaryService;
import com.restaurant.pos.service.SessionContextService;
import com.restaurant.pos.service.SessionService;
import com.restaurant.pos.service.ShiftService;
import com.restaurant.pos.service.TableService;
import com.restaurant.pos.service.TechCardService;
import com.restaurant.pos.service.impl.InMemoryAuthService;
import com.restaurant.pos.service.impl.InMemoryEmployeeStorageService;
import com.restaurant.pos.service.impl.InMemoryFinanceReportService;
import com.restaurant.pos.service.impl.InMemoryFinanceService;
import com.restaurant.pos.service.impl.InMemoryInventoryService;
import com.restaurant.pos.service.impl.InMemoryMenuService;
import com.restaurant.pos.service.impl.InMemoryOrderService;
import com.restaurant.pos.service.impl.InMemoryReportService;
import com.restaurant.pos.service.impl.InMemorySalaryReportService;
import com.restaurant.pos.service.impl.InMemorySalaryService;
import com.restaurant.pos.service.impl.InMemorySessionService;
import com.restaurant.pos.service.impl.InMemoryShiftService;
import com.restaurant.pos.service.impl.InMemoryTableService;
import com.restaurant.pos.service.impl.InMemoryTechCardService;
import com.restaurant.pos.service.impl.SessionContextServiceImpl;
import com.restaurant.pos.service.impl.SimpleCostCalculationService;
import com.restaurant.pos.service.impl.SimpleDirectorDashboardService;
import com.restaurant.pos.service.impl.SimpleKPIService;
import com.restaurant.pos.service.impl.InMemoryDashboardService;
import com.restaurant.pos.service.impl.SimpleNetProfitService;
import com.restaurant.pos.service.impl.SimpleOrderPricingService;
import com.restaurant.pos.ui.context.SessionContext;
import com.restaurant.pos.model.AppContext;
import com.restaurant.pos.model.InMemoryOrderStore;
import com.restaurant.pos.ui.controller.AnalyticsController;
import com.restaurant.pos.ui.controller.OrderHistoryController;
import com.restaurant.pos.ui.controller.DashboardController;
import com.restaurant.pos.ui.controller.DirectorDashboardController;
import com.restaurant.pos.ui.controller.MenuController;
import com.restaurant.pos.ui.controller.NavigationController;
import com.restaurant.pos.ui.controller.OrderController;
import com.restaurant.pos.ui.controller.SalaryController;
import com.restaurant.pos.ui.controller.InventoryController;
import com.restaurant.pos.ui.controller.LoginController;
import com.restaurant.pos.ui.controller.WaiterController;
import com.restaurant.pos.ui.view.LoginView;
import com.restaurant.pos.ui.view.MainWindow;
import com.restaurant.pos.model.Session;
import com.restaurant.pos.service.UserAuthService;
import com.restaurant.pos.service.impl.InMemoryUserAuthService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.restaurant.pos.util.TechCardSeeder;
import com.restaurant.pos.db.DatabaseBootstrap;
import com.restaurant.pos.db.OrderRepository;
import com.restaurant.pos.db.TableRepository;
import com.restaurant.pos.db.ReceiptRepository;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;
    private MainWindow mainWindow;
    private NavigationController navigationController;
    private AppContext appContext;
    private UserAuthService userAuthService;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        DatabaseBootstrap.ensureSchema();
        // Создание сервисов
        // Create InventoryService first (without TechCardService)
        InventoryService inventoryService = new InMemoryInventoryService();
        // Create TechCardService with InventoryService for calculateDishCost
        TechCardService techCardService = new InMemoryTechCardService(inventoryService);
        // Create FinanceService with TechCardService for profit analytics
        FinanceService financeService = new com.restaurant.pos.service.impl.InMemoryFinanceService(techCardService);
        // Recreate InventoryService with TechCardService and FinanceService for writeOffByOrder and expense tracking
        inventoryService = new InMemoryInventoryService(techCardService, financeService);
        inventoryService.seedDefaultInventory();
        OrderPricingService orderPricingService = new SimpleOrderPricingService();
        TableService tableService = new InMemoryTableService();
        tableService.initializeDefaultTables();
        TableRepository tableRepo = new TableRepository();
        // Enforce exactly 5 tables in DB (Table 1..5) and load them into memory
        tableRepo.ensureExactTables(5);
        java.util.List<com.restaurant.pos.domain.model.Table> loaded = tableRepo.findAll();
        if (loaded != null && tableService instanceof InMemoryTableService) {
            ((InMemoryTableService) tableService).loadTables(loaded);
        }
        ShiftService shiftService = new InMemoryShiftService(orderPricingService);
        shiftService.ensureActiveShift();
        OrderRepository orderRepository = new OrderRepository();
        OrderService orderService = new InMemoryOrderService(techCardService, inventoryService, financeService, orderPricingService, shiftService);
        ((InMemoryOrderService) orderService).setOrderRepository(orderRepository);
        // Restore FinanceService state from persisted PAID orders so Dashboard shows correct totals
        java.util.List<com.restaurant.pos.domain.model.Order> persistedOrders = orderRepository.findAll();
        for (com.restaurant.pos.domain.model.Order o : persistedOrders) {
            if (o != null && o.getStatus() == com.restaurant.pos.domain.enums.OrderStatus.PAID) {
                java.math.BigDecimal rev = orderPricingService.calculateTotal(o);
                financeService.recordPaidOrderProfit(o, rev != null ? rev : java.math.BigDecimal.ZERO);
            }
        }
        MenuService menuService = new InMemoryMenuService(techCardService, inventoryService);
        menuService.seedDefaultMenu();
        TechCardSeeder.seed(menuService, inventoryService, techCardService);

        CostCalculationService costCalculationService = new SimpleCostCalculationService(techCardService);
        FinanceReportService financeReportService = new InMemoryFinanceReportService();
        List<com.restaurant.pos.domain.model.SalaryTransaction> salaryTransactions = new ArrayList<>();
        SalaryReportService salaryReportService = new InMemorySalaryReportService(salaryTransactions);
        NetProfitService netProfitService = new SimpleNetProfitService(financeReportService, salaryReportService);
        KPIService kpiService = new SimpleKPIService(orderService, orderPricingService, costCalculationService);
        AnalyticsService analyticsService = new com.restaurant.pos.service.impl.InMemoryAnalyticsService(orderService, financeService, orderPricingService);
        // Director dashboard service (used by SimpleDirectorDashboardService inside ReportService)
        SimpleDirectorDashboardService directorDashboardService = new SimpleDirectorDashboardService(
                orderService,
                orderPricingService,
                costCalculationService,
                salaryReportService,
                netProfitService,
                kpiService
        );
        SessionService sessionService = new InMemorySessionService();
        AuthService authService = new InMemoryAuthService(sessionService);

        // Employee storage and salary services
        EmployeeStorageService employeeStorageService = new InMemoryEmployeeStorageService();
        SalaryService salaryService = new InMemorySalaryService(employeeStorageService);

        employeeStorageService.seedDefaultEmployees();
        for (com.restaurant.pos.domain.model.Employee employee : authService.getAllEmployees()) {
            employeeStorageService.saveEmployee(employee);
        }

        com.restaurant.pos.service.BIAnalyticsService biAnalyticsService = new com.restaurant.pos.service.impl.BIAnalyticsServiceImpl(orderService, orderPricingService, techCardService, employeeStorageService);
        com.restaurant.pos.service.ReportExportService reportExportService = new com.restaurant.pos.service.ReportExportService(biAnalyticsService);

        // Report service
        ReportService reportService = new InMemoryReportService(
                orderService,
                orderPricingService,
                costCalculationService,
                inventoryService,
                salaryService,
                employeeStorageService
        );

        // SessionContextService for unified session management
        SessionContextService sessionContextService = new SessionContextServiceImpl(
                authService, sessionService, shiftService
        );

        // User auth (login screen) - delegate UI auth to domain AuthService
        userAuthService = new InMemoryUserAuthService(authService);

        // Application context (global state: role, in-memory orders)
        appContext = new AppContext();
        InMemoryOrderStore orderStore = appContext.getOrderStore();
        DashboardService dashboardService = new InMemoryDashboardService(orderStore);

        // UI контекст сессии (для ролей/смены в интерфейсе)
        SessionContext sessionContext = new SessionContext(sessionContextService);
        // Sync initial state
        sessionContext.syncFromService();

        // Создание контроллеров
        DashboardController dashboardController = new DashboardController(
                dashboardService,
                orderService,
                orderPricingService,
                employeeStorageService,
                analyticsService,
                financeService,
                shiftService,
                tableService,
                inventoryService,
                biAnalyticsService
        );
        OrderController orderController = new OrderController(
                orderService,
                menuService,
                inventoryService,
                tableService,
                sessionService,
                sessionContext,
                sessionContextService,
                orderStore,
                employeeStorageService,
                appContext
        );
        // Set dashboard controller for auto-refresh on order submission
        orderController.setDashboardController(dashboardController);
        
        MenuController menuController = new MenuController(menuService, sessionContext);
        SalaryController salaryController = new SalaryController(
                employeeStorageService,
                salaryService,
                sessionContext
        );
        DirectorDashboardController directorDashboardController = new DirectorDashboardController(
                reportService,
                sessionContext
        );
        
        // Create WaiterController and set dashboard controller for auto-refresh
        WaiterController waiterController = new WaiterController(
                orderService,
                menuService,
                inventoryService,
                orderPricingService,
                tableService,
                employeeStorageService,
                sessionService
        );
        waiterController.setDashboardController(dashboardController);
        
        navigationController = new NavigationController(appContext);
        navigationController.setDashboardController(dashboardController);
        navigationController.setDirectorDashboardController(directorDashboardController);
        navigationController.setOrderController(orderController);
        navigationController.setWaiterController(waiterController);
        navigationController.setMenuController(menuController);
        navigationController.setSalaryController(salaryController);
        InventoryController inventoryController = new InventoryController(inventoryService, appContext);
        navigationController.setInventoryController(inventoryController);
        AnalyticsController analyticsController = new AnalyticsController(biAnalyticsService, employeeStorageService, analyticsService, reportExportService);
        navigationController.setAnalyticsController(analyticsController);
        OrderHistoryController orderHistoryController = new OrderHistoryController(orderService, orderRepository, orderPricingService, employeeStorageService);
        navigationController.setOrderHistoryController(orderHistoryController);
        navigationController.setSessionContext(sessionContext);
        navigationController.setAnalyticsService(analyticsService);
        
        // Set navigation controller for order controllers to refresh FinanceView
        orderController.setNavigationController(navigationController);
        waiterController.setNavigationController(navigationController);

        // Waiter POS workflow service (tables -> order -> receipt)
        com.restaurant.pos.service.WaiterPosService waiterPosService = new com.restaurant.pos.service.WaiterPosService(
                orderService,
                menuService,
                inventoryService,
                orderPricingService,
                tableService,
                sessionService,
                tableRepo,
                new ReceiptRepository()
        );
        navigationController.setWaiterPosService(waiterPosService);

        mainWindow = new MainWindow(navigationController, appContext);
        navigationController.setMainWindow(mainWindow);

        mainScene = new Scene(mainWindow.getRoot(), 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Login screen (first shown)
        Runnable onLoginSuccess = () -> {
            updateHeaderFromSession();
            mainWindow.getSidebar().setSessionContext(sessionContext);
            primaryStage.setScene(mainScene);

            com.restaurant.pos.model.UserRole role = appContext != null
                    ? appContext.getCurrentRole()
                    : com.restaurant.pos.model.UserRole.ADMIN;

            if (role == com.restaurant.pos.model.UserRole.WAITER) {
                // Ensure at least 5 tables for waiter workflow
                tableService.ensureMinimumTables(5);
                primaryStage.setTitle("Profit Agent - Orders");
                navigationController.showOrders();
            } else {
                // ADMIN and others: show dashboard as before
                primaryStage.setTitle("Profit Agent - Dashboard");
                navigationController.showDashboard();
            }
        };
        LoginController loginController = new LoginController(userAuthService, authService, appContext, onLoginSuccess);
        LoginView loginView = new LoginView(loginController);
        loginScene = new Scene(loginView, 400, 320);
        loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        mainWindow.getHeaderBar().setOnLogout(() -> {
            userAuthService.logout();
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Profit Agent - Вход");
        });

        primaryStage.setTitle("Profit Agent - Вход");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void updateHeaderFromSession() {
        if (mainWindow == null || !Session.isLoggedIn()) {
            return;
        }
        String username = Session.getCurrentUser().getUsername();
        String roleName = Session.getCurrentUser().getRole() != null ? Session.getCurrentUser().getRole().name() : "";
        mainWindow.getHeaderBar().setUserInfo(username, roleName);
    }

    public static void main(String[] args) {
        launch(args);
    }
}


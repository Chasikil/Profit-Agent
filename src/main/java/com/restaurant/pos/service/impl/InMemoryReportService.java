package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.InventoryOperationType;
import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.InventoryOperation;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.Product;
import com.restaurant.pos.service.CostCalculationService;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.service.ReportService;
import com.restaurant.pos.service.SalaryService;
import com.restaurant.pos.ui.model.InventoryReport;
import com.restaurant.pos.ui.model.SalaryReport;
import com.restaurant.pos.ui.model.SalesReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * In-memory implementation of ReportService.
 * All calculations are performed inside this service using BigDecimal.
 */
public class InMemoryReportService implements ReportService {

    private final OrderService orderService;
    private final OrderPricingService orderPricingService;
    private final CostCalculationService costCalculationService;
    private final InventoryService inventoryService;
    private final SalaryService salaryService;
    private final EmployeeStorageService employeeStorageService;

    public InMemoryReportService(OrderService orderService,
                                 OrderPricingService orderPricingService,
                                 CostCalculationService costCalculationService,
                                 InventoryService inventoryService,
                                 SalaryService salaryService,
                                 EmployeeStorageService employeeStorageService) {
        this.orderService = orderService;
        this.orderPricingService = orderPricingService;
        this.costCalculationService = costCalculationService;
        this.inventoryService = inventoryService;
        this.salaryService = salaryService;
        this.employeeStorageService = employeeStorageService;
    }

    @Override
    public SalesReport buildSalesReport() {
        SalesReport report = new SalesReport();

        // Get all orders
        List<Order> allOrders = orderService.getAllOrders();
        if (allOrders == null || allOrders.isEmpty()) {
            return report;
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        // Calculate revenue and cost for all PAID orders
        for (Order order : allOrders) {
            if (order == null || order.getStatus() != OrderStatus.PAID) {
                continue;
            }

            // Calculate revenue using OrderPricingService
            BigDecimal revenue = orderPricingService.calculateTotal(order);
            if (revenue != null) {
                totalRevenue = totalRevenue.add(revenue);
            }

            // Calculate cost using CostCalculationService
            BigDecimal cost = costCalculationService.calculateOrderCost(order);
            if (cost != null) {
                totalCost = totalCost.add(cost);
            }
        }

        // Calculate gross profit
        BigDecimal grossProfit = totalRevenue.subtract(totalCost);

        // Calculate margin percentage
        BigDecimal marginPercent = BigDecimal.ZERO;
        if (totalRevenue.signum() > 0) {
            marginPercent = grossProfit
                    .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        report.setTotalRevenue(totalRevenue);
        report.setTotalCost(totalCost);
        report.setGrossProfit(grossProfit);
        report.setMarginPercent(marginPercent);

        return report;
    }

    @Override
    public InventoryReport buildInventoryReport() {
        InventoryReport report = new InventoryReport();

        // Calculate total stock value
        List<Product> allProducts = inventoryService.getAllProducts();
        BigDecimal totalStockValue = BigDecimal.ZERO;
        if (allProducts != null) {
            for (Product product : allProducts) {
                if (product == null) {
                    continue;
                }
                BigDecimal quantity = product.getQuantityInStock();
                BigDecimal costPerUnit = product.getCostPerUnit();
                if (quantity != null && costPerUnit != null && quantity.signum() > 0 && costPerUnit.signum() > 0) {
                    BigDecimal productValue = quantity.multiply(costPerUnit);
                    totalStockValue = totalStockValue.add(productValue);
                }
            }
        }

        // Calculate total write-offs
        List<InventoryOperation> operations = inventoryService.getInventoryReport();
        BigDecimal totalWriteOffs = BigDecimal.ZERO;
        if (operations != null) {
            for (InventoryOperation operation : operations) {
                if (operation == null || operation.getType() != InventoryOperationType.OUT) {
                    continue;
                }
                Product product = operation.getProduct();
                BigDecimal quantity = operation.getQuantity();
                if (product != null && quantity != null && quantity.signum() > 0) {
                    BigDecimal costPerUnit = product.getCostPerUnit();
                    if (costPerUnit != null && costPerUnit.signum() > 0) {
                        BigDecimal writeOffValue = quantity.multiply(costPerUnit);
                        totalWriteOffs = totalWriteOffs.add(writeOffValue);
                    }
                }
            }
        }

        report.setTotalStockValue(totalStockValue);
        report.setTotalWriteOffs(totalWriteOffs);

        return report;
    }

    @Override
    public SalaryReport buildSalaryReport() {
        SalaryReport report = new SalaryReport();

        // Get all employees
        List<Employee> allEmployees = employeeStorageService.getAllEmployees();
        if (allEmployees == null || allEmployees.isEmpty()) {
            return report;
        }

        BigDecimal totalPayroll = BigDecimal.ZERO;
        BigDecimal totalBonuses = BigDecimal.ZERO;
        BigDecimal totalPenalties = BigDecimal.ZERO;

        // Calculate payroll, bonuses, and penalties for each employee
        for (Employee employee : allEmployees) {
            if (employee == null || employee.getId() == null) {
                continue;
            }

            // Calculate salary balance (includes worked hours * rate + bonuses - penalties)
            BigDecimal salaryBalance = salaryService.getSalaryBalance(employee.getId());
            if (salaryBalance != null) {
                totalPayroll = totalPayroll.add(salaryBalance);
            }

            // Note: InMemorySalaryService stores bonuses and penalties in internal maps
            // We need to access them. Since SalaryService doesn't expose these directly,
            // we'll calculate them from the salary balance and worked hours.
            // Actually, we can't get bonuses/penalties separately from SalaryService interface.
            // Let me check the implementation again...

            // Alternative: We can calculate base salary and infer bonuses/penalties
            // But that's not accurate. Let me add methods to SalaryService to get bonuses/penalties.
            // Actually, for now, let's just calculate total payroll. We can enhance SalaryService later.
        }

        // Get total bonuses and penalties from SalaryService
        totalBonuses = salaryService.getTotalBonuses();
        totalPenalties = salaryService.getTotalPenalties();

        report.setTotalPayroll(totalPayroll);
        report.setTotalBonuses(totalBonuses);
        report.setTotalPenalties(totalPenalties);

        return report;
    }
}

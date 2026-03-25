package com.restaurant.pos.util;

import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.model.ShiftModel;
import com.restaurant.pos.service.FinanceService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.ShiftService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates PDF shift reports.
 */
public class ShiftReportGenerator {

    private static final String REPORTS_DIR = "reports";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public static String generateShiftReport(ShiftService.ShiftSummary summary, ShiftModel shift,
                                             FinanceService financeService, OrderPricingService pricingService) {
        if (summary == null || shift == null) {
            return null;
        }

        try {
            ensureReportsDirectory();
            String filename = REPORTS_DIR + "/shift_" + shift.getId() + ".pdf";
            File file = new File(filename);
            if (file.exists()) {
                return filename;
            }

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                    float y = 750;
                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    content.newLineAtOffset(50, y);
                    content.showText("SHIFT REPORT");
                    y -= 30;
                    content.setFont(PDType1Font.HELVETICA, 10);
                    if (shift.getStartTime() != null) {
                        content.newLineAtOffset(0, -20);
                        content.showText("Start Time: " + shift.getStartTime().format(DATE_FORMAT));
                        y -= 20;
                    }
                    if (shift.getEndTime() != null) {
                        content.newLineAtOffset(0, -20);
                        content.showText("End Time: " + shift.getEndTime().format(DATE_FORMAT));
                        y -= 20;
                    }
                    content.newLineAtOffset(0, -20);
                    content.showText("----------------------------------------");
                    y -= 20;
                    content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    content.newLineAtOffset(0, -20);
                    content.showText("FINANCIAL SUMMARY");
                    y -= 25;
                    content.setFont(PDType1Font.HELVETICA, 10);
                    content.newLineAtOffset(0, -20);
                    content.showText("Total Revenue: " + MONEY_FORMAT.format(summary.totalRevenue) + " ₽");
                    y -= 20;
                    content.newLineAtOffset(0, -20);
                    content.showText("Total Cost: " + MONEY_FORMAT.format(summary.totalCost) + " ₽");
                    y -= 20;
                    content.newLineAtOffset(0, -20);
                    content.showText("Total Profit: " + MONEY_FORMAT.format(summary.totalProfit) + " ₽");
                    y -= 20;
                    content.newLineAtOffset(0, -20);
                    content.showText("Orders Count: " + summary.orderCount);
                    y -= 20;
                    content.newLineAtOffset(0, -20);
                    content.showText("Cash Total: " + MONEY_FORMAT.format(summary.cashTotal) + " ₽");
                    y -= 20;
                    content.newLineAtOffset(0, -20);
                    content.showText("Card Total: " + MONEY_FORMAT.format(summary.cardTotal) + " ₽");
                    y -= 30;

                    if (shift.getOrders() != null && !shift.getOrders().isEmpty() && financeService != null) {
                        Map<Long, DishStats> dishStats = calculateDishStats(shift.getOrders(), pricingService);
                        if (!dishStats.isEmpty()) {
                            content.newLineAtOffset(0, -20);
                            content.showText("----------------------------------------");
                            content.setFont(PDType1Font.HELVETICA_BOLD, 11);
                            content.newLineAtOffset(0, -20);
                            content.showText("TOP SELLING DISHES");
                            content.setFont(PDType1Font.HELVETICA, 9);
                            List<DishStats> topSelling = dishStats.values().stream()
                                    .sorted((a, b) -> Integer.compare(b.quantity, a.quantity))
                                    .limit(5)
                                    .collect(java.util.stream.Collectors.toList());
                            for (DishStats d : topSelling) {
                                content.newLineAtOffset(0, -15);
                                content.showText((d.name.length() > 20 ? d.name.substring(0, 17) + "..." : d.name) + ": " + d.quantity + " sold");
                            }
                            content.setFont(PDType1Font.HELVETICA, 10);
                            content.newLineAtOffset(0, -20);
                            content.showText("----------------------------------------");
                            content.setFont(PDType1Font.HELVETICA_BOLD, 11);
                            content.newLineAtOffset(0, -20);
                            content.showText("MOST PROFITABLE DISHES");
                            content.setFont(PDType1Font.HELVETICA, 9);
                            List<DishStats> mostProfitable = dishStats.values().stream()
                                    .sorted((a, b) -> b.profit.compareTo(a.profit))
                                    .limit(5)
                                    .collect(java.util.stream.Collectors.toList());
                            for (DishStats d : mostProfitable) {
                                content.newLineAtOffset(0, -15);
                                content.showText((d.name.length() > 20 ? d.name.substring(0, 17) + "..." : d.name) + ": " + MONEY_FORMAT.format(d.profit) + " profit");
                            }
                        }
                    }
                    content.endText();
                }
                doc.save(filename);
            }
            return filename;
        } catch (IOException e) {
            System.err.println("Failed to generate shift report: " + e.getMessage());
            return null;
        }
    }

    private static Map<Long, DishStats> calculateDishStats(List<Order> orders, OrderPricingService pricingService) {
        Map<Long, DishStats> stats = new HashMap<>();
        if (orders == null || pricingService == null) return stats;
        for (Order order : orders) {
            if (order == null || order.getItems() == null) continue;
            for (com.restaurant.pos.domain.model.OrderItem item : order.getItems()) {
                if (item == null || item.getDish() == null) continue;
                Long dishId = item.getDish().getId();
                if (dishId == null) continue;
                DishStats s = stats.computeIfAbsent(dishId, k -> new DishStats(item.getDish().getName()));
                s.quantity += item.getQuantity();
                BigDecimal revenue = item.getDish().getSalePrice() != null
                        ? item.getDish().getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : BigDecimal.ZERO;
                BigDecimal cost = order.getTotalCost() != null && order.getItems().size() > 0
                        ? order.getTotalCost().divide(BigDecimal.valueOf(order.getItems().size()), 2, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                s.profit = s.profit.add(revenue.subtract(cost));
            }
        }
        return stats;
    }

    private static void ensureReportsDirectory() throws IOException {
        File dir = new File(REPORTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static class DishStats {
        final String name;
        int quantity;
        BigDecimal profit = BigDecimal.ZERO;

        DishStats(String name) {
            this.name = name != null ? name : "Unknown";
        }
    }
}

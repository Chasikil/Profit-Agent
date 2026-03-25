package com.restaurant.pos.service;

import com.restaurant.pos.service.BIAnalyticsService.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports analytics reports to Excel.
 */
public class ReportExportService {

    private static final String EXPORTS_DIR = "exports";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final java.text.DecimalFormat MONEY = new java.text.DecimalFormat("#,##0.00");

    private final BIAnalyticsService biAnalyticsService;

    public ReportExportService(BIAnalyticsService biAnalyticsService) {
        this.biAnalyticsService = biAnalyticsService;
    }

    public String exportToExcel(LocalDate start, LocalDate end) throws IOException {
        if (biAnalyticsService == null) throw new IllegalStateException("BIAnalyticsService not set");
        ensureExportsDir();
        String dateStr = LocalDate.now().format(DATE_FMT);
        String path = EXPORTS_DIR + "/report_" + dateStr + ".xlsx";
        File file = new File(path);
        int suffix = 1;
        while (file.exists()) {
            path = EXPORTS_DIR + "/report_" + dateStr + "_" + (suffix++) + ".xlsx";
            file = new File(path);
        }

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            PeriodSummary summary = biAnalyticsService.getPeriodSummary(start, end);
            List<DishMarginStats> dishStats = biAnalyticsService.getDishMarginStats(start, end);
            List<WaiterKPI> waiterKPI = biAnalyticsService.getWaiterKPI(start, end);

            // Sheet 1: Summary
            var sheet1 = wb.createSheet("Summary");
            int row = 0;
            createHeaderCell(sheet1, row, 0, "Period: " + start.format(DATE_FMT) + " - " + end.format(DATE_FMT));
            row++;
            createRow(sheet1, row++, "Revenue", formatMoney(summary.revenue()));
            createRow(sheet1, row++, "Cost", formatMoney(summary.cost()));
            createRow(sheet1, row++, "Profit", formatMoney(summary.profit()));
            createRow(sheet1, row++, "Margin %", formatPct(summary.marginPercent()));
            createRow(sheet1, row++, "Orders Count", String.valueOf(summary.orderCount()));
            sheet1.autoSizeColumn(0);
            sheet1.autoSizeColumn(1);

            // Sheet 2: Dish statistics
            var sheet2 = wb.createSheet("Dish Statistics");
            createDishHeader(sheet2, 0);
            row = 1;
            for (DishMarginStats d : dishStats) {
                var r = sheet2.createRow(row++);
                r.createCell(0).setCellValue(d.dishName());
                r.createCell(1).setCellValue(d.timesSold());
                r.createCell(2).setCellValue(formatMoney(d.revenue()));
                r.createCell(3).setCellValue(formatMoney(d.totalCost()));
                r.createCell(4).setCellValue(formatMoney(d.grossProfit()));
                r.createCell(5).setCellValue(formatPct(d.marginPercent()));
            }
            for (int i = 0; i <= 5; i++) sheet2.autoSizeColumn(i);

            // Sheet 3: Waiter KPI
            var sheet3 = wb.createSheet("Waiter KPI");
            createWaiterHeader(sheet3, 0);
            row = 1;
            for (WaiterKPI w : waiterKPI) {
                var r = sheet3.createRow(row++);
                r.createCell(0).setCellValue(w.waiterName());
                r.createCell(1).setCellValue(w.ordersHandled());
                r.createCell(2).setCellValue(formatMoney(w.totalRevenue()));
                r.createCell(3).setCellValue(formatMoney(w.totalProfit()));
                r.createCell(4).setCellValue(formatMoney(w.averageOrderValue()));
                r.createCell(5).setCellValue(formatMoney(w.averageProfitPerOrder()));
            }
            for (int i = 0; i <= 5; i++) sheet3.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
        return path;
    }

    private void createHeaderCell(org.apache.poi.ss.usermodel.Sheet sheet, int row, int col, String text) {
        var r = sheet.getRow(row) != null ? sheet.getRow(row) : sheet.createRow(row);
        var c = r.createCell(col);
        c.setCellValue(text);
        var style = sheet.getWorkbook().createCellStyle();
        var font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        c.setCellStyle(style);
    }

    private void createRow(org.apache.poi.ss.usermodel.Sheet sheet, int row, String label, String value) {
        var r = sheet.createRow(row);
        r.createCell(0).setCellValue(label);
        r.createCell(1).setCellValue(value);
    }

    private void createDishHeader(org.apache.poi.ss.usermodel.Sheet sheet, int rowIdx) {
        var r = sheet.createRow(rowIdx);
        String[] headers = {"Dish Name", "Times Sold", "Revenue", "Cost", "Profit", "Margin %"};
        for (int i = 0; i < headers.length; i++) {
            r.createCell(i).setCellValue(headers[i]);
        }
    }

    private void createWaiterHeader(org.apache.poi.ss.usermodel.Sheet sheet, int rowIdx) {
        var r = sheet.createRow(rowIdx);
        String[] headers = {"Waiter", "Orders", "Revenue", "Profit", "Avg Order Value", "Avg Profit/Order"};
        for (int i = 0; i < headers.length; i++) {
            r.createCell(i).setCellValue(headers[i]);
        }
    }

    private static String formatMoney(BigDecimal v) {
        if (v == null) return "0.00";
        return MONEY.format(v.doubleValue());
    }

    private static String formatPct(BigDecimal v) {
        if (v == null) return "0%";
        return v.setScale(2, java.math.RoundingMode.HALF_UP) + "%";
    }

    private static void ensureExportsDir() {
        File dir = new File(EXPORTS_DIR);
        if (!dir.exists()) dir.mkdirs();
    }
}

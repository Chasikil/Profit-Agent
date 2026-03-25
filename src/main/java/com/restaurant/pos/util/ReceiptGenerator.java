package com.restaurant.pos.util;

import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.service.OrderPricingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Generates PDF receipts for paid orders.
 */
public class ReceiptGenerator {

    private static final String RECEIPTS_DIR = "receipts";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public static String generateReceipt(Order order, OrderPricingService pricingService) {
        if (order == null || !order.isPaid() || pricingService == null) {
            return null;
        }

        try {
            ensureReceiptsDirectory();
            String filename = RECEIPTS_DIR + "/order_" + order.getId() + ".pdf";
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
                    content.showText("Profit Agent");
                    y -= 30;
                    content.setFont(PDType1Font.HELVETICA, 10);
                    content.newLineAtOffset(0, -30);
                    content.showText("Order ID: " + order.getId());
                    y -= 20;
                    if (order.getCreatedAt() != null) {
                        content.newLineAtOffset(0, -20);
                        content.showText("Date: " + order.getCreatedAt().format(DATE_FORMAT));
                        y -= 20;
                    }
                    if (order.getTable() != null) {
                        content.newLineAtOffset(0, -20);
                        content.showText("Table: " + order.getTable().getNumber());
                        y -= 20;
                    }
                    if (order.getWaiter() != null) {
                        content.newLineAtOffset(0, -20);
                        content.showText("Waiter: " + order.getWaiter().getFullName());
                        y -= 20;
                    }
                    content.newLineAtOffset(0, -20);
                    content.showText("----------------------------------------");
                    y -= 20;
                    content.newLineAtOffset(0, -20);
                    content.showText("Items:");
                    y -= 20;

                    BigDecimal total = BigDecimal.ZERO;
                    if (order.getItems() != null) {
                        for (OrderItem item : order.getItems()) {
                            if (item == null || item.getDish() == null) continue;
                            String name = item.getDish().getName() != null ? item.getDish().getName() : "Unknown";
                            int qty = item.getQuantity();
                            BigDecimal price = item.getDish().getSalePrice() != null ? item.getDish().getSalePrice() : BigDecimal.ZERO;
                            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));
                            total = total.add(itemTotal);
                            String line = String.format("%s %d x %s = %s",
                                    name.length() > 20 ? name.substring(0, 17) + "..." : name,
                                    qty, MONEY_FORMAT.format(price), MONEY_FORMAT.format(itemTotal));
                            content.newLineAtOffset(0, -15);
                            content.showText(line);
                            y -= 15;
                        }
                    }

                    content.newLineAtOffset(0, -20);
                    content.showText("----------------------------------------");
                    y -= 20;
                    content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    content.newLineAtOffset(0, -20);
                    content.showText("Total: " + MONEY_FORMAT.format(total) + " ₽");
                    y -= 25;
                    content.setFont(PDType1Font.HELVETICA, 10);
                    if (order.getPaymentMethod() != null) {
                        content.newLineAtOffset(0, -20);
                        content.showText("Payment: " + order.getPaymentMethod().name());
                        y -= 20;
                    }
                    if (order.getPaymentMethod() == PaymentMethod.CASH && order.getAmountPaid() != null && order.getChange() != null) {
                        content.newLineAtOffset(0, -20);
                        content.showText("Paid: " + MONEY_FORMAT.format(order.getAmountPaid()) + " ₽");
                        y -= 20;
                        content.newLineAtOffset(0, -20);
                        content.showText("Change: " + MONEY_FORMAT.format(order.getChange()) + " ₽");
                        y -= 20;
                    }
                    content.newLineAtOffset(0, -30);
                    content.showText("Thank you for your visit!");
                    content.endText();
                }
                doc.save(filename);
            }
            return filename;
        } catch (IOException e) {
            System.err.println("Failed to generate receipt: " + e.getMessage());
            return null;
        }
    }

    private static void ensureReceiptsDirectory() throws IOException {
        File dir = new File(RECEIPTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}

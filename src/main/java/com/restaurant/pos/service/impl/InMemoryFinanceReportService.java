package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.OrderFinancialResult;
import com.restaurant.pos.service.FinanceReportService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryFinanceReportService implements FinanceReportService {

    private final List<OrderFinancialResult> orderResults = new ArrayList<>();

    @Override
    public BigDecimal getTotalRevenue() {
        return orderResults.stream()
                .map(OrderFinancialResult::getTotalRevenue)
                .filter(r -> r != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalCost() {
        return orderResults.stream()
                .map(OrderFinancialResult::getTotalCost)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalProfit() {
        return getTotalRevenue().subtract(getTotalCost());
    }

    @Override
    public BigDecimal getProfitByPeriod(LocalDateTime start, LocalDateTime end) {
        // Упрощенная реализация: возвращаем общую прибыль
        // В реальной системе здесь была бы фильтрация по датам заказов
        return getTotalProfit();
    }

    @Override
    public void recordOrderResult(OrderFinancialResult result) {
        if (result != null) {
            orderResults.add(result);
        }
    }
}

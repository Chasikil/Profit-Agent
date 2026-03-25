package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.OrderFinancialResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FinanceReportService {

    BigDecimal getTotalRevenue();

    BigDecimal getTotalCost();

    BigDecimal getTotalProfit();

    BigDecimal getProfitByPeriod(LocalDateTime start, LocalDateTime end);

    void recordOrderResult(OrderFinancialResult result);
}


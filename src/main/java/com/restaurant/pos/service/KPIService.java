package com.restaurant.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface KPIService {

    BigDecimal getAverageMargin(LocalDateTime start, LocalDateTime end);

    BigDecimal getFoodCostPercent(LocalDateTime start, LocalDateTime end);

    BigDecimal getRevenuePerOrder(LocalDateTime start, LocalDateTime end);
}

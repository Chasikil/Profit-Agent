package com.restaurant.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface NetProfitService {

    BigDecimal calculateNetProfit(LocalDateTime start, LocalDateTime end);
}

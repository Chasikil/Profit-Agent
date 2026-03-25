package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderFinancialResult;

public interface OrderFinancialService {

    OrderFinancialResult calculateOrderFinancials(Order order);
}


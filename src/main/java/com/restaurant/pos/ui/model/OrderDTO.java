package com.restaurant.pos.ui.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO {

    private Long id;
    private List<OrderItemDTO> items = new ArrayList<>();
    private BigDecimal total;

    public OrderDTO() { }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public List<OrderItemDTO> getItems() { return items; }

    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public BigDecimal getTotal() { return total; }

    public void setTotal(BigDecimal total) { this.total = total; }
}

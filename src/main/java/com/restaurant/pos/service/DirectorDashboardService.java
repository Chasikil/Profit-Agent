package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.DashboardMetrics;

import java.time.LocalDateTime;

public interface DirectorDashboardService {

    DashboardMetrics getDashboardMetrics(LocalDateTime start, LocalDateTime end);
}

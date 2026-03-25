package com.restaurant.pos.app.facade;

import com.restaurant.pos.domain.model.DashboardMetrics;
import com.restaurant.pos.service.DirectorDashboardService;

import java.time.LocalDateTime;

public class DashboardFacade {

    private final DirectorDashboardService directorDashboardService;

    public DashboardFacade(DirectorDashboardService directorDashboardService) {
        this.directorDashboardService = directorDashboardService;
    }

    public DashboardMetrics getDashboardMetrics(LocalDateTime start, LocalDateTime end) {
        return directorDashboardService.getDashboardMetrics(start, end);
    }
}

// com/opes/account/web/HomeController.java
package com.opes.account.web;

import com.opes.account.service.DashboardService;
import com.opes.account.web.dto.DashboardSummaryDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary/dev")
    public DashboardSummaryDTO getSummaryDev(@RequestParam String userId) {
        return dashboardService.getSummary(userId);
    }
}

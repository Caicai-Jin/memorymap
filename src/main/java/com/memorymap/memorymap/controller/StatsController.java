package com.memorymap.memorymap.controller;

import com.memorymap.memorymap.dto.StatsResponse;
import com.memorymap.memorymap.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {
    private final StatsService statsService;

    public  StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats/{year}")
    public StatsResponse getStats(@PathVariable int year) {
        return statsService.getStats(year);
    }
}

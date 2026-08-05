package com.memorymap.memorymap.controller;

import com.memorymap.memorymap.dto.LocationResponse;
import com.memorymap.memorymap.dto.LocationSearchResult;
import com.memorymap.memorymap.model.Location;
import com.memorymap.memorymap.service.LocationSearchService;
import com.memorymap.memorymap.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LocationController {
    private final LocationSearchService locationSearchService;
    private final LocationService locationService;

    public LocationController(LocationSearchService locationSearchService, LocationService locationService) {
        this.locationSearchService = locationSearchService;
        this.locationService = locationService;
    }

    @GetMapping("/locations/search")
    public List<LocationSearchResult> search(@RequestParam String query) {
        return locationSearchService.search(query);
    }

    @PostMapping("/locations")
    public LocationResponse createLocation(@RequestBody Location location){
        return toResponse(locationService.createLocation(location));
    }

    private LocationResponse toResponse(Location location){
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getLatitude(),
                location.getLongitude(),
                location.getType()
        );
    }
}
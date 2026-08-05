package com.memorymap.memorymap.controller;

import java.util.List;
import com.memorymap.memorymap.dto.LocationResponse;
import com.memorymap.memorymap.dto.MomentResponse;
import com.memorymap.memorymap.model.Location;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.service.LocationService;
import com.memorymap.memorymap.service.MomentService;
import org.springframework.web.bind.annotation.*;

@RestController
public class MomentController {
    private final MomentService momentService;
    private final LocationService locationService;

    public MomentController(MomentService momentService, LocationService locationService) {
        this.momentService = momentService;
        this.locationService = locationService;
    }

    @PostMapping("/moments")
    public MomentResponse createMoment(@RequestBody Moment moment){
        return toResponse(momentService.createMoment(moment));
    }

    @GetMapping("/moments")
    public List<MomentResponse> getAllMoments(){
        return momentService.getAllMoments().stream().map(this::toResponse).toList();
    }
    //.map(moment -> this.toResponse(moment))

    @GetMapping("/moments/{id}")
    public MomentResponse getMomentById(@PathVariable Long id){
        return toResponse(momentService.getMomentById(id).get());
    }

    @PutMapping("/moments/{id}")
    public MomentResponse updateMoment(@PathVariable Long id, @RequestBody Moment updatedData){
        return toResponse(momentService.updateMoment(id, updatedData));
    }
    // 1. momentService.getAllMoments() — get the list of all your real moments off the shelf
    //  2. .stream() — put that whole list onto the conveyor belt, one item at a time
    //  3. .map(this::toResponse) — as each real Moment passes by, the translator (toResponse)
    //  converts it into a safe MomentResponse
    //  4. .toList() — collect everything that came off the belt into a fresh list

    @DeleteMapping("/moments/{id}")
    public void deleteMoment(@PathVariable Long id){
        momentService.deleteMoment(id);
    }

    // Converts the real JPA entity into a safe response shape.
    // Never returns the raw Moment/Location entities to the client,
    // so there's no risk of leaking internals (like the owner's password hash)
    // and masking the real Location is just building this object, not mutating the database one.
    private MomentResponse toResponse(Moment moment){
        Location maskedLocation = locationService.maskIfHome(moment.getLocation());
        LocationResponse locationResponse = null;
        if (maskedLocation != null) {
            locationResponse = new LocationResponse(
                    maskedLocation.getId(),
                    maskedLocation.getName(),
                    maskedLocation.getAddress(),
                    maskedLocation.getLatitude(),
                    maskedLocation.getLongitude(),
                    maskedLocation.getType()
            );
        }
        return new MomentResponse(
                moment.getId(),
                moment.getContent(),
                moment.getMood(),
                moment.getCreatedAt(),
                moment.getUpdatedAt(),
                locationResponse
        );
    }
}

package com.memorymap.memorymap.controller;

import java.util.List;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.service.MomentService;
import org.springframework.web.bind.annotation.*;

@RestController
public class MomentController {
    private final MomentService momentService;

    public MomentController(MomentService momentService) {
        this. momentService = momentService;
    }

    @PostMapping("/moments")
    public Moment createMoment(@RequestBody Moment moment){
        return momentService.createMoment(moment);
    }

    @GetMapping("/moments")
    public List<Moment> getAllMoments(){
        return momentService.getAllMoments();
    }

    @GetMapping("/moments/{id}")
    public Moment getMomentById(@PathVariable Long id){
        return momentService.getMomentById(id).get();
    }

    @PutMapping("/moments/{id}")
    public Moment updateMoment(@PathVariable Long id, @RequestBody Moment updatedData){
        return momentService.updateMoment(id, updatedData);
    }

    @DeleteMapping("/moments/{id}")
    public void deleteMoment(@PathVariable Long id){
        momentService.deleteMoment(id);
    }
}

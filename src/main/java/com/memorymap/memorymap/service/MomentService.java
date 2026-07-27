package com.memorymap.memorymap.service;

import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.repository.MomentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MomentService {
    private final MomentRepository momentRepository;

     public MomentService(MomentRepository momentRepository) {
        this.momentRepository = momentRepository;
    }

    //return the created moment
    public Moment createMoment(Moment moment){
         //user filled content and mood, id is auto-generated
         moment.setCreatedAt(LocalDateTime.now());
         moment.setUpdatedAt(moment.getCreatedAt());
         //service hands object to momentRepository.save(moment)
        return momentRepository.save(moment);
    }

    public List<Moment> getAllMoments(){
         return momentRepository.findAll();
    }

    public Optional<Moment> getMomentById(Long id){
         return momentRepository.findById(id);
    }

    public Moment updateMoment(Long id, Moment updatedData){
         Moment existingMoment= momentRepository.findById(id).get();
         existingMoment.setContent(updatedData.getContent());
         existingMoment.setMood(updatedData.getMood());
         existingMoment.setUpdatedAt(LocalDateTime.now());
         return momentRepository.save(existingMoment);

    }

    public void deleteMoment(Long id){
         momentRepository.deleteById(id);
    }


}

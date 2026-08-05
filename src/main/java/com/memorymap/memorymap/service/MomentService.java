package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.MomentAccessDeniedException;
import com.memorymap.memorymap.exception.MomentNotFoundException;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.repository.MomentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MomentService {
    private final MomentRepository momentRepository;
    private final UserService userService;

    public MomentService(MomentRepository momentRepository, UserService userService) {
        this.momentRepository = momentRepository;
        this.userService = userService;
    }

    //return the created moment
    public Moment createMoment(Moment moment){
         //user filled content and mood, id is auto-generated
         moment.setCreatedAt(LocalDateTime.now());
         moment.setUpdatedAt(moment.getCreatedAt());
         moment.setOwner(userService.getCurrentUser());
         //service hands object to momentRepository.save(moment)
        return momentRepository.save(moment);
    }

    public List<Moment> getAllMoments(){
         return momentRepository.findByOwner(userService.getCurrentUser());
    }

    public Optional<Moment> getMomentById(Long id){
        Moment moment = momentRepository.findById(id)
                .orElseThrow(() -> new MomentNotFoundException("Moment not found"));

        if(userService.getCurrentUser().getEmail().equals(moment.getOwner().getEmail())){
            return Optional.of(moment);
        }
        else{
            throw new MomentAccessDeniedException("not the owner of the memory");
        }

    }

    public Moment updateMoment(Long id, Moment updatedData){
        Moment existingMoment= momentRepository.findById(id)
                .orElseThrow(() -> new MomentNotFoundException("Moment not found"));
        if(userService.getCurrentUser().getEmail().equals(existingMoment.getOwner().getEmail())){
            existingMoment.setContent(updatedData.getContent());
            existingMoment.setMood(updatedData.getMood());
            existingMoment.setUpdatedAt(LocalDateTime.now());
            return momentRepository.save(existingMoment);
        }
        else{
            throw new MomentAccessDeniedException("not the owner of the memory");
        }
    }

    public void deleteMoment(Long id){
        Moment moment= momentRepository.findById(id)
                        .orElseThrow(() -> new MomentNotFoundException("Moment not found"));
        if(userService.getCurrentUser().getEmail().equals(moment.getOwner().getEmail())){
            momentRepository.deleteById(id);
        }
        else{
            throw new MomentAccessDeniedException("not the owner of the memory");
        }
    }


}

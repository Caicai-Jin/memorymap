package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.LocationNotFoundException;
import com.memorymap.memorymap.exception.MomentAccessDeniedException;
import com.memorymap.memorymap.exception.MomentNotFoundException;
import com.memorymap.memorymap.model.Location;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.repository.LocationRepository;
import com.memorymap.memorymap.repository.MomentRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MomentService {
    private final MomentRepository momentRepository;
    private final UserService userService;
    private final LocationRepository locationRepository;
    private final StringRedisTemplate redisTemplate;

    public MomentService(MomentRepository momentRepository, UserService userService, LocationRepository locationRepository, StringRedisTemplate redisTemplate) {
        this.momentRepository = momentRepository;
        this.userService = userService;
        this.locationRepository = locationRepository;
        this.redisTemplate = redisTemplate;
    }

    // Cached stats are keyed "stats::<userId>-<year>". A moment write can only
    // affect the owning user's own stats, so we only need to clear that user's
    // entries (across all years) rather than the whole "stats" cache.
    private void evictStatsCache(Long userId) {
        Set<String> keys = redisTemplate.keys("stats::" + userId + "-*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // The client only sends a location's id (e.g. {"id": 3}), not its full data.
    // JPA would happily save that stub as-is, but then moment.getLocation() would
    // return the incomplete stub instead of the real, fully-loaded row — so we
    // look up the real Location here before it's ever attached to a Moment.
    private Location resolveLocation(Location location){
        if (location == null) {
            return null;
        }
        return locationRepository.findById(location.getId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
    }

    //return the created moment
    public Moment createMoment(Moment moment){
         //user filled content and mood, id is auto-generated
         moment.setCreatedAt(LocalDateTime.now());
         moment.setUpdatedAt(moment.getCreatedAt());
         moment.setOwner(userService.getCurrentUser());
         moment.setLocation(resolveLocation(moment.getLocation()));
         //service hands object to momentRepository.save(moment)
        Moment saved = momentRepository.save(moment);
        evictStatsCache(saved.getOwner().getId());
        return saved;
    }

    public List<Moment> getAllMoments(){
         return momentRepository.findByOwnerOrderByCreatedAtDesc(userService.getCurrentUser());
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
            existingMoment.setLocation(resolveLocation(updatedData.getLocation()));
            existingMoment.setUpdatedAt(LocalDateTime.now());
            Moment saved = momentRepository.save(existingMoment);
            evictStatsCache(saved.getOwner().getId());
            return saved;
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
            evictStatsCache(moment.getOwner().getId());
        }
        else{
            throw new MomentAccessDeniedException("not the owner of the memory");
        }
    }


}


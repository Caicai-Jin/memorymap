package com.memorymap.memorymap.service;

import com.memorymap.memorymap.dto.LocationResponse;
import com.memorymap.memorymap.dto.StatsResponse;
import com.memorymap.memorymap.model.Location;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.model.MoodSentiment;
import com.memorymap.memorymap.repository.MomentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

@Service
public class StatsService {
    private final MomentRepository momentRepository;
    private final UserService userService;
    private final MoodSentimentClassifier classifier = new MoodSentimentClassifier();
    private final LocationService locationService;

    public StatsService(MomentRepository momentRepository, UserService userService, LocationService locationService) {
        this.momentRepository = momentRepository;
        this.userService = userService;
        this.locationService = locationService;
    }

    // SpEL cache keys can only call public members on #root.target, not private
    // fields directly — this small public wrapper is what the @Cacheable key below calls.
    public Long currentUserId() {
        return userService.getCurrentUser().getId();
    }

    //before running this method, check if the result is already saved (cached) in Redis.
    // If it is, return that saved result instantly instead of re-running the method.
    // If not, run the method normally, then save the result before returning it.
    @Cacheable(value = "stats", key = "#root.target.currentUserId() + '-' + #year")
    public StatsResponse getStats(int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        List<Moment> moments = momentRepository.findByOwnerAndCreatedAtBetween(userService.getCurrentUser(), start, end);
        List<String> allMoods = moments.stream()
                .flatMap(moment -> moment.getMood().stream())
                .toList();

        Map<String, Long> moodBreakdown = allMoods.stream()
                .collect(Collectors.groupingBy(mood -> mood, Collectors.counting()));

        String dominantMood = moodBreakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        List<Location> positiveLocationHits = new ArrayList<>();
        List<Location> negativeLocationHits = new ArrayList<>();
        for (Moment moment : moments) {
            if (moment.getLocation() == null) {
                continue;
            }
            for (String mood : moment.getMood()) {
                MoodSentiment sentiment = classifier.classify(mood);
                if (sentiment == MoodSentiment.POSITIVE) {
                    positiveLocationHits.add(moment.getLocation());
                }
                if (sentiment == MoodSentiment.NEGATIVE) {
                    negativeLocationHits.add(moment.getLocation());
                }
            }
        }

        Map<Location, Long> positiveLocationCounts = positiveLocationHits.stream()
                .collect(Collectors.groupingBy(location -> location, Collectors.counting()));
        Location happiestLocation = positiveLocationCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<Location, Long> negativeLocationCounts = negativeLocationHits.stream()
                .collect(Collectors.groupingBy(location -> location, Collectors.counting()));
        Location saddestLocation = negativeLocationCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<Location, Set<String>> moodsByLocation = new HashMap<>();
        for (Moment moment : moments) {
            if (moment.getLocation() == null) {
                continue;
            }
            moodsByLocation.computeIfAbsent(moment.getLocation(), key -> new HashSet<>()).addAll(moment.getMood());
        }

        Location mostEmotionallyDiverseLocation = moodsByLocation.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(null);
        return new StatsResponse(
                year,
                dominantMood,
                moodBreakdown,
                toLocationResponse(happiestLocation),
                toLocationResponse(saddestLocation),
                toLocationResponse(mostEmotionallyDiverseLocation)
        );
    }

    private LocationResponse toLocationResponse(Location location) {
        if (location == null) {
            return null;
        }
        Location masked = locationService.maskIfHome(location);
        return new LocationResponse(masked.getId(), masked.getName(), masked.getAddress(), masked.getLatitude(), masked.getLongitude(), masked.getType());
    }
}

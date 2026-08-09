package com.memorymap.memorymap.service;

import com.memorymap.memorymap.model.MoodSentiment;

import java.util.Set;

public class MoodSentimentClassifier {
    private Set<String> positiveEmotions = Set.of(
            "happy", "excited", "calm", "proud", "grateful",
            "hopeful", "confident", "loved", "inspired", "peaceful"
    );
    private Set<String> negativeEmotions = Set.of(
            "sad", "angry", "anxious", "frustrated", "lonely",
            "guilty", "ashamed", "jealous", "disappointed", "overwhelmed"
    );

    public MoodSentiment classify(String mood){
        String mood1= mood.toLowerCase().trim();
        if(positiveEmotions.contains(mood1)){
            return MoodSentiment.POSITIVE;
        }
        if(negativeEmotions.contains(mood1)){
            return MoodSentiment.NEGATIVE;
        }
        return MoodSentiment.NEUTRAL;
    }


}

package com.memorymap.memorymap.service;

import com.memorymap.memorymap.model.MoodSentiment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoodSentimentClassifierTest {

    private final MoodSentimentClassifier classifier = new MoodSentimentClassifier();

    @Test
    void knownPositiveMoodIsClassifiedAsPositive() {
        assertEquals(MoodSentiment.POSITIVE, classifier.classify("Happy"));
    }

    @Test
    void knownNegativeMoodIsClassifiedAsNegative() {
        assertEquals(MoodSentiment.NEGATIVE, classifier.classify(" sad "));
    }

    @Test
    void unrecognizedMoodIsClassifiedAsNeutral() {
        assertEquals(MoodSentiment.NEUTRAL, classifier.classify("curious"));
    }
}

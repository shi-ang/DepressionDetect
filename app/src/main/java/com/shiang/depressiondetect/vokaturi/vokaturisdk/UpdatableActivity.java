package com.shiang.depressiondetect.vokaturi.vokaturisdk;

import vokaturi.vokaturisdk.entities.EmotionProbabilities;

/**
 * Interface that enforces the updatable functionality. This method is triggered on them when new emotion data is available.
 */
public interface UpdatableActivity {
    public void updateBars(EmotionProbabilities emotionProbabilities);
}

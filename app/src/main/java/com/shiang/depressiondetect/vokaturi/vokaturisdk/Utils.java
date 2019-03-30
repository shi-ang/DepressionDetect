package com.shiang.depressiondetect.vokaturi.vokaturisdk;

import vokaturi.vokaturisdk.entities.EmotionProbabilities;

/**
 * Processing methods
 */
public class Utils {

    /**
     * Number of smoothing frames
     */
    private float numberOfSmoothingFrames = 4.f;
    /**
     * Object containing the sum of multiple emotion values. Used for calculating avg values
     */
    private EmotionProbabilities sum = new EmotionProbabilities();
    /**
     * Previous emotion object
     */
    private EmotionProbabilities history = new EmotionProbabilities();
    /**
     * The instance of this utils object
     */
    private static Utils instance;

    /**
     * Returns the singleton instance
     * @return The singleton instance of the Utils
     */
    public static Utils getInstance()
    {
        if(instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    /**
     * Returns an average emotion object
     * @return An average emotion object
     */
    public EmotionProbabilities getCurrentAverageEmotion() {
        EmotionProbabilities currentSumEmotion = sum;
        EmotionProbabilities avgEmotion = new EmotionProbabilities();
        avgEmotion.happiness = currentSumEmotion.happiness / numberOfSmoothingFrames;
        avgEmotion.sadness = currentSumEmotion.sadness / numberOfSmoothingFrames;
        avgEmotion.anger = currentSumEmotion.anger / numberOfSmoothingFrames;
        avgEmotion.neutrality = currentSumEmotion.neutrality / numberOfSmoothingFrames;
        avgEmotion.fear = currentSumEmotion.fear / numberOfSmoothingFrames;

        return avgEmotion;
    }

    /**
     * Returns the Y value for the graph point of a given emotion
     * @param emotion The emotion object that needs to be evaluated
     * @return the Y value for the graph point of a given emotion
     */
    public float getEmotionPoint(EmotionProbabilities emotion) {
        float value = 0.f;
        float speed = 0.01f;
        if( emotion.numberOfFramesAnalyzed > 0 ){
            float factor1 = 1.f - 1.f / numberOfSmoothingFrames;
            sum.happiness = factor1 * sum.happiness + emotion.happiness;
            sum.sadness = factor1 * sum.sadness + emotion.sadness;
            sum.anger = factor1 * sum.anger + emotion.anger;
            sum.neutrality = factor1 * sum.neutrality + emotion.neutrality;
            sum.fear = factor1 * sum.fear + emotion.fear;
        }

        if( emotion.numberOfFramesAnalyzed > 0 ) {
            float localSpeed = speed * emotion.numberOfFramesAnalyzed;
            history.happiness = (1.f - localSpeed) * history.happiness + localSpeed * emotion.happiness;

            if (history.happiness < -1.0f) {history.happiness = -1.0;}
            if (history.happiness > 1.0f) {history.happiness = 1.f;}

            history.sadness = (1.f - localSpeed) * history.sadness + localSpeed * history.sadness;
            if (history.sadness < -1.0f) {history.sadness = -1.0;}
            if (history.sadness > 1.0f) {history.sadness = 1.f;}


        }

        value = (float) (history.happiness - history.sadness);
        return value;
    }

}

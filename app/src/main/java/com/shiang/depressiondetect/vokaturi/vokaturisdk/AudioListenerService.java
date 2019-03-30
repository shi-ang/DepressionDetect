package com.shiang.depressiondetect.vokaturi.vokaturisdk;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;

import vokaturi.vokaturisdk.entities.EmotionProbabilities;
import vokaturi.vokaturisdk.entities.Voice;

/**
 * Singleton service used to retrieve microphone data and update existing activities accordingly
 */
public class AudioListenerService
{
    /**
     * Array list of existing updatable activities - activities that will receive emotion data
     */
    public ArrayList<UpdatableActivity> updatableActivities = new ArrayList<UpdatableActivity>();
    /**
     * Vokaturi voice data object
     */
    private Voice voice = new Voice(44100.f, 441000);
    /**
     * The audio sample rate. We have an array because some devices might not be able to sample 44100
     */
    private final static int[] sampleRates = {44100};
    /**
     * The audio recorder object retrieveing the audio data
     */
    private AudioRecord audioRecorder = null;
    /**
     * Audio buffer collected from the microphone
     */
    private float[] buffer;
    /**
     * The sample rate of the audio recording
     */
    public int sRate;
    /**
     * The size of the audio buffer sampled
     */
    private int bufferSize;
    /**
     * Number of sample to be analized at once in a voice
     */
    private int numOfSamples = 3;

    /**
     * Current number of samples added
     */
    private int currentNumOfSamples = 0;

    /**
     * Updates the activities with the available emotion data
     * @param emotionProbabilities Object containing emotion data
     */
    public void updateActivities( EmotionProbabilities emotionProbabilities ) {

        for( int i = 0; i < updatableActivities.size(); i++ ) {

            UpdatableActivity activity = updatableActivities.get(i);
            activity.updateBars( emotionProbabilities );
        }
    }

    /**
     * Returns the singleton instance of this audio listener service
     * @return Singleton instance of AudioListenerService
     */
    public static AudioListenerService getInstance()
    {
        AudioListenerService result = null;

        int i=0;
        do
        {
            result = new AudioListenerService(true,
                    MediaRecorder.AudioSource.MIC,
                    sampleRates[i],
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT);

        } while(++i<sampleRates.length);

        return result;
    }

    /**
     * Triggered when audio data is available from the microphone
     */
    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener()
    {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onPeriodicNotification(AudioRecord recorder)
        {
            audioRecorder.read(buffer, 0, buffer.length, AudioRecord.READ_BLOCKING);

            try {
                voice.fill(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentNumOfSamples++;

            if (currentNumOfSamples % numOfSamples == 0) {
                try {
                    new ProcessAudioDataTask(voice).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentNumOfSamples = 0;
            }

        }

        public void onMarkerReached(AudioRecord recorder)
        {
            // NOT USED
        }
    };

    /**
     * Initialier method of the audio service
     * @param uncompressed Wether the audio data is compressed or uncomressed
     * @param audioSource The audio source (microphone identifier)
     * @param sampleRate The audio sample rate required
     * @param channelConfig The configuration of the audio channels
     * @param audioFormat The audio format of the samples
     */
    public AudioListenerService(boolean uncompressed, int audioSource, int sampleRate, int channelConfig, int audioFormat)
    {
        try
        {
            sRate = sampleRate;
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            buffer = new float[bufferSize];

            audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);

            if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
                throw new Exception("AudioRecord initialization failed");
            audioRecorder.setRecordPositionUpdateListener(updateListener);
            audioRecorder.setPositionNotificationPeriod(bufferSize);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Starts recording the audio data
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public synchronized void start()
    {
        audioRecorder.startRecording();
        audioRecorder.read(buffer, 0, buffer.length, AudioRecord.READ_BLOCKING);
    }

    /**
     * Asynchronous task for processing the audio data via the sdk. We do this to avoid prcessing on the main thread and freezing the application
     */
    public class ProcessAudioDataTask extends AsyncTask<EmotionProbabilities, Void, EmotionProbabilities> {

        private Voice processedVoice;

        /**
         * Task initializer
         * @param voice The voice object that processes the data
         */
        public ProcessAudioDataTask( Voice voice ) {
            processedVoice = voice;
        }

        /**
         * Executes the background task
         * @param params The task parameter. This is not really needed but the signature enforces it
         * @return null if there's a failure. Emotion data if successfull
         */
        @Override
        protected EmotionProbabilities doInBackground(EmotionProbabilities... params) {
            try {
                return processedVoice.extract();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Triggered when the probabilities data is ready to be sent to the activities
         * @param probabilities The emotion probability object
         */
        @Override
        protected void onPostExecute(EmotionProbabilities probabilities) {
            if(probabilities.isValid) {
                updateActivities( probabilities );
            }
        }

        /**
         * Triggered right before execution
         */
        @Override
        protected void onPreExecute() {
        }

        /**
         * Triggered when there's a progress update. We're not using this
         * @param values Not used
         */
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    /**
     * Destroys the voice objets for cleanup
     */
    public void cleanup() {
        voice.destroy();
    }
}
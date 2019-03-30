package com.shiang.depressiondetect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.shiang.depressiondetect.vokaturi.vokaturisdk.AudioListenerService;
import com.shiang.depressiondetect.vokaturi.vokaturisdk.UpdatableActivity;

import vokaturi.vokaturisdk.entities.EmotionProbabilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is a very bare sample app to demonstrate the usage of the CameraDetector object from Affectiva.
 * It displays statistics on frames per second, percentage of time a face was detected, and the user's smile score.
 *
 * The app shows off the maneuverability of the SDK by allowing the user to start and stop the SDK and also hide the camera SurfaceView.
 *
 * For use with SDK 2.02
 */
public class MainActivity extends Activity implements Detector.ImageListener, CameraDetector.CameraEventListener, UpdatableActivity {

    final String LOG_TAG = "DepressionDetect";

    private AudioListenerService audioListenerService = AudioListenerService.getInstance();

    FloatingActionButton startSDKButton;
    FloatingActionButton cameraButton;
    FloatingActionButton settingButton;
    Button surfaceViewVisibilityButton;

    TextView joyTextView;
    TextView sadnessTextView;
    TextView angerTextView;
    TextView contemptTextView;
    TextView disgustTextView;
    TextView engagementTextView;
    TextView fearTextView;
    TextView surpriseTextView;
    TextView valenceTextView;

    TextView ageTextView;
    TextView ethnicityTextView;

    TextView audioHappinessTextView;
    TextView audioNeutralityTextView;
    TextView audioAngerTextView;
    TextView audioSadnessTextView;
    TextView audioFearTextView;

    TextView[] metricNames;
    TextView[] metricValues;

    GraphView graphView;


    SurfaceView cameraPreview;

    boolean isCameraBack = false;
    boolean isSDKStarted = false;
    private boolean cameraPermissionsAvailable = false;
    private boolean audioPermissionAvailable = false;
    private boolean storagePermissionAvailable = false;
    private boolean isFrontFacingCameraDetected = true;
    private boolean isBackFacingCameraDetected = true;
    private static final int CAMERA_PERMISSIONS_REQUEST = 42;
    private static final int AUDIO_PERMISSIONS_REQUEST = 5;
    private static final int EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 73;
    public static final int NUM_METRICS_DESPLAYED = 6;

    RelativeLayout mainLayout;

    CameraDetector detector;
    CameraDetector.CameraType cameraType;

    int previewWidth = 0;
    int previewHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*joyTextView = findViewById(R.id.joy_textview);
        sadnessTextView = findViewById(R.id.sadness_textview);
        angerTextView = findViewById(R.id.anger_textView);
        contemptTextView = findViewById(R.id.contempt_textView);
        disgustTextView = findViewById(R.id.disgust_textView);
        engagementTextView = findViewById(R.id.engagement_textView);
        fearTextView = findViewById(R.id.fear_textView);
        surpriseTextView = findViewById(R.id.surprise_textView);
        valenceTextView = findViewById(R.id.valence_textView);*/

        ageTextView = findViewById(R.id.age_textview);
        ethnicityTextView = findViewById(R.id.ethnicity_textview);

        audioHappinessTextView = findViewById(R.id.audio_happiness_textview);
        audioNeutralityTextView = findViewById(R.id.audio_neutrality_textview);
        audioAngerTextView = findViewById(R.id.audio_anger_textview);
        audioSadnessTextView = findViewById(R.id.audio_sadness_textview);
        audioFearTextView = findViewById(R.id.audio_fear_textview);

        graphView = findViewById(R.id.graph_view);

        metricNames = new TextView[NUM_METRICS_DESPLAYED];
        metricNames[0] = findViewById(R.id.metric_name_0);
        metricNames[1] = findViewById(R.id.metric_name_1);
        metricNames[2] = findViewById(R.id.metric_name_2);
        metricNames[3] = findViewById(R.id.metric_name_3);
        metricNames[4] = findViewById(R.id.metric_name_4);
        metricNames[5] = findViewById(R.id.metric_name_5);

        metricValues = new TextView[NUM_METRICS_DESPLAYED];
        metricValues[0] = findViewById(R.id.metric_value_0);
        metricValues[1] = findViewById(R.id.metric_value_1);
        metricValues[2] = findViewById(R.id.metric_value_2);
        metricValues[3] = findViewById(R.id.metric_value_3);
        metricValues[4] = findViewById(R.id.metric_value_4);
        metricValues[5] = findViewById(R.id.metric_value_5);


        try {
            audioListenerService.updatableActivities.add(this);
            audioListenerService.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        startSDKButton = findViewById(R.id.sdk_start_button);
        startSDKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSDKStarted) {
                    isSDKStarted = false;
                    stopDetector();
                } else {
                    isSDKStarted = true;
                    startDetector();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd"); // 2019_01_01
                    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss zzzz"); // 2019.01.01 at 12:01:01 Mountain Daylight Time
                    Date now = new Date();
                    String fileName = dateFormat.format(now) + ".txt";
                    String fileContent = timeFormat.format(now);
                    saveDataOnExternalStorage( getApplicationContext(), fileName, fileContent);
                    Toast.makeText(getApplicationContext(), "Text file has been created", Toast.LENGTH_LONG).show();

                    checkForStoragePermissions();
                }
            }
        });

        cameraType = CameraDetector.CameraType.CAMERA_FRONT;
        cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switchCamera(cameraType == CameraDetector.CameraType.CAMERA_FRONT? CameraDetector.CameraType.CAMERA_BACK : CameraDetector.CameraType.CAMERA_FRONT);
            }
        });

        settingButton = findViewById(R.id.setting_button);
        settingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingHeadersActivity.class);
                startActivity(intent);
            }
        });

        //We create a custom SurfaceView that resizes itself to match the aspect ratio of the incoming camera frames
        mainLayout = findViewById(R.id.main_layout);
        cameraPreview = new SurfaceView(this) {
            @Override
            public void onMeasure(int widthSpec, int heightSpec) {
                int measureWidth = MeasureSpec.getSize(widthSpec);
                int measureHeight = MeasureSpec.getSize(heightSpec);
                int width;
                int height;
                if (previewHeight == 0 || previewWidth == 0) {
                    width = measureWidth;
                    height = measureHeight;
                } else {
                    float viewAspectRatio = (float)measureWidth/measureHeight;
                    float cameraPreviewAspectRatio = (float) previewWidth/previewHeight;

                    if (cameraPreviewAspectRatio > viewAspectRatio) {
                        width = measureWidth;
                        height =(int) (measureWidth / cameraPreviewAspectRatio);
                    } else {
                        width = (int) (measureHeight * cameraPreviewAspectRatio);
                        height = measureHeight;
                    }
                }
                setMeasuredDimension(width,height);
            }
        };
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
        cameraPreview.setLayoutParams(params);
        mainLayout.addView(cameraPreview,0);

        //Marked!!! Couldn't change the order here. Has some problem with function checkForAudioPermissions(). Needs to be solved!
        checkForCameraPermissions();
        checkForAudioPermissions();
        //checkForStoragePermissions();


        detector = new CameraDetector(this, cameraType, cameraPreview);
        detector.setDetectAllEmotions(true);
        detector.setDetectAge(true);
        detector.setDetectEthnicity(true);
        detector.setImageListener(this);
        detector.setOnCameraEventListener(this);
    }


    private void checkForCameraPermissions() {
        cameraPermissionsAvailable =
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionsAvailable) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showPermissionExplanationDialog(CAMERA_PERMISSIONS_REQUEST);
            } else {
                // No explanation needed, we can request the permission.
                requestCameraPermissions();
            }
        }
    }

    private void requestCameraPermissions() {
        if (!cameraPermissionsAvailable) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSIONS_REQUEST);

            // CAMERA_PERMISSIONS_REQUEST is an app-defined int constant that must be between 0 and 255.
            // The callback method gets the result of the request.
        }
    }

    private void checkForAudioPermissions() {
        audioPermissionAvailable =
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionsAvailable) {              // Marked!!! Couldn't use audioPermissionAvailable at here . Have no idea. Need to be fixed!
            showPermissionExplanationDialog(AUDIO_PERMISSIONS_REQUEST);
        } else {
            requestAudioPermissions();
        }
    }

    private void requestAudioPermissions() {
        if (!audioPermissionAvailable) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSIONS_REQUEST
            );
        }
    }

    private void checkForStoragePermissions(){
        storagePermissionAvailable =
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!storagePermissionAvailable) {
            showPermissionExplanationDialog(EXTERNAL_STORAGE_PERMISSIONS_REQUEST);
        } else {
            requestStoragePermissions();
        }
    }

    private void requestStoragePermissions(){
        if (!storagePermissionAvailable) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_PERMISSIONS_REQUEST
            );
        }
    }

    private void showPermissionExplanationDialog(int requestCode) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set title
        alertDialogBuilder.setTitle(getResources().getString(R.string.insufficient_permissions));

        // set dialog message
        if (requestCode == CAMERA_PERMISSIONS_REQUEST) {
            alertDialogBuilder
                    .setMessage(getResources().getString(R.string.permissions_camera_needed_explanation))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.understood), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            requestCameraPermissions();
                        }
                    });
        } else if (requestCode == AUDIO_PERMISSIONS_REQUEST) {
            alertDialogBuilder
                    .setMessage(getResources().getString(R.string.permissions_audio_needed_explanation))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.understood), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            requestAudioPermissions();
                        }
                    });
        } else if (requestCode == EXTERNAL_STORAGE_PERMISSIONS_REQUEST) {
            alertDialogBuilder
                    .setMessage(getResources().getString(R.string.permissions_storage_needed_explanation))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.understood), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            requestStoragePermissions();
                        }
                    });
        }

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        restoreApplicationSettings();
        audioListenerService.start();
        if (isSDKStarted) {
            startDetector();
        }
    }

    private void restoreApplicationSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDetector();
        audioListenerService.cleanup();
    }

    void startDetector() {
        if (!detector.isRunning()) {
            try {
                detector.start();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    void stopDetector() {
        if (detector.isRunning()) {
            try {
                detector.stop();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    void switchCamera(CameraDetector.CameraType type) {
        if(cameraType != type) {
            switch (type) {
                case CAMERA_BACK:
                    cameraType = CameraDetector.CameraType.CAMERA_BACK;
                    break;
                case CAMERA_FRONT:
                    cameraType = CameraDetector.CameraType.CAMERA_FRONT;
                    break;
                default:
                    Log.e(LOG_TAG, "Unkown camera type selected");
            }
        }

        detector.setCameraType(cameraType);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onImageResults(List<Face> list, Frame frame, float v) {
        if (list == null){
            return;
        }

        if (list.size() <= 0) {
            ageTextView.setText(R.string.noFace);
            ethnicityTextView.setText("");
        } else {
            Face face = list.get(0);
            //joyTextView.setText(String.format("JOY\n%.2f",face.emotions.getJoy()));
            //sadnessTextView.setText(String.format("SADNESS\n%.2f",face.emotions.getSadness()));
            //angerTextView.setText(String.format("ANGER\n%.2f",face.emotions.getAnger()));
            //contemptTextView.setText(String.format("CONTEMPT\n%.2f",face.emotions.getContempt()));
            //disgustTextView.setText(String.format("DISGUST\n%.2f",face.emotions.getDisgust()));
            //engagementTextView.setText(String.format("ENGAGEMENT\n%.2f",face.emotions.getEngagement()));
            //fearTextView.setText(String.format("FEAR\n%.2f",face.emotions.getFear()));
            //surpriseTextView.setText(String.format("SURPRISE\n%.2f",face.emotions.getSurprise()));
            //valenceTextView.setText(String.format("VALENCE\n%.2f",face.emotions.getValence()));

            for(TextView textView: metricValues){
                    updateMetricValue(textView, face);
            }

            switch (face.appearance.getAge()) {
                case AGE_UNKNOWN:
                    ageTextView.setText("");
                    break;
                case AGE_UNDER_18:
                    ageTextView.setText(R.string.age_under_18);
                    break;
                case AGE_18_24:
                    ageTextView.setText(R.string.age_18_24);
                    break;
                case AGE_25_34:
                    ageTextView.setText(R.string.age_25_34);
                    break;
                case AGE_35_44:
                    ageTextView.setText(R.string.age_35_44);
                    break;
                case AGE_45_54:
                    ageTextView.setText(R.string.age_45_54);
                    break;
                case AGE_55_64:
                    ageTextView.setText(R.string.age_55_64);
                    break;
                case AGE_65_PLUS:
                    ageTextView.setText(R.string.age_over_64);
                    break;
            }

            switch (face.appearance.getEthnicity()) {
                case UNKNOWN:
                    ethnicityTextView.setText("");
                    break;
                case CAUCASIAN:
                    ethnicityTextView.setText(R.string.ethnicity_caucasian);
                    break;
                case BLACK_AFRICAN:
                    ethnicityTextView.setText(R.string.ethnicity_black_african);
                    break;
                case EAST_ASIAN:
                    ethnicityTextView.setText(R.string.ethnicity_east_asian);
                    break;
                case SOUTH_ASIAN:
                    ethnicityTextView.setText(R.string.ethnicity_south_asian);
                    break;
                case HISPANIC:
                    ethnicityTextView.setText(R.string.ethnicity_hispanic);
                    break;
            }

            ArrayList<Float> happinessValues = graphView.facialValues;

            if (happinessValues.size() > GraphView.MaxNumberOfGraphPoints) {
                happinessValues.remove(0);
            }

            float hValues = (float) face.emotions.getJoy();
            happinessValues.add(hValues);

            if (graphView != null) {
                graphView.invalidate();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
            Date now = new Date();
            String fileNameString = dateFormat.format(now)+".txt";  // to create text file like 2019_01_01.txt
            String fileContentString = String.format("Ethnicity \t\t %s \t\t", face.appearance.getEthnicity()) +
                    String.format("Age \t\t %s \t\t",face.appearance.getAge()) +
                    String.format("JOY \t\t %.2f \t\t",face.emotions.getJoy()) +
                    String.format("SADNESS \t\t %.2f \t\t",face.emotions.getSadness()) +
                    String.format("ANGER \t\t %.2f \t\t",face.emotions.getAnger()) +
                    String.format("CONTEMPT \t\t %.2f \t\t",face.emotions.getContempt()) +
                    String.format("DISGUST \t\t %.2f \t\t",face.emotions.getDisgust()) +
                    String.format("ENGAGEMENT \t\t %.2f \t\t",face.emotions.getEngagement()) +
                    String.format("FEAR \t\t %.2f \t\t",face.emotions.getFear()) +
                    String.format("SURPRISE \t\t %.2f \t\t",face.emotions.getSurprise()) +
                    String.format("VALENCE \t\t %.2f \t\t",face.emotions.getValence());
            saveDataOnExternalStorage(this, fileNameString, fileContentString);
        }
    }

    /**
     *
     * @param textView
     * @param face
     */
    void updateMetricValue(TextView textView, Face face){

    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onCameraSizeSelected(int width, int height, Frame.ROTATE rotate) {
        if (rotate == Frame.ROTATE.BY_90_CCW || rotate == Frame.ROTATE.BY_90_CW) {
            previewWidth = height;
            previewHeight = width;
        } else {
            previewHeight = height;
            previewWidth = width;
        }
        cameraPreview.requestLayout();
    }

    @Override
    public void updateBars(EmotionProbabilities emotionProbabilities) {
        audioHappinessTextView.setText(String.format("Happiness\n%.2f",(float)emotionProbabilities.happiness));
        audioNeutralityTextView.setText(String.format("Neutrality\n%.2f",(float)emotionProbabilities.neutrality));
        audioAngerTextView.setText(String.format("Anger\n%.2f",(float)emotionProbabilities.anger));
        audioSadnessTextView.setText(String.format("Sadness\n%.2f",(float)emotionProbabilities.sadness));
        audioFearTextView.setText(String.format("Fear\n%.2f",(float)emotionProbabilities.fear));

        ArrayList<Float> happinessValues = graphView.voiceValues;

        if (happinessValues.size() > GraphView.MaxNumberOfGraphPoints) {
            happinessValues.remove(0);
        }

        float hValues = (float) emotionProbabilities.happiness;
        happinessValues.add(hValues);

        if (graphView != null) {
            graphView.invalidate();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Date now = new Date();
        String fileNameString = dateFormat.format(now)+".txt";  // to create text file like 2019_01_01.txt
        String fileContentString = String.format("HAPPINESS \t\t %.2f \t\t ",(float)emotionProbabilities.happiness) +
                String.format("NEUTRALITY \t\t %.2f \t\t ",(float)emotionProbabilities.neutrality) +
                String.format("ANGER \t\t %.2f \t\t ",(float)emotionProbabilities.anger) +
                String.format("SADNESS \t\t %.2f \t\t ",(float)emotionProbabilities.sadness) +
                String.format("FEAR \t\t %.2f \t\t ",(float)emotionProbabilities.fear);
        saveDataOnExternalStorage(this, fileNameString, fileContentString);
    }


    /**
     * This function is to create and write a file and save it to the public external storage.
     * @param context context of the activity
     * @param sFileName The file name that will be saved to.
     * @param sBody The content that will be saved
     */
    public void saveDataOnExternalStorage(Context context, String sFileName, String sBody){
        if (!isExternalStorageWritable()){
            Toast.makeText(context, "External Storage Not Writable", Toast.LENGTH_LONG).show();
        }

        try {
            // to create folder in external storage
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"Data");
            // if folder does not exist, create it
            if (!root.exists()){
                root.mkdirs();
            }

            File file = new File(root, sFileName);
            // to write on the file
            FileWriter writer = new FileWriter(file, true);
            writer.append(sBody);
            writer.append(System.getProperty("line.separator"));
            writer.append(System.getProperty("line.separator"));
            /**
             * System.getProperty("line.separator") returns the OS dependent line separator.
             *
             * On Windows it returns "\r\n", on Unix "\n", on MacOS "\r"
             * So if you want to generate a file with line endings for the current operating systems
             * use System.getProperty("line.separator") or write using a PrintWriter.
             */
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
   }

    public boolean isExternalStorageWritable(){
        String storageState = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(storageState);
    }
}

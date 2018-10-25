package com.shiang.depressiondetect;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;

import vokaturi.vokaturisdk.entities.EmotionProbabilities;

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

    final String LOG_TAG = "shiangMessage";

    private AudioListenerService audioListenerService = AudioListenerService.getInstance();

    Button startSDKButton;
    ToggleButton toggleButton;
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


    SurfaceView cameraPreview;

    boolean isCameraBack = false;
    boolean isSDKStarted = false;
    private boolean cameraPermissionsAvailable = false;
    private boolean audioPermissionAvailable = false;
    private boolean isFrontFacingCameraDetected = true;
    private boolean isBackFacingCameraDetected = true;
    private static final int CAMERA_PERMISSIONS_REQUEST = 42;
    private static final int AUDIO_PERMISSIONS_REQUEST = 5;

    RelativeLayout mainLayout;

    CameraDetector detector;

    int previewWidth = 0;
    int previewHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joyTextView = (TextView) findViewById(R.id.joy_textview);
        sadnessTextView = (TextView) findViewById(R.id.sadness_textview);
        angerTextView = (TextView) findViewById(R.id.anger_textView);
        contemptTextView = (TextView) findViewById(R.id.contempt_textView);
        disgustTextView = (TextView) findViewById(R.id.disgust_textView);
        engagementTextView = (TextView) findViewById(R.id.engagement_textView);
        fearTextView = (TextView) findViewById(R.id.fear_textView);
        surpriseTextView = (TextView) findViewById(R.id.surprise_textView);
        valenceTextView = (TextView) findViewById(R.id.valence_textView);

        ageTextView = (TextView) findViewById(R.id.age_textview);
        ethnicityTextView = (TextView) findViewById(R.id.ethnicity_textview);

        audioHappinessTextView = (TextView) findViewById(R.id.audio_happiness_textview);
        audioNeutralityTextView = (TextView) findViewById(R.id.audio_neutrality_textview);
        audioAngerTextView = (TextView) findViewById(R.id.audio_anger_textview);
        audioSadnessTextView = (TextView) findViewById(R.id.audio_sadness_textview);
        audioFearTextView = (TextView) findViewById(R.id.audio_fear_textview);

        try {
            audioListenerService.updatableActivities.add(this);
            audioListenerService.start();;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        startSDKButton = (Button) findViewById(R.id.sdk_start_button);
        startSDKButton.setText("Start Camera");
        startSDKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSDKStarted) {
                    isSDKStarted = false;
                    stopDetector();
                    startSDKButton.setText("Start Camera");
                } else {
                    isSDKStarted = true;
                    startDetector();
                    startSDKButton.setText("Stop Camera");
                }
            }
        });

        toggleButton = (ToggleButton) findViewById(R.id.front_back_toggle_button);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCameraBack = isChecked;
                switchCamera(isCameraBack? CameraDetector.CameraType.CAMERA_BACK : CameraDetector.CameraType.CAMERA_FRONT);
            }
        });

        //We create a custom SurfaceView that resizes itself to match the aspect ratio of the incoming camera frames
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
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
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        cameraPreview.setLayoutParams(params);
        mainLayout.addView(cameraPreview,0);



        checkForCameraPermissions();
        checkForAudioPermissions();


        detector = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT, cameraPreview);
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

        if (!cameraPermissionsAvailable) {
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
        }

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isSDKStarted) {
            startDetector();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDetector();
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
        detector.setCameraType(type);
    }

    @Override
    public void onImageResults(List<Face> list, Frame frame, float v) {
        if (list == null)
            return;
        if (list.size() == 0) {
            joyTextView.setText("NO FACE");
            sadnessTextView.setText("");
            angerTextView.setText("");
            contemptTextView.setText("");
            disgustTextView.setText("");
            engagementTextView.setText("");
            fearTextView.setText("");
            surpriseTextView.setText("");
            valenceTextView.setText("");
            ageTextView.setText("");
            ethnicityTextView.setText("");
        } else {
            Face face = list.get(0);
            joyTextView.setText(String.format("JOY\n%.2f",face.emotions.getJoy()));
            sadnessTextView.setText(String.format("SADNESS\n%.2f",face.emotions.getSadness()));
            angerTextView.setText(String.format("ANGER\n%.2f",face.emotions.getAnger()));
            contemptTextView.setText(String.format("CONTEMPT\n%.2f",face.emotions.getContempt()));
            disgustTextView.setText(String.format("DISGUST\n%.2f",face.emotions.getDisgust()));
            engagementTextView.setText(String.format("ENGAGEMENT\n%.2f",face.emotions.getEngagement()));
            fearTextView.setText(String.format("FEAR\n%.2f",face.emotions.getFear()));
            surpriseTextView.setText(String.format("SURPRISE\n%.2f",face.emotions.getSurprise()));
            valenceTextView.setText(String.format("VALENCE\n%.2f",face.emotions.getValence()));
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

        }
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
    }
}

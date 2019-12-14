package com.shiang.depressiondetect;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MetricsSelectionFragment extends Fragment implements View.OnClickListener{

    final static String LOG_TAG = "DepressionDetect_MetricsSelectionFragment";

    SharedPreferences sharedPreferences;
    //static ArrayList<String> metricsContainer = new ArrayList<>();
    Map<String, Integer> metricsContainer = new HashMap<>();
    public String[] emotionNames = {"happy","anger","fear","disgust","sadness","surprise","contempt","engagement","valence"};

    Button clearAllButton;
    TextView metricsChooserTextView;

    CheckBox[] checkBoxArray = new CheckBox[10];
    ArrayList<CheckBox> allCheckBox = new ArrayList<>(10);
    CheckBox joyCheckBox;
    CheckBox angerCheckBox;
    CheckBox sadnessCheckBox;

    CheckBox smileCheckBox;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentLayout = inflater.inflate(R.layout.fragment_setting, container, false);
        initialUI(fragmentLayout);
        restoreSettings(savedInstanceState);
        return fragmentLayout;
    }

    /**
     * Initialized UI
     * @param fragmentLayout pass the view object created in onCreateView
     */
    void initialUI(View fragmentLayout){
        clearAllButton = fragmentLayout.findViewById(R.id.clear_all_button);
        metricsChooserTextView = fragmentLayout.findViewById(R.id.metrics_chooser_textview);

        checkBoxArray[0] = fragmentLayout.findViewById(R.id.joy_checkbox);
        checkBoxArray[1] = fragmentLayout.findViewById(R.id.anger_checkbox);
        checkBoxArray[2] = fragmentLayout.findViewById(R.id.fear_checkbox);
        checkBoxArray[3] = fragmentLayout.findViewById(R.id.disgust_checkbox);
        checkBoxArray[4] = fragmentLayout.findViewById(R.id.sadness_checkbox);
        checkBoxArray[5] = fragmentLayout.findViewById(R.id.surprise_checkbox);
        checkBoxArray[6] = fragmentLayout.findViewById(R.id.contempt_checkbox);
        checkBoxArray[7] = fragmentLayout.findViewById(R.id.engagement_checkbox);
        checkBoxArray[8] = fragmentLayout.findViewById(R.id.valence_checkbox);

        checkBoxArray[0].setOnClickListener(this);
        checkBoxArray[1].setOnClickListener(this);
        checkBoxArray[2].setOnClickListener(this);
        checkBoxArray[3].setOnClickListener(this);
        checkBoxArray[4].setOnClickListener(this);
        checkBoxArray[5].setOnClickListener(this);
        checkBoxArray[6].setOnClickListener(this);
        checkBoxArray[7].setOnClickListener(this);
        checkBoxArray[8].setOnClickListener(this);

        //joyCheckBox = fragmentLayout.findViewById(R.id.joy_checkbox);
        //angerCheckBox = fragmentLayout.findViewById(R.id.anger_checkbox);
        //sadnessCheckBox =fragmentLayout.findViewById(R.id.sadness_checkbox);

        smileCheckBox = fragmentLayout.findViewById(R.id.smile_checkbox);

        clearAllButton.setOnClickListener(this);

        //joyCheckBox.setOnClickListener(this);
        //angerCheckBox.setOnClickListener(this);
        //sadnessCheckBox.setOnClickListener(this);

        smileCheckBox.setOnClickListener(this);

        //didn't finish here
        for (int i = 0 ; i < 9; i++ ){
            if (checkBoxArray[i].isChecked()) {
                metricsContainer.put(emotionNames[i],checkBoxArray[i].getId());
            }

        }


    }

    void saveSettings(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int n = 0; n < metricsContainer.size(); n++){
            editor.putString(String.format(Locale.US, "Metrics_num_%d",n), emotionNames[n]);
        }

        editor.apply();
    }

    void restoreSettings(Bundle bundle){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    void clearAllItems(){
        for (int i = 0; i < 9; i++){
            checkBoxArray[i].setChecked(false);
        }
        metricsContainer.clear();
        SharedPreferences.Editor editorClear = sharedPreferences.edit();
        editorClear.clear();
        editorClear.apply();
    }

    boolean isOverflow(){
        return metricsContainer.size() > 6;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // The following is the answer for implement onClick handler in a fragment
    // https://stackoverflow.com/questions/35533905/how-to-handle-onclick-in-fragments?rq=1
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clear_all_button:
                clearAllItems();
                break;
            case R.id.joy_checkbox:
                if (checkBoxArray[0].isChecked()){
                    metricsContainer.put(emotionNames[0],v.getId());
                } else {
                    metricsContainer.remove(emotionNames[0]);
                }
                break;
            case R.id.anger_checkbox:
                break;
            case R.id.fear_checkbox:
                break;
            case R.id.disgust_checkbox:
                break;
            case R.id.sadness_checkbox:
                break;
            case R.id.surprise_checkbox:
                break;
            case R.id.contempt_checkbox:
                break;
            case R.id.engagement_checkbox:
                break;
            case R.id.valence_checkbox:
                break;


            case R.id.smile_checkbox:
                break;
        }
        if (metricsContainer.size() > 1) {
            metricsChooserTextView.setText(String.format(Locale.US,"%d emotions chosen", metricsContainer.size()));
        } else {
            metricsChooserTextView.setText(String.format(Locale.US,"%d emotion chosen", metricsContainer.size()));
        }
        if (isOverflow()){
            //textView to red color
            metricsChooserTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
        } else {
            //textView normally display
            metricsChooserTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }
}

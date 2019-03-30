package com.shiang.depressiondetect;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import java.util.List;

public class SettingActivity extends PreferenceActivity {

    final static String LOG_TAG= "SettingActivity";

    private int numberOfSelectedItems = 0;

    Button clearAllButton;
    TextView metricsChooserTextView;


    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_setting);
        //addPreferencesFromResource(R.xml.preference_metrics);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MainSettingsFragment()).commit();

        metricsChooserTextView = findViewById(R.id.metrics_chooser_textview);
        clearAllButton = findViewById(R.id.clear_all_button);
    }

    public void clearAllItem(View view) {

        metricsChooserTextView.setText("0 metric chosen");
        Toast.makeText(this, "Clear all selected items", Toast.LENGTH_SHORT).show();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        //editor.commit();
    }

    public static class MainSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_metrics);
        }
    }

    public static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener(){
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }
    };
}

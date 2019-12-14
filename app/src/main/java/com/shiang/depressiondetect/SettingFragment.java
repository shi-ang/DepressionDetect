package com.shiang.depressiondetect;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //super.onCreatePreferences(bundle, s);
        addPreferencesFromResource(R.xml.preference_metrics);
    }
}

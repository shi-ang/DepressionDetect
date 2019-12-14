package com.shiang.depressiondetect;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v14.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import java.util.List;

public class SettingHeadersActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setIcon(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.transparent_overlay)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingFragment.class.getName().equals(fragmentName) || MetricsSeletionFragment2.class.getName().equals(fragmentName);
        //return MetricsSelectionFragment.class.getName().equals(fragmentName);
    }

}

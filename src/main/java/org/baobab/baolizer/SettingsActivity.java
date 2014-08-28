package org.baobab.baolizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

public class SettingsActivity extends PreferenceActivity {

    private ListPreference mapSetting;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mapSetting = (ListPreference) findPreference("map");
        mapSetting.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (((String) newValue).equals("Google Maps")) {
                            switch (isGooglePlayServicesAvailable(SettingsActivity.this)) {
                                case SUCCESS:
                                    mapSetting.setTitle("Google Maps");
                                    return true;
                                case SERVICE_MISSING:
                                case SERVICE_DISABLED:
                                case SERVICE_VERSION_UPDATE_REQUIRED:
                                    getErrorDialog(isGooglePlayServicesAvailable(SettingsActivity.this),
                                            SettingsActivity.this, 0).show();
                                    return false;
                            }
                        } else {
                            new AlertDialog.Builder(SettingsActivity.this)
                                    .setTitle(R.string.uninstall_title)
                                    .setMessage(R.string.uninstall_msg)
                                    .setPositiveButton(R.string.uninstall_btn_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                                                    Uri.parse("package:com.google.android.gms")));
                                        }
                                    })
                                    .setNegativeButton(R.string.uninstall_btn_not, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                            mapSetting.setTitle("Mapbox Openstreetmap");
                        }
                        return true;
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapSetting.setTitle(prefs.getString("map", ""));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isGooglePlayServicesAvailable(SettingsActivity.this) == SUCCESS) {
            prefs.edit().putString("map", "Google Maps").commit();
        }
    }

}

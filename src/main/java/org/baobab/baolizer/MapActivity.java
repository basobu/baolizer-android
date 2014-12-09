
package org.baobab.baolizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import ch.hsr.geohash.GeoHash;

import static com.google.android.gms.common.ConnectionResult.SUCCESS;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

public class MapActivity  extends ActionBarActivity implements
        View.OnClickListener, LoaderCallbacks<Cursor> {

    public static final String TAG = "Baolizer";
    public static final String SUBMIT = "submit/";
    public static final String WEBVIEW = "webview/";
    private static final String PAGE_HTML = ".page.html";

    private Map map;

    public interface Map {
        public void clear();
        public void addBaobab(GeoHash latlng, String url, String title, String description);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        startService(new Intent(this, RefreshService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains("map")) {
            if (isGooglePlayServicesAvailable(this) == SUCCESS) {
                prefs.edit().putString("map", "Google Maps").commit();
            } else {
                prefs.edit().putString("map", "Mapbox Openstreetmap").commit();
            }
        }
        if (prefs.getString("map", "").equals("Google Maps")) {
            map = new GMapsFragment();
        } else if (prefs.getString("map", "").equals("Mapbox Openstreetmap")) {
            map = new MapboxFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, (Fragment) map)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(0, null, this);
        findViewById(R.id.hand).setOnClickListener(this);
        findViewById(R.id.filter).setOnClickListener(this);
        findViewById(R.id.info).setOnClickListener(this);
    }

    @Override
    public void onClick(View btn) {
        switch (btn.getId()) {
            case R.id.hand:
                Toast.makeText(this, "..Hand?", Toast.LENGTH_LONG).show();
//                openWebsite("http://map.baobab.org/submit/");
                break;
            case R.id.filter:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.info:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        return new CursorLoader(this,
                Uri.parse("content://org.baobab.baolizer/baobabs"),
                null, getWhereClause(), null,  null);
    }

    private String getWhereClause() {
        return "(" + ((FilterFragment) getSupportFragmentManager()
                .findFragmentById(R.id.products)).getWhereClause() +
                ") AND (" + ((FilterFragment) getSupportFragmentManager()
                .findFragmentById(R.id.categories)).getWhereClause() + ")";
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, final Cursor cursor) {
        Log.d(TAG, "load finished: " + cursor.getCount());
        map.clear();
        if (cursor.getCount() == 0) return;
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            GeoHash latlng = GeoHash.fromGeohashString(cursor.getString(6));
            map.addBaobab(latlng,
                    cursor.getString(7),  // id
                    cursor.getString(1),  // name
                    cursor.getString(5)); // street
        }
        cursor.registerContentObserver(new ContentObserver(null) {

            @Override
            public void onChange(boolean selfChange) {
                getSupportLoaderManager().restartLoader(0, null, MapActivity.this);
                cursor.unregisterContentObserver(this);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> l) {
        Log.d(TAG, "loader reset");
    }

    public void onBaobabClicked(String id) {
        Log.d(TAG, "clicked " + id);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, DetailsFragment.newInstance(id))
                .addToBackStack("details")
                .commit();
    }

    private void openWebsite(String url) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, WebFragment.newInstance(url))
                .addToBackStack("web")
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSupportFragmentManager().beginTransaction()
                .remove((Fragment) map).commitAllowingStateLoss();
    }
}

package org.baobab.baolizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

//import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
//import org.springframework.web.socket.sockjs.client.SockJsClient;
//import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;

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
//        List<Transport> transports = new ArrayList<>(2);
//        transports.add(new WebSocketTransport(StandardWebSocketClient()));
//        transports.add(new RestTemplateXhrTransport());
//
//        SockJsClient sockJsClient = new SockJsClient(transports);
//        sockJsClient.doHandshake(new MyWebSocketHandler(), "ws://example.com:8080/sockjs");
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
        findViewById(R.id.plant).setOnClickListener(this);
        findViewById(R.id.profile).setOnClickListener(this);
        findViewById(R.id.filter).setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ((ImageView) findViewById(R.id.tree_bottom))
                .setImageDrawable(getResources()
                        .getDrawable(R.drawable.tree_bottom));
        findViewById(R.id.gradient).getLayoutParams().height =
                (int) getResources().getDimension(R.dimen.gradient_height);
        ((FrameLayout.LayoutParams) findViewById(R.id.gradient)
                .getLayoutParams()).setMargins((int) getResources()
                .getDimension(R.dimen.gradient_margin_left), 0, 0, 0);
    }

    @Override
    public void onClick(View btn) {
        switch (btn.getId()) {
            case R.id.plant:
                Toast.makeText(this, "pflanzt..", Toast.LENGTH_LONG).show();
                openWebsite("http://map.baobab.org/submit/");
                break;
            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.filter:
                ((DrawerLayout) findViewById(R.id.drawer_layout))
                        .openDrawer(Gravity.END);
                break;
            case R.id.impressum:
                Toast.makeText(this, "Impressum", Toast.LENGTH_LONG).show();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DrawerLayout drawer = (DrawerLayout)
                    findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(Gravity.END)) {
                drawer.closeDrawer(Gravity.END);
                return true;
            }
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
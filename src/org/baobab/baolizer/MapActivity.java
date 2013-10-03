
package org.baobab.baolizer;

import java.util.HashMap;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import ch.hsr.geohash.GeoHash;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MapActivity  extends FragmentActivity implements
        LoaderCallbacks<Cursor>, OnInfoWindowClickListener,
        OnMapClickListener, OnCheckedChangeListener {

    private static final String TAG = "Baolizer";
    private HashMap<String, String> podioId;
    private GoogleMap map;
    private LinearLayout types;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition(
                        new LatLng(48.138790, 11.553338), 12, 90, 0)));
        map.setOnMapClickListener(this);
        map.setOnInfoWindowClickListener(this);
        types = (LinearLayout) findViewById(R.id.content);
        for (int i = 0; i < types.getChildCount(); i++) {
            ((CheckBox) types.getChildAt(i)).setOnCheckedChangeListener(this);
        }
        getSupportLoaderManager().initLoader(0, null, this);
        startService(new Intent(this, RefreshService.class));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        String where = "types IS 'none'";
        for (int i = 0; i < types.getChildCount(); i++) {
            CheckBox typ = (CheckBox) types.getChildAt(i);
            if (typ.isChecked()) {
                where += " OR types LIKE '%" + typ.getText() + "%'";
            }
        }
        return new CursorLoader(this, Uri.parse(
                "content://org.baobab.baolizer"), null, where, null,  null);
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> l, final Cursor cursor) {
        Log.d(TAG, "load finished: " + cursor.getCount());
        if (cursor.getCount() == 0) return;
        map.clear();
        cursor.moveToFirst();
        podioId = new HashMap<String, String>();
        while (!cursor.isLast()) {
            cursor.moveToNext();
            GeoHash geohash = GeoHash
                    .fromGeohashString(cursor.getString(7));
            Marker marker = map.addMarker(
                        new MarkerOptions()
                    .title(cursor.getString(3))
                    .snippet(cursor.getString(6))
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.tree))
                    .position(new LatLng(
                           geohash.getPoint().getLatitude(),
                           geohash.getPoint().getLongitude())));
            podioId.put(marker.getId(), cursor.getString(8));
        }
        cursor.registerContentObserver(onChange);
    }

    ContentObserver onChange = new ContentObserver(null) {

        @Override
        public void onChange(boolean selfChange) {
            getSupportLoaderManager().restartLoader(0, null, MapActivity.this);
        }
    };

    @Override
    public void onInfoWindowClick(Marker marker) {
//        Log.d(TAG, "browse " + SyncService.URL + marker.getSnippet());
        startActivity(new Intent(this, WebActivity.class)
        .setData(Uri.parse(RefreshService.URL + marker.getSnippet())));
    }

    @Override
    public void onMapClick(LatLng position) {
        Crouton.makeText(this, "Planted new Baobab Seed!", Style.INFO).show();
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.seedling))
                .draggable(true));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> l) {
    }
}

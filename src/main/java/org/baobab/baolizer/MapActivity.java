
package org.baobab.baolizer;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SlidingDrawer;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import ch.hsr.geohash.GeoHash;

public class MapActivity  extends ActionBarActivity implements
        ListView.OnItemClickListener, LoaderCallbacks<Cursor> {

    private static final String TAG = "Baolizer";
    public static final String SUBMIT = "submit/";
    public static final String WEBVIEW = "webview/";
    private static final String PAGE_HTML = ".page.html";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout navDrawer;
    private ListView drawerList;
    private MapView map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportLoaderManager().initLoader(0, null, this);
        startService(new Intent(this, RefreshService.class));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setupNavDrawer();
        setupMap();
    }

    private void setupMap() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(new MapboxTileLayer("examples.map-zgrqqx0w"));
        map.setCenter(new LatLng(48.138790, 11.553338));
        map.setZoom(13);
        UserLocationOverlay myLocationOverlay = new UserLocationOverlay(
                new GpsLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        map.getOverlays().add(myLocationOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_filter:
                SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.drawer);
                if (drawer.isOpened()) {
                    drawer.animateClose();
                } else {
                    drawer.animateOpen();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        return new CursorLoader(this,
                Uri.parse("content://org.baobab.baolizer/baobabs"),
                null, ((FilterFragment) getSupportFragmentManager()
                .findFragmentById(R.id.categories)).getWhereClause(),
                null,  null);
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> l, final Cursor cursor) {
        Log.d(TAG, "load finished: " + cursor.getCount());
        if (map.getItemizedOverlays().size() > 0) {
            map.getItemizedOverlays().get(0).removeAllItems();
        }
        map.invalidate();
        if (cursor.getCount() == 0) return;
        while (cursor.moveToNext()) {
            GeoHash geohash = GeoHash.fromGeohashString(cursor.getString(6));
            Marker m = new Baobab(
                    cursor.getString(7), // id
                    cursor.getString(1), // name
                    cursor.getString(5), // street
                    new LatLng(geohash.getPoint().getLatitude(),
                            geohash.getPoint().getLongitude()));
            map.addMarker(m);
        }
        requeryOnChange(cursor);
    }

    public class Baobab extends Marker {

        private final String id;

        public Baobab(String url, String title, String description, LatLng latLng) {
            super(map, title, description, latLng);
            setMarker(getResources().getDrawable(R.drawable.tree));
            setHotspot(Marker.HotspotPlace.BOTTOM_CENTER);
            this.id = url;
        }

        @Override
        protected InfoWindow createTooltip(MapView mv) {
            InfoWindow bubble = new InfoWindow(R.layout.tooltip, mv);
            bubble.getView().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        openWebsite(RefreshService.BASE_URL +
                                WEBVIEW + id + PAGE_HTML);
                    }
                    return true;
                }
            });
            return bubble;
        }
    }

    private void setupNavDrawer() {
        navDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item,
                getResources().getStringArray(R.array.menu)));
        drawerList.setOnItemClickListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                navDrawer,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                System.out.println("close");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                System.out.println("open");
            }
        };
        navDrawer.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        drawerList.setItemChecked(position, true);
        switch (position) {
            case 1:
                openWebsite("http://map.baobab.org/submit/");
        }
        closeDrawers();
    }

    private void openWebsite(String url) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, WebFragment.newInstance(url))
                .addToBackStack("submit")
                .commit();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (closeDrawers()) return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void requeryOnChange(Cursor cursor) {
        cursor.registerContentObserver(new ContentObserver(null) {

            @Override
            public void onChange(boolean selfChange) {
                getSupportLoaderManager().restartLoader(0, null, MapActivity.this);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> l) {
        Log.d(TAG, "loader reset");
    }

    private boolean closeDrawers() {
        boolean closed = false;
        SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.drawer);
        if (drawer.isOpened()) {
            drawer.close();
            closed = true;
        }
        if (navDrawer.isDrawerOpen(drawerList)) {
            navDrawer.closeDrawer(drawerList);
            closed = true;
        }
        return closed;
    }
}
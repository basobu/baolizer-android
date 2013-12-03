
package org.baobab.baolizer;

import android.app.Fragment;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.SpinnerAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import ch.hsr.geohash.GeoHash;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MapActivity  extends ActionBarActivity implements
        View.OnClickListener,
        LoaderCallbacks<Cursor>,
        OnCheckedChangeListener,
        OnInfoWindowClickListener {

    // < TSC
    private String[] mMenuTitles;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // > TSC

    private static final String PAGE_HTML = ".page.html";
    private static final String TAG = "Baolizer";
    public static final String WEBVIEW = "webview/";
    public static final String SUBMIT = "submit/";
    private HashMap<String, String> podioId;
    private GoogleMap map;
    private LinearLayout types;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);



        // < tsc
        mTitle = mDrawerTitle = getTitle();
        mMenuTitles = getResources().getStringArray(R.array.menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());





        // TODO: Drawer toggle don't work!!??

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Filter Dropdown
        // TODO



        // > tsc



        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition(
                        new LatLng(48.138790, 11.553338), 12, 90, 0)));
        map.setOnInfoWindowClickListener(this);
        types = (LinearLayout) findViewById(R.id.types);
        for (int i = 0; i < types.getChildCount(); i++) {
            ((CheckBox) types.getChildAt(i)).setOnCheckedChangeListener(this);
        }
        //findViewById(R.id.seed).setOnClickListener(this);
        getSupportLoaderManager().initLoader(0, null, this);
        startService(new Intent(this, RefreshService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the map_menu content by replacing fragments


        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    ContentObserver onChange = new ContentObserver(null) {

        @Override
        public void onChange(boolean selfChange) {
            getSupportLoaderManager().restartLoader(0, null, MapActivity.this);
        }
    };

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
                    .snippet(cursor.getString(1))
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.tree))
                    .position(new LatLng(
                           geohash.getPoint().getLatitude(),
                           geohash.getPoint().getLongitude())));
            podioId.put(marker.getId(), cursor.getString(8));
        }
        cursor.registerContentObserver(onChange);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Uri url = Uri.parse(RefreshService.BASE_URL + WEBVIEW +
                podioId.get(marker.getId()) + PAGE_HTML);
        System.out.println("clicked " + url);
        startActivity(new Intent(this, WebActivity.class).setData(url));
    }


    @Override
    public void onLoaderReset(Loader<Cursor> l) {
        System.out.println("loader reset");
        map.clear();
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, WebActivity.class).setData(
                Uri.parse("http://baolizer.baobab.org/submit")));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.drawer);
            if (drawer.isOpened()) {
                drawer.close();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}

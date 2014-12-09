package org.baobab.baolizer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import ch.hsr.geohash.GeoHash;

public class MapboxFragment extends Fragment implements MapActivity.Map {

    private MapView map;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        return inf.inflate(R.layout.fragment_mapbox, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(new MapboxTileLayer("examples.map-zgrqqx0w"));
        map.setCenter(new LatLng(48.138790, 11.553338));
        map.setZoom(13);
        UserLocationOverlay myLocationOverlay = new UserLocationOverlay(
                new GpsLocationProvider(getActivity()), map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        map.getOverlays().add(myLocationOverlay);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void clear() {
        if (map.getItemizedOverlays().size() > 0) {
            map.getItemizedOverlays().get(0).removeAllItems();
        }
        map.invalidate();
    }

    @Override
    public void addBaobab(GeoHash latlng, String id, String title, String description) {
        Marker m = new Baobab(id, title, description,
            new LatLng(latlng.getPoint().getLatitude(),
                    latlng.getPoint().getLongitude()));
        map.addMarker(m);
    }

    public class Baobab extends Marker {

        private final String id;

        public Baobab(String id, String title, String description, LatLng latLng) {
            super(map, title, description, latLng);
            setMarker(getResources().getDrawable(R.drawable.tree));
            setHotspot(Marker.HotspotPlace.BOTTOM_CENTER);
            this.id = id;
        }

        @Override
        protected InfoWindow createTooltip(MapView mv) {
            InfoWindow bubble = new InfoWindow(R.layout.tooltip, mv);
            bubble.getView().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ((MapActivity) getActivity()).onBaobabClicked(id);
                    }
                    return true;
                }
            });
            return bubble;
        }
    }
}
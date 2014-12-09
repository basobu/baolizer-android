package org.baobab.baolizer;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import ch.hsr.geohash.GeoHash;

public class GMapsFragment extends SupportMapFragment implements MapActivity.Map {

    HashMap<String, String> get = new HashMap<String, String>(500);

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMap().setMyLocationEnabled(true);
        getMap().animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition(
                        new LatLng(48.138790, 11.553338), 12, 90, 0)));
        getMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {
                ((MapActivity) getActivity())
                        .onBaobabClicked(get.get(marker.getId()));
            }
        });
    }

    @Override
    public void clear() {
        getMap().clear();
    }

    @Override
    public void addBaobab(GeoHash latlng, String id, String title, String description) {
        get.put(getMap().addMarker(
            new MarkerOptions()
                .title(title)
                .snippet(description)
                .icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.tree))
                .position(new LatLng(
                    latlng.getPoint().getLatitude(),
                    latlng.getPoint().getLongitude()))
        ).getId(), id);
    }
}
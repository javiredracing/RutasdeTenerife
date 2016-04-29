package com.rutas.java;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jfernandez on 28/04/2016.
 */
public class PathPoints implements ColoredPolylineTileOverlay.PointHolder {

    private LatLng myLatlng;
    private long myAltitude;

    public PathPoints(LatLng latLng, long altitude){
        super();
        myLatlng = latLng;
        myAltitude = altitude;
    }

    @Override
    public LatLng getLatLng() {
        return myLatlng;
    }

    @Override
    public long getTime() {
        return myAltitude;
    }
}

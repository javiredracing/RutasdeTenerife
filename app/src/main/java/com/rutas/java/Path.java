package com.rutas.java;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfernandez on 28/04/2016.
 */
public class Path implements ColoredPolylineTileOverlay.PointCollection{

    private ArrayList<PathPoints> camino;

    public Path(ArrayList<LatLng> _camino,  ArrayList<Integer> altitude){
        super();
        camino = new ArrayList<PathPoints>();
        int size = _camino.size();
        for (int i = 0; i < size; i++){
            PathPoints p = new PathPoints(_camino.get(i), altitude.get(i));
            camino.add(p);
        }
    }

    public Path(){
        super();
        camino = new ArrayList<PathPoints>();
    }

    public void addItem(LatLng latLng, long altitude){
        PathPoints p = new PathPoints(latLng, altitude);
        camino.add(p);
    }

    @Override
    public List getPoints() {
        return camino;
    }
}

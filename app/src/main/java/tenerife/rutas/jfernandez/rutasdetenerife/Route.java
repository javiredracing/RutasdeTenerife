package tenerife.rutas.jfernandez.rutasdetenerife;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by jfernandez on 14/05/2015.
 */
public class Route {

    private ArrayList<Marker> markersList;
    private String name;
    private String xmlRoute;
    public boolean isActive;
    private float dist;
    private int difficulty;
    private int id;
    private float durac;

    Route(int _id, String _name, String _xml, float _dist, int _difficulty, float durac){
        id = _id;
        name = _name;
        xmlRoute = _xml;
        dist = _dist;
        difficulty = _difficulty;
        this.durac = durac;
        markersList = new ArrayList<Marker>();
    }

    public void setMarker(Marker marker){
        markersList.add(marker);
    }

    public String getName(){
        return name;
    }

    public String getXmlRoute(){
        return xmlRoute;
    }

    public float getDist(){
        return dist;
    }

    public float getDurac(){ return durac; }

    public int getDifficulty(){
        return difficulty;
    }

    public int getId(){
        return id;
    }

    public LatLng getFirstPoint(){
        return markersList.get(0).getPosition();
    }

    public void setMarkersVisibility(boolean visibility){
        int size = markersList.size();
        for (int i = 0; i < size; i++){
            Marker m = markersList.get(i);
            m.setVisible(visibility);
        }
    }
}

package tenerife.rutas.jfernandez.rutasdetenerife;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by jfernandez on 14/05/2015.
 */
public class Route {
    private ArrayList<LatLng> pointList;
    private String name;
    private String xmlRoute;
    public boolean isActive;
    private float dist;
    private int difficulty;
    private int id;
    Route(int _id, String _name, String _xml, float _dist, int _difficulty){
        id = _id;
        name = _name;
        xmlRoute = _xml;
        dist = _dist;
        difficulty = _difficulty;
        pointList = new ArrayList<LatLng>();
    }

    public void setPoint(LatLng point){
        pointList.add(point);
    }

    public ArrayList<LatLng> getPointList() {
        return pointList;
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

    public int getDifficulty(){
        return difficulty;
    }

    public int getId(){
        return id;
    }
}

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
    private String weatherJson;
    private long timeStamp;
    private int region;
    private boolean isApproved;

    Route(int _id, String _name, String _xml, float _dist, int _difficulty, float durac, int approved, int reg){
        id = _id;
        name = _name;
        xmlRoute = _xml;
        dist = _dist;
        difficulty = _difficulty;
        this.durac = durac;
        markersList = new ArrayList<Marker>();
        weatherJson = null;
        timeStamp = 0;
        region = reg;
        if (approved == 1)
            isApproved = true;
        else
            isApproved = false;
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
    public void setWeatherJson(String json){
        weatherJson = json;
        timeStamp = System.currentTimeMillis();
    }
    public String getWeatherJson(){
        long nowTime = System.currentTimeMillis();
        if (weatherJson != null){
            if ((nowTime - timeStamp) <= 3600000){    //1 hour
                return weatherJson;
            }
            else
                clearWeather();
        }
        return null;
    }

    public void clearWeather(){
        weatherJson = null;
        timeStamp = 0;
    }

    public int getRegion(){
        return region;
    }

    public boolean isApproved(){
        return isApproved;
    }
}

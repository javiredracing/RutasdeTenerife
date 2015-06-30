package tenerife.rutas.jfernandez.rutasdetenerife;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by jfernandez on 14/05/2015.
 */
public class Route {

    private ArrayList<MyMarker> markersList;
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
    private int approved;
    //type{0 gr,1 pr,2 sl,3 regular};
    //private int type;

    Route(int _id, String _name, String _xml, float _dist, int _difficulty, float durac, int _approved, int reg){
        id = _id;
        name = _name;
        xmlRoute = _xml;
        dist = _dist;
        difficulty = _difficulty;
        this.durac = durac;
        markersList = new ArrayList<MyMarker>();
        weatherJson = null;
        timeStamp = 0;
        region = reg;
        approved = _approved;
       /* if (approved == 1)
            isApproved = true;
        else
            isApproved = false;
        //determine path type
        if (approved == 1){
            if ((region == 5)|| dist > 50 ){
                type = 0;   //GR
            }else{
                if ( dist > 6){
                    type = 1;   //PR
                }else
                if (dist <= 6)
                    type = 2;   //SL
            }
        }else
            type = 3;   //regular*/
    }

    public void setMarker(MyMarker marker){
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
            MyMarker m = markersList.get(i);
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

    /**
     * Return path type
     * @return type{0 GR, 1 PR, 2 SL ,3 REGULAR};
     */
    public int approved(){
        return approved;
    }

    public ArrayList<MyMarker> getMarkersList() {
        return markersList;
    }


   /* public int getType(){
        return type;
    }*/
}

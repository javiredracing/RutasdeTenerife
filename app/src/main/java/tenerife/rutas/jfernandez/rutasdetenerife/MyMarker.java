package tenerife.rutas.jfernandez.rutasdetenerife;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Javi on 21/06/2015.
 */
public class MyMarker implements ClusterItem {
    private LatLng latLng;
    private String name;
    private int id;
    private int icon;
    private boolean isVisible;

    public MyMarker(double lat, double lon, String _name, int _id, int type){
        latLng = new LatLng(lat, lon);
        name = _name;
        id = _id;
        switch (type){
            case 0: //gr
                icon = R.drawable.marker_sign_16_red;
                break;
            case 1: //pr
                icon = R.drawable.marker_sign_16_yellow;
                break;
            case 2: //sl
                icon = R.drawable.marker_sign_16_green;
                break;
            case 3: //regular
                icon = R.drawable.marker_sign_16_normal;
                break;
            default:
                icon = R.drawable.marker_sign_16_normal;
        }
        isVisible = true;
    }
    @Override
    public LatLng getPosition() {
        return latLng;
    }
    public String getName(){
        return name;
    }
    public int getId(){
        return id;
    }
    public int getIcon(){
        return icon;
    }
    public boolean isVisible(){
        return isVisible;
    }
    public void setVisible(boolean visibility){
        isVisible = visibility;
    }
}

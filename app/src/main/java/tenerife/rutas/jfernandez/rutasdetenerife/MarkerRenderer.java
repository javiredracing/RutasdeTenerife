package tenerife.rutas.jfernandez.rutasdetenerife;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Javi on 21/06/2015.
 */

public class MarkerRenderer extends DefaultClusterRenderer {

    private BitmapDescriptor iconRed, iconGreen, iconNormal, iconYellow;

    public MarkerRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
        iconGreen = BitmapDescriptorFactory.fromResource(R.drawable.marker_sign_16_green);
        iconNormal = BitmapDescriptorFactory.fromResource(R.drawable.marker_sign_16_normal);
        iconYellow = BitmapDescriptorFactory.fromResource(R.drawable.marker_sign_16_yellow);
        iconRed = BitmapDescriptorFactory.fromResource(R.drawable.marker_sign_16_red);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {
        //Log.v("CLusterRenderer",markerOptions.toString());
        MyMarker marker = (MyMarker)item;
       // BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(marker.getIcon());

        markerOptions.title(marker.getName());
        markerOptions.snippet("" + marker.getId());
        switch (marker.getIcon()){
            case R.drawable.marker_sign_16_green:
                markerOptions.icon(iconGreen);
                break;
            case R.drawable.marker_sign_16_normal:
                markerOptions.icon(iconNormal);
                break;
            case R.drawable.marker_sign_16_yellow:
                markerOptions.icon(iconYellow);
                break;
            case R.drawable.marker_sign_16_red:
                markerOptions.icon(iconRed);
                break;
            default:
                markerOptions.icon(iconNormal);
        }
        MyMarker m = (MyMarker)item;
        float alpha = 1;
        if (!m.isVisible())
            alpha = 0.4f;
        markerOptions.alpha(alpha);
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected void onClusterItemRendered(ClusterItem clusterItem, Marker marker) {
        //Log.v("CLusterRenderer","onClusterItemRendered");
        MyMarker m = (MyMarker)clusterItem;
        float alpha = 1;
        if (!m.isVisible())
            alpha = 0.4f;
        marker.setAlpha(alpha);
        super.onClusterItemRendered(clusterItem, marker);
    }

  /*  @Override
    protected void onBeforeClusterRendered(Cluster cluster, MarkerOptions markerOptions) {

        super.onBeforeClusterRendered(cluster, markerOptions);
    }*/
    /* @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }*/
}

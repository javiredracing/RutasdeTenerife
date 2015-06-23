package tenerife.rutas.jfernandez.rutasdetenerife;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Set;

/**
 * Created by Javi on 21/06/2015.
 */

public class MarkerRenderer extends DefaultClusterRenderer {

    public MarkerRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {
        MyMarker marker = (MyMarker)item;
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(marker.getIcon());
        markerOptions.title(marker.getName());
        markerOptions.snippet("" + marker.getId());
        markerOptions.icon(icon);
        MyMarker m = (MyMarker)item;
        float alpha = 1;
        if (!m.isVisible())
            alpha = 0.4f;
        markerOptions.alpha(alpha);
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected void onClusterItemRendered(ClusterItem clusterItem, Marker marker) {
        MyMarker m = (MyMarker)clusterItem;
        float alpha = 1;
        if (!m.isVisible())
            alpha = 0.5f;
        marker.setAlpha(alpha);
        super.onClusterItemRendered(clusterItem, marker);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster cluster, MarkerOptions markerOptions) {

        super.onBeforeClusterRendered(cluster, markerOptions);
    }
    /* @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }*/
}

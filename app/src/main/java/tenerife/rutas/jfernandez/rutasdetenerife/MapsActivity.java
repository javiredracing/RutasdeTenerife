package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MapsActivity extends Activity implements OnMapReadyCallback, LocationListener,
        ConnectionCallbacks,
        OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment fragmentMap;
    //private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private ArrayList<Route> routesList = new ArrayList<Route>();
    private BaseDatos bd;

    private Handler handlerPath;
    private Polyline pathShowed;
    private Route lastRouteShowed;

    private LinearLayout quickInfo;

    private boolean enableTap = false;

    //private FusedLocationProviderApi locator = LocationServices.FusedLocationApi;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private Location mCurrentLocation;
    private Marker myPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        quickInfo = (LinearLayout) findViewById(R.id.layoutQuickInfo);
        handlerPath = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ArrayList<LatLng> path = (ArrayList<LatLng>) msg.obj;
                drawPath(path);
                enableTap = true;
            }
        };
        if (isGooglePlayServicesAvailable()) {
            createLocationRequest();
            mGoogleApiClient = new GoogleApiClient.Builder(this).
                    addApi(LocationServices.API).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    build();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
            Log.d("onResume", "Location update resumed .....................");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
        if (myPos == null)
            myPos = mMap.addMarker(new MarkerOptions().
                    position(new LatLng(28.299221, -16.525690))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_pos))
                            .title("myPos")
            );
        Log.v("Connected", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("onConnectionSuspended", "Connection suspended!");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        myPos.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        Log.v("Location", mCurrentLocation.toString());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("Connection failed", "FAILED!");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mMap = googleMap;
            bd = new BaseDatos(getApplicationContext());
            try {
                bd.crearBaseDatos();
                bd.abrirBD();
                //Carga posicion inicial
                setUpMap(googleMap);
                enableTap = true;
            }catch(SQLException sqle){
                throw sqle;
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    /*********************************************/
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (fragmentMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            fragmentMap = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            // Check if we were successful in obtaining the map.
            fragmentMap.getMapAsync(this);
            //fragmentMap.getMap();
        }
    }

    protected void createLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(GoogleMap googleMap) {
        //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        Cursor c = bd.getInfoMap(false, new String[]{"nombre", "inicX", "inicY", "finX", "finY", "duracion", "longitud", "dificultad", "kml", "id"}, null, null, null, null, null);
        if (c.getCount() > 0){
            String nombre;
            //String dificultad;
            String kml = "";
            while (c.moveToNext()){
                nombre = c.getString(0);
                double inicX = c.getDouble(1);
                double inicY = c.getDouble(2);
                double finX = c.getDouble(3);
                double finY = c.getDouble(4);
                //float durac = c.getFloat(5);
                float dist = c.getFloat(6);
                int dific = c.getInt(7);
                kml = c.getString(8);
                int id = c.getInt(9);
                LatLng geopoint = new LatLng(inicX, inicY);
                Marker m = googleMap.addMarker(new MarkerOptions().
                        position(geopoint).
                        title(nombre).snippet(""+id));
                //markerList.add(m);
                Route route = new Route(id,nombre,kml,dist,dific);
                route.setPoint(geopoint);
                if ((finX != 0) && (finY != 0)){
                    LatLng geopoint2 = new LatLng(finX, finY);
                    m = googleMap.addMarker(new MarkerOptions().
                            position(geopoint2).
                            title(nombre).snippet(""+id));
                    //markerList.add(m);
                    route.setPoint(geopoint2);
                }
                routesList.add(route);
            }
        }
        c.close();
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(28.299221, -16.525690), 10);
        googleMap.moveCamera(center);
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getTitle().contentEquals("myPos")) {
                    int markerId = Integer.parseInt(marker.getSnippet());
                    Route route = getRoute(markerId);
                    clickAction(route, marker.getPosition());
                    //mMap.moveCamera(center);
                }else{
                   holaMundo(null);
                }
                return true;
            }
        });
    }



    /**************************************************************************/

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private Route getRoute(int id){
        int size = routesList.size();
        for (int i = 0; i < size; i++ ){
            Route r = routesList.get(i);
            if (r.getId() == id){
                return r;
            }
        }
        return null;
    }

    private void clickAction(Route route, LatLng pos){
        if (route == null)
            return;
        if (enableTap){
            enableTap = false;

            if (lastRouteShowed != null)
                if (lastRouteShowed.getId() == route.getId()){
                    lastRouteShowed = null;
                }

            route.isActive = !(route.isActive);
            if (lastRouteShowed != null){
                lastRouteShowed.isActive = false;
            }
            lastRouteShowed = route;
            if (pathShowed != null)
                pathShowed.remove();

            if ((route.isActive) && (route.getXmlRoute()!= "")){
                showQuickInfo(route);
                CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, mMap.getCameraPosition().zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing));
                mMap.animateCamera(cu);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        try{
                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();
                            Message msg = new Message();
                            String xmlPath = lastRouteShowed.getXmlRoute();
                            String fileNameArray[] = xmlPath.split("\\.");
                            String extension = fileNameArray[fileNameArray.length - 1];
                            InputStream myInput;
                            myInput = getApplicationContext().getAssets().open(xmlPath);
                            InputSource arhivo = new InputSource(myInput);
                            arhivo.setEncoding("UTF-8");
                            if (extension.toLowerCase().contentEquals("kml")){
                                KmlHandler kmlHandler = new KmlHandler();
                                xr.setContentHandler(kmlHandler);
                                xr.parse(arhivo);
                                msg.obj = kmlHandler.getPath();
                            }else{
                                GpxHandler gpxHandler = new GpxHandler(true);
                                xr.setContentHandler(gpxHandler);
                                xr.parse(arhivo);
                                msg.obj = gpxHandler.getPath();
                            }
                            handlerPath.sendMessage(msg);
                            myInput.close();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                            enableTap = true;
                        } catch (SAXException e) {
                            e.printStackTrace();
                            enableTap = true;
                        } catch (IOException e){
                            e.printStackTrace();
                            enableTap = true;
                        }
                    }
                }).start();
            }else{
                //Log.d("Action click", "close info or do nothing");
                if (quickInfo.getVisibility() != View.GONE){
                    quickInfo.setVisibility(View.GONE);
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animate_off);
                    animation.setDuration(400);
                    quickInfo.setAnimation(animation);
                    quickInfo.animate();
                    animation.start();
                }
                enableTap = true;
            }
        }
    }

    /**
     * Draw a polyline over map
     * @param path ArrayList<LatLng>
     */
    private void drawPath(ArrayList<LatLng> path){
        //pathShowed.remove();
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(path);
        polylineOptions.width(5).color(Color.BLUE);

        pathShowed = mMap.addPolyline(polylineOptions);
    }

    private void showQuickInfo(Route route){
        if (route != null){
            LinearLayout itemNested = (LinearLayout)quickInfo.getChildAt(0);
            ImageView icon = (ImageView) itemNested.getChildAt(0);
            //TODO set icon drawable
            TextView title = (TextView) itemNested.getChildAt(1);
            title.setText(route.getName());
            LinearLayout itemNested2 = (LinearLayout) quickInfo.getChildAt(1);
            TextView distance = (TextView) itemNested2.getChildAt(0);
            distance.setText(""+route.getDist()+ " Km");
            ImageView difficult = (ImageView) itemNested2.getChildAt(1);
            switch (route.getDifficulty()){
                case 1:
                    difficult.setImageResource(R.drawable.nivel_facil);
                    break;
                case 2:
                    difficult.setImageResource(R.drawable.nivel_intermedio);
                    break;
                case 3:
                    difficult.setImageResource(R.drawable.nivel_dificil);
                    break;
                default:
                    difficult.setImageResource(R.drawable.nivel_intermedio);
            }
            if (quickInfo.getVisibility()== View.GONE){

                quickInfo.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animate_on);
                animation.setDuration(400);
                quickInfo.setAnimation(animation);
                quickInfo.animate();
                animation.start();
            }
        }
    }

    public void holaMundo(View v){
        Toast.makeText(getApplicationContext(),"hola mundo",Toast.LENGTH_LONG).show();
    }
}

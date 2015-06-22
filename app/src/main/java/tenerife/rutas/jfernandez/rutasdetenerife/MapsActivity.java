package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
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

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        ConnectionCallbacks,
        OnConnectionFailedListener, SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment fragmentMap;
    //private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private ClusterManager<MyMarker> clusterManager;
    private ArrayList<Route> routesList = new ArrayList<Route>();
    private BaseDatos bd;
    private LatLngBounds latLngBounds;

    private Handler handlerPath;
    private Polyline pathShowed;
    private Route lastRouteShowed;

    private LinearLayout quickInfo;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private EditText et_search;

    private ListView drawerListMenu;

    private boolean enableTap = false;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private Location mCurrentLocation;
    private Marker myPos;
    private Handler handlerGeocoder;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity;
    private float[] geomagnetic;
    private int lastAzimuth = 0;

    private SharedPreferences prefs;
    private Toast globalToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        prefs = this.getSharedPreferences("options", Context.MODE_PRIVATE);
        //Configuring global toast
        globalToast = Toast.makeText(getApplicationContext(),null, Toast.LENGTH_LONG);
        View v = globalToast.getView();
        v.setBackgroundResource(R.drawable.border_toast);
        TextView tv = (TextView) v.findViewById(android.R.id.message);
        if( tv != null) tv.setGravity(Gravity.CENTER);
        globalToast.setView(v);

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.btActionMenu);
        //button.setSize(FloatingActionButton.SIZE_NORMAL);
        /*button.setColorNormalResId(R.color.gris);
        button.setColorPressedResId(android.R.color.white);*/
        button.setIcon(R.drawable.logo);
        //button.setStrokeVisible(false);
        FloatingActionButton button2 = (FloatingActionButton) findViewById(R.id.btActionList);
        button2.setIcon(R.drawable.list_32);

        //Configuring quick info
        quickInfo = (LinearLayout) findViewById(R.id.layoutQuickInfo);
        quickInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Close info, center in path, share path buttons
                if (lastRouteShowed.isActive){
                    if (getSupportFragmentManager().findFragmentByTag("FragmentDialogExtendedInfo") == null){
                        FragmentDialogExtendedInfo extendedInfo = new FragmentDialogExtendedInfo();
                        Bundle bundle = new Bundle();
                        bundle.putString(getString(R.string.VALUE_NAME),lastRouteShowed.getName());
                        bundle.putString(getString(R.string.VALUE_XML_ROUTE), lastRouteShowed.getXmlRoute());
                        bundle.putFloat(getString(R.string.VALUE_DIST), lastRouteShowed.getDist());
                        bundle.putFloat(getString(R.string.VALUE_TIME), lastRouteShowed.getDurac());
                        bundle.putInt(getString(R.string.VALUE_ID), lastRouteShowed.getId());
                        bundle.putInt(getString(R.string.VALUE_DIF), lastRouteShowed.getDifficulty());
                        bundle.putBoolean(getString(R.string.VALUE_APPROVED), lastRouteShowed.isApproved());
                        LatLng latLng = lastRouteShowed.getFirstPoint();
                        double[] latLngDouble = new double[2];
                        latLngDouble[0] = latLng.latitude;
                        latLngDouble[1] = latLng.longitude;
                        bundle.putDoubleArray(getString(R.string.VALUE_LATLNG),latLngDouble);
                        if (myPos != null){
                            double[] latLngMyPos = new double[2];
                            latLngMyPos[0] = myPos.getPosition().latitude;
                            latLngMyPos[1] = myPos.getPosition().longitude;
                            bundle.putDoubleArray(getString(R.string.VALUE_LATLNG_POS),latLngMyPos);
                        }
                        extendedInfo.setArguments(bundle);
                        extendedInfo.show(getSupportFragmentManager(), "FragmentDialogExtendedInfo");
                    }
                }
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.right_drawer);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(),routesList.get(position).getName(), Toast.LENGTH_LONG).show();
                //et_search.clearFocus();
                RouteListAdapter rla = (RouteListAdapter) drawerList.getAdapter();
                //int routeId = rla.getArrayList().get(position).getId();
                int routeId = rla.getItemIdAtPosition(position);
                closeNavigationDrawer();
                //drawerLayout.closeDrawers();
                //int routeId = routesList.get(position).getId();
                if (routeId >= 0) {
                    Route r = getRoute(routeId);
                    clickAction(r, r.getFirstPoint());
                }
            }
        });
        et_search = (EditText)findViewById(R.id.et_search);
        configureSearch();
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        configureMenu();/*Menu*/

        handlerPath = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ArrayList<LatLng> path = (ArrayList<LatLng>) msg.obj;
                drawPath(path);
                enableTap = true;
            }
        };
        handlerGeocoder = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Address dir = (Address) msg.obj;
                showCurrentAddress(dir);
            }
        };
        if (isGooglePlayServicesAvailable()) {
            createLocationRequest();
            mGoogleApiClient = new GoogleApiClient.Builder(this).
                    addApi(LocationServices.API).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    build();
        }else{
            Toast.makeText(getApplicationContext(),"Error loading Google Play Services, try again", Toast.LENGTH_SHORT).show();
            finish();
        }


        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mGoogleApiClient != null){
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, locationRequest, this);

                Log.d("onResume", "Location update resumed .....................");
            }
            sensorManager.registerListener(this, accelerometer, 330000);
            sensorManager.registerListener(this, magnetometer, 330000); //3 times per second
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU){
            //Toast.makeText(getApplicationContext(),"menu key", Toast.LENGTH_SHORT).show();
            if (drawerLayout.isDrawerOpen(Gravity.LEFT))
                //drawerLayout.closeDrawers();
                closeNavigationDrawer();
            if (!drawerLayout.isDrawerOpen(Gravity.LEFT)){
                if(drawerLayout.isDrawerOpen(Gravity.RIGHT)){
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                }
                drawerLayout.openDrawer(Gravity.LEFT);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.RIGHT) || drawerLayout.isDrawerOpen(Gravity.LEFT)){
            //drawerLayout.closeDrawers();
            closeNavigationDrawer();
        }else{
            AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setIcon(R.drawable.logo)
                    .setMessage("Quit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapsActivity.super.onBackPressed();
                        }
                    }).create();

            d.show();
            changeAlertDividerColor(d);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        globalToast.cancel();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(getString(R.string.FILTER_LONG),0);
        editor.putInt(getString(R.string.FILTER_DURAC),0);
        editor.putInt(getString(R.string.FILTER_DIF), 0);
        editor.commit();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);

        Log.v("Connected", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("onConnectionSuspended", "Connection suspended!");
        if (myPos != null){
            myPos.remove();
            myPos = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (myPos == null)
            myPos = mMap.addMarker(new MarkerOptions().
                            position(new LatLng(28.299221, -16.525690))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_pos2_24))
                            .title("My position")
                            .anchor(0.5f, 0.5f)
                            .flat(true)
                            .draggable(false)
            );
        else
            myPos.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        //myPos.setRotation(30);
        //Log.v("Location", mCurrentLocation.toString());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("Connection failed", "FAILED!");
        if (myPos != null){
            myPos.remove();
            myPos = null;
        }
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
                globalToast.setText("Error cargandado base de datos");
                globalToast.show();
                finish();
                //throw sqle;
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
            finally {
                bd.close();
            }
                    /*ClusterManager*/

            //mMap.setOnMarkerClickListener(clusterManager);
        /**/
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (myPos != null){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                gravity = event.values.clone();
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                geomagnetic = event.values.clone();
            }
            if (gravity != null && geomagnetic != null){
                float R[] = new float[9];
                float I[]= new float[9];
                boolean success = SensorManager.getRotationMatrix(R,I,gravity,geomagnetic);
                if (success){
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    float azimuth = (float) Math.toDegrees(orientation[0]);
                    int azimuthFinal = Math.round(azimuth);
                    if (azimuthFinal < 0)
                        azimuthFinal = 360 + azimuthFinal;
                    if (azimuthFinal != lastAzimuth){
                        switch (getWindowManager().getDefaultDisplay()
                                .getRotation()) {
                            case Surface.ROTATION_90:
                                azimuthFinal = (azimuthFinal + 90)%360;
                                break;
                            case Surface.ROTATION_180:
                                azimuthFinal = azimuthFinal - 180;
                                if (azimuthFinal< 0){
                                    azimuthFinal = 360 - azimuthFinal;
                                }
                                break;
                            case Surface.ROTATION_270:
                                azimuthFinal = azimuthFinal - 90;
                                if (azimuthFinal < 0)
                                    azimuthFinal = 360 - azimuthFinal;
                                break;

                        }
                        lastAzimuth = azimuthFinal;
                        myPos.setRotation(azimuthFinal);
                        //Log.v("Orientation",""+azimuthFinal);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        int size = routesList.size();
        for (int i = 0; i< size; i++){
            routesList.get(i).clearWeather();
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
            //Next code is for update camera with bounds limits
            //http://stackoverflow.com/questions/13692579/movecamera-with-cameraupdatefactory-newlatlngbounds-crashes
            View mapView = fragmentMap.getView();
            if (mapView.getViewTreeObserver().isAlive()){
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            fragmentMap.getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }else
                            fragmentMap.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (mMap != null && latLngBounds != null)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,20));
                    }
                });
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(GoogleMap googleMap) {
        //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        Cursor c = bd.getInfoMap(false, new String[]{"nombre", "inicX", "inicY", "finX", "finY", "duracion", "longitud", "dificultad", "kml", "id","homologado", "region"}, null, null, null, null, null);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        if (c.getCount() > 0){
            String nombre;
            //String dificultad;
            String kml = "";
            //ArrayList<Route> items = new ArrayList<Route>();
            clusterManager = new ClusterManager<MyMarker>(getApplicationContext(), mMap);
            clusterManager.setRenderer(new MarkerRenderer(getApplicationContext(), mMap, clusterManager));
            clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyMarker>() {
                @Override
                public boolean onClusterClick(Cluster<MyMarker> cluster) {
                  float  zoom = mMap.getCameraPosition().zoom;
                    if (zoom < 20 )
                        zoom++;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),zoom);
                    mMap.animateCamera(cu);
                    return true;
                }
            });
            clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMarker>() {
                @Override
                public boolean onClusterItemClick(MyMarker myMarker) {
                    //Toast.makeText(getApplicationContext(), myMarker.getName(), Toast.LENGTH_LONG).show();
                    int markerId = (myMarker.getId());
                    Route route = getRoute(markerId);
                    clickAction(route, myMarker.getPosition());
                    return true;
                }
            });
            //clusterManager.getMarkerCollection().getMarkers();
            mMap.setOnCameraChangeListener(clusterManager);
            mMap.setOnMarkerClickListener(clusterManager);

            //BitmapDescriptor iconPR = BitmapDescriptorFactory.fromResource(R.drawable.marker_sign_24);
            while (c.moveToNext()){
                nombre = c.getString(0);
                double inicLat = c.getDouble(1);
                double inicLong = c.getDouble(2);
                double finLat = c.getDouble(3);
                double finLon = c.getDouble(4);
                float durac = c.getFloat(5);
                float dist = c.getFloat(6);
                int dific = c.getInt(7);
                kml = c.getString(8);
                int id = c.getInt(9);
                int approved = c.getInt(10);
                int region = c.getInt(11);
                LatLng geopoint = new LatLng(inicLat, inicLong);
               /* Marker m = googleMap.addMarker(new MarkerOptions()
                                .position(geopoint)
                                .title(nombre)
                                .snippet("" + id)
                                .icon(iconPR)
                );*/
                MyMarker m = new MyMarker(inicLat, inicLong, nombre, id, R.drawable.marker_sign_24);
                clusterManager.addItem(m);
                boundsBuilder.include(geopoint);
                Route route = new Route(id,nombre,kml,dist,dific, durac,approved, region);    //insert region at the end
                route.setMarker(m);
                if ((finLat != 0) && (finLon != 0)){
                    LatLng geopoint2 = new LatLng(finLat, finLon);
                    /*m = googleMap.addMarker(new MarkerOptions().
                            position(geopoint2).
                            title(nombre).snippet(""+id).icon(iconPR));*/
                    //markerList.add(m);
                    MyMarker m2 = new MyMarker(finLat, finLon, nombre, id, R.drawable.marker_sign_24);
                    clusterManager.addItem(m2);
                    boundsBuilder.include(geopoint2);
                    route.setMarker(m2);
                }
                routesList.add(route);
            }
            drawerList.setAdapter(new RouteListAdapter(getApplicationContext(), routesList));
        }
        c.close();
        //CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(28.299221, -16.525690), 10);
        latLngBounds = boundsBuilder.build();
        //CameraUpdate center = CameraUpdateFactory.newLatLngBounds(latLngBounds,0);
        //googleMap.moveCamera(center);
        int map_type = prefs.getInt(getString(R.string.MAP_TYPE),3);
        googleMap.setMapType(map_type);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
       /* googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (!marker.getTitle().contentEquals("myPos")) {
                    int markerId = Integer.parseInt(marker.getSnippet());
                    Route route = getRoute(markerId);
                    clickAction(route, marker.getPosition());
                } else {//Is my position
                    getCurrentAddress(marker.getPosition());
                }
                return true;
            }
        });*/
    }

    /**************************************************************************/

    protected void createLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

   /* private Route getRoute(int id){
        int size = routesList.size();
        for (int i = 0; i < size; i++ ){
            Route r = routesList.get(i);
            if (r.getId() == id){
                return r;
            }
        }
        return null;
    }*/

    /**
     * Route's Binary search
     * @param key id to search
     * @return Route element if found, null otherwise
     */
    private Route getRoute(int key) {

        int start = 0;
        int end = routesList.size() - 1;
        while (start <= end) {
            int mid = (start + end) / 2;
            Route r = routesList.get(mid);
            if (key == r.getId()) {
                return r;
            }
            if (key < r.getId()) {
                end = mid - 1;
            } else {
                start = mid + 1;
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
                route.setMarkersVisibility(true);
                showQuickInfo(route);
                float zoom = mMap.getCameraPosition().zoom;
                if (zoom < 12)
                    zoom = 12;
                CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing));
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
                closeQuickInfo();
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
        polylineOptions.width(3).color(Color.BLUE);

        pathShowed = mMap.addPolyline(polylineOptions);
    }

    private void showQuickInfo(Route route){
        if (route != null){
            TextView title = (TextView) quickInfo.getChildAt(0);
            title.setText(route.getName());
            LinearLayout itemNested = (LinearLayout) quickInfo.getChildAt(1);
            ImageView icon = (ImageView) itemNested.getChildAt(0);//TODO set icon drawable
            TextView distance = (TextView) itemNested.getChildAt(1);
            distance.setText("" + route.getDist() + " Km");
            ImageView difficult = (ImageView) itemNested.getChildAt(2);
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
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_screen);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        quickInfo.setVisibility(View.VISIBLE);
                        quickInfo.setClickable(true);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                animation.setDuration(400);
                quickInfo.startAnimation(animation);
            }
        }
    }

    private void closeQuickInfo(){
        if (quickInfo.getVisibility() != View.GONE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.out_screen);
            animation.setFillEnabled(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    quickInfo.setVisibility(View.GONE);
                    quickInfo.setClickable(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setDuration(400);
            quickInfo.startAnimation(animation);
        }
    }

    public void openDrawer(View v){
        //Toast.makeText(getApplicationContext(),"hola mundo",Toast.LENGTH_LONG).show();
        et_search.setText("");
        drawerLayout.openDrawer(Gravity.RIGHT);
    }

    public void openDrawerMenu(View v){
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    private void getCurrentAddress(final LatLng myPos){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(getApplicationContext());
                if (geocoder.isPresent()){
                    try {
                       List<Address> dirs = geocoder.getFromLocation(myPos.latitude, myPos.longitude, 1);
                        if (!dirs.isEmpty()) {
                            Address direction = dirs.get(0);
                            Message msg = new Message();
                            msg.obj = direction;
                            handlerGeocoder.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void showCurrentAddress(Address address){
        int maxLines = address.getMaxAddressLineIndex();
        if (maxLines > 0) {
            String text = "";
            text = text + address.getAddressLine(0);
            for (int i = 1; i < maxLines; i++) {
                text = text + ", ";
                text = text + address.getAddressLine(i);
            }
            globalToast.setDuration(Toast.LENGTH_LONG);
            globalToast.setText(text);
            globalToast.show();
        }
    }

    private void configureMenu(){

        drawerListMenu = (ListView)findViewById(R.id.left_drawer);
        ArrayList<DrawerItem> itemsMenu = new ArrayList<DrawerItem>();

        itemsMenu.add(new DrawerItem("Item1",R.drawable.icon_my_pos64,1));
        itemsMenu.add(new DrawerItem("Item2",R.drawable.map64,2));
        itemsMenu.add(new DrawerItem("Item3",R.drawable.filter64,3));
        itemsMenu.add(new DrawerItem("Item4",R.drawable.info64,4));
        itemsMenu.add(new DrawerItem("Item5",R.drawable.share64,5));
        drawerListMenu.setAdapter(new MenuListAdapter(getApplicationContext(), itemsMenu));

        drawerListMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: //get current position
                        if (myPos != null) {
                            getCurrentAddress(myPos.getPosition());
                            float zoom = mMap.getCameraPosition().zoom;
                            if (zoom < 14)
                                zoom = 14;
                            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(myPos.getPosition(), zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing));
                            mMap.animateCamera(cu);
                            //drawerLayout.closeDrawers();
                            closeNavigationDrawer();
                        }
                        break;
                    case 1: //change map type
                        int type = mMap.getMapType();
                        type = (type % 3) + 1;
                        mMap.setMapType(type);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(getString(R.string.MAP_TYPE), type);
                        editor.commit();
                        String text = "";
                        switch (type) {
                            case 1:
                                text = "Modo callejero";
                                break;
                            case 2:
                                text = "Modo satélite";
                                break;
                            case 3:
                                text = "Modo topográfico";
                                break;
                            default:
                                text = "" + type;
                        }
                        globalToast.setDuration(Toast.LENGTH_SHORT);
                        globalToast.setText(text);
                        globalToast.show();
                        break;
                    case 2:     //Filter
                        if (getFragmentManager().findFragmentByTag("filter") == null) {
                            DialogFragment dialogFilter = new DialogFilter();
                            dialogFilter.setCancelable(true);

                            dialogFilter.show(getFragmentManager(), "filter");
                        }
                        break;
                    case 3: //MORE INFO
                        if (getSupportFragmentManager().findFragmentByTag("FragmentDialogInfo") == null) {
                            FragmentDialogInfo dialogInfo = new FragmentDialogInfo();
                            dialogInfo.show(getSupportFragmentManager(), "FragmentDialogInfo");
                            //getFragmentManager();
                            //getSupportFragmentManager();
                        }
                        break;
                    case 4: //Share
                        String url = "https://play.google.com/store/apps/details?id=com.rutas.java";
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Rutas de Tenerife" + "\n" + url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Comparte Rutas de Tenerife"));
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "" + position, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void configureSearch() {
        et_search.setTextColor(getResources().getColor(android.R.color.white));
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RouteListAdapter rla = (RouteListAdapter) drawerList.getAdapter();
                rla.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public ArrayList<Route> getRoutesList(){
        return routesList;
    }
    public void closeNavigationDrawer(){
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
//txtName is a reference of an EditText Field
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
        drawerLayout.closeDrawers();
    }
    public void clearSearch(View v){
        if (!et_search.getText().equals(""))
            et_search.setText("");
    }

    public void closeVisiblePath(){
        if (lastRouteShowed != null){
            lastRouteShowed.isActive = false;
        }
        int size = routesList.size();
        for (int i = 0; i< size; i++){
            routesList.get(i).isActive = false;
        }

        if (pathShowed != null)
            pathShowed.remove();
        closeQuickInfo();
    }

    public Route getLastRouteShowed(){
        return lastRouteShowed;
    }

    public ClusterManager<MyMarker> getClusterManager(){
        return clusterManager;
    }
    /*private boolean isInRange(int azimuth, int angle){
        int azimuthInverse = (360 - azimuth);
        final int RANGE = 30;	//30 degrees in each side = 60

        int from = azimuthInverse - RANGE;
        if (from < 0)
            from = 360 - from;
        int to = (azimuthInverse + RANGE) % 360;

        if(from > to){
            return ((angle > from) || ( angle < to));
        } else if ( to > from){
            return ((angle < to) && ( angle > from));
        } else // to == from
            return (angle == to);
        return true;
    }*/
    private void changeAlertDividerColor(Dialog dialog){
        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(getResources().getColor(R.color.lightGreen));
    }
}

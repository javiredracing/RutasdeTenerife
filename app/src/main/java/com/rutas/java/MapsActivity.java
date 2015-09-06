package com.rutas.java;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.rutas.java.util.IabHelper;
import com.rutas.java.util.IabResult;
import com.rutas.java.util.Inventory;
import com.rutas.java.util.Purchase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        ConnectionCallbacks,
        OnConnectionFailedListener, SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment fragmentMap;

    private ClusterManager<MyMarker> clusterManager;
    private ArrayList<Route> routesList = new ArrayList<Route>();
    private BaseDatos bd;
    private LatLngBounds latLngBounds;

    private Handler handlerPath;
    private Polyline pathShowed;
    private Route lastRouteShowed;

    private LinearLayout quickInfo;
    private LinearLayout bottomMenu;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private EditText et_search;

    private boolean enableTap = false;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

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

    private AdView mAdView;

    private IabHelper mHelper;
    private boolean isPremium;
    private static final String SKU_PREMIUM = "android.test.purchased";

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
        if( tv != null) {
            tv.setGravity(Gravity.CENTER);
            tv.setShadowLayer(0,0,0,0);
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        globalToast.setView(v);

        ImageButton button = (ImageButton)findViewById(R.id.btActionMenu);
        button.setImageResource(R.mipmap.ic_launcher);

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
                        bundle.putInt(getString(R.string.VALUE_APPROVED), lastRouteShowed.approved());
                        int drawable = Utils.getIconBigger(lastRouteShowed);
                        bundle.putInt(getString(R.string.VALUE_ICON), drawable);
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
                        Tracker tracker = ((RutasTenerife)getApplication()).getTracker();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Extended-Info")
                                .setAction("Show")
                                .setLabel("" + lastRouteShowed.getName())
                                .build());
                        extendedInfo.setArguments(bundle);
                        extendedInfo.show(getSupportFragmentManager(), "FragmentDialogExtendedInfo");
                    }
                }
            }
        });
        //initializing lower menu
        bottomMenu = (LinearLayout) findViewById(R.id.llBottomMenu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.right_drawer);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(),routesList.get(position).getName(), Toast.LENGTH_LONG).show();
                //et_search.clearFocus();
                RouteListAdapter rla = (RouteListAdapter) drawerList.getAdapter();
                int routeId = rla.getItemIdAtPosition(position);
                closeNavigationDrawer();

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
            Toast.makeText(getApplicationContext(),getString(R.string.error_loading), Toast.LENGTH_SHORT).show();
            finish();
        }

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        /* In app billing*/
        isPremium = false;
        mHelper = new IabHelper(this, getString(R.string.Base64EncodedPublicKey));
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()){
                    Log.v("onIabSetupFinished", "Problem setting up In-app Billing: " + result.getMessage());

                }
                else {
                    Log.v("onIabSetupFinished", "In-app Billing OK!");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            }
        });
        /*Load admob*/
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mAdView.getVisibility() == View.GONE)
                    mAdView.setVisibility(View.VISIBLE);
                Log.i("Ads", "onAdLoaded");
            }
        });

    }

    /**********In app billing overrides *********/
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            isPremium = false;
            if ((mHelper != null) && (!result.isFailure())) {
                Purchase premiumPurchase = inv.getPurchase(SKU_PREMIUM);
                isPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            }
            if (!isPremium){
                loadAdRequest();
            }else
                Toast.makeText(getApplicationContext(),"PREMIUM version!", Toast.LENGTH_LONG).show();
            Log.v(SKU_PREMIUM,""+isPremium);
        }
    };

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

                //Log.d("onResume", "Location update resumed .....................");
            }
            if (accelerometer != null && magnetometer != null){
                sensorManager.registerListener(this, accelerometer, 330000);
                sensorManager.registerListener(this, magnetometer, 330000); //3 times per second
            }
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
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage(getString(R.string.quit_app))
                    .setNegativeButton(getString(R.string.No), null)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

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
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;

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

        //Log.v("Connected", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.v("onConnectionSuspended", "Connection suspended!");
        if (myPos != null){
            myPos.remove();
            myPos = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //mCurrentLocation
        // = location;
        if (myPos == null){
            int icon = R.drawable.my_pos24_center_no_bearing;
            if (accelerometer != null && magnetometer != null){
                icon = R.drawable.my_pos24_center;
            }
            myPos = mMap.addMarker(new MarkerOptions().
                            position(new LatLng(28.299221, -16.525690))
                            .icon(BitmapDescriptorFactory.fromResource(icon))
                            .title(getString(R.string.my_position))
                            .anchor(0.5f, 0.5f)
                            .flat(true)
                            .draggable(false)
            );
        }else
            myPos.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        //myPos.setRotation(30);
        //Log.v("Location", mCurrentLocation.toString());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.v("Connection failed", "FAILED!");
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
                globalToast.setText(getString(R.string.error_loading_db));
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
                        if (mMap != null){
                            if (latLngBounds != null) {
                                Log.v("Loading Map",latLngBounds.toString());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20));
                            }/*else
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.299221, -16.525690), 12));*/
                        }
                    }
                });
            }/*else{
                if (mMap!= null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.299221, -16.525690), 12));
            }*/
        }
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;
        super.onActivityResult(requestCode, resultCode, data);
    }*/

/************************************************************************************/
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
                    if (zoom < 20 ){
                        if (zoom < 10)
                            zoom = 10;
                        else
                            zoom++;
                    }
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),zoom);
                    mMap.animateCamera(cu, 600, null);
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
                Route route = new Route(id,nombre,kml,dist,dific, durac,approved, region);
                MyMarker m = new MyMarker(inicLat, inicLong, nombre, id, approved);
                clusterManager.addItem(m);
                boundsBuilder.include(geopoint);
           //insert region at the end
                route.setMarker(m);
                if ((finLat != 0) && (finLon != 0)){
                    LatLng geopoint2 = new LatLng(finLat, finLon);
                    MyMarker m2 = new MyMarker(finLat, finLon, nombre, id, approved);
                    clusterManager.addItem(m2);
                    boundsBuilder.include(geopoint2);
                    route.setMarker(m2);
                }
                routesList.add(route);
            }
            drawerList.setAdapter(new RouteListAdapter(getApplicationContext(), routesList));
        }
        c.close();
        latLngBounds = boundsBuilder.build();
        int map_type = prefs.getInt(getString(R.string.MAP_TYPE),3);
        googleMap.setMapType(map_type);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        //Google analytics
        Tracker tracker = ((RutasTenerife)getApplication()).getTracker();
        tracker.setScreenName("main_map");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        AppRater.app_launched(this);
        boolean isFirstTime = prefs.getBoolean(getString(R.string.VALUE_FIRST_TIME), true);
        if (isFirstTime){
            //if (getSupportFragmentManager().findFragmentByTag("FragmentDialogInfo") == null) {
                FragmentDialogInfo dialogInfo = new FragmentDialogInfo();
                dialogInfo.show(getSupportFragmentManager(), "FragmentDialogInfo");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(getString(R.string.VALUE_FIRST_TIME), false);
                editor.apply();
                if (globalToast != null){
                    globalToast.setText("Press back to continue");
                    globalToast.setDuration(Toast.LENGTH_LONG);
                    globalToast.show();
                }
            //}
        }
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

            if ((route.isActive) && (!route.getXmlRoute().equals(""))){
                route.setMarkersVisibility(true);
                showQuickInfo(route);
                showBottomMenu();
                float zoom = mMap.getCameraPosition().zoom;
                if (zoom < 12)
                    zoom = 12;
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoom);
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
                closeBottomMenu();
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
        int color = Color.BLUE;
        if (lastRouteShowed!= null)
            color = Utils.selectColor(lastRouteShowed.approved(), getApplicationContext());
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(path);
        polylineOptions.width(3).color(color);
        pathShowed = mMap.addPolyline(polylineOptions);
    }

    private void showQuickInfo(Route route){
        if (route != null){
            TextView title = (TextView) quickInfo.getChildAt(0);
            title.setText(route.getName());
            LinearLayout itemNested = (LinearLayout) quickInfo.getChildAt(1);
            ImageView icon = (ImageView) itemNested.getChildAt(0);
            int drawable = Utils.getIconBigger(route);
            icon.setImageResource(drawable);
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

    private void showBottomMenu(){
        if (bottomMenu.getVisibility() == View.GONE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animate_on);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    bottomMenu.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setDuration(400);
            bottomMenu.startAnimation(animation);
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

    private void closeBottomMenu(){
        if (bottomMenu.getVisibility() != View.GONE) {
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animate_off);
            animation.setFillEnabled(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    bottomMenu.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setDuration(400);
            bottomMenu.startAnimation(animation);
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



    public void actionCenter(View v){
        if( (lastRouteShowed != null) && lastRouteShowed.isActive && (pathShowed != null)){

            LatLngBounds bounds = Utils.centerOnPath(pathShowed.getPoints());
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30), 600, null);
        }
    }

    public void actionSharePath(View v){

        if (lastRouteShowed != null && lastRouteShowed.isActive) {
            final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    Log.v("onSnapShot","Processing bitmap!");
                    Bitmap b = Utils.exportBitmap(getApplicationContext(), bitmap, lastRouteShowed.getName());
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), b, lastRouteShowed.getName(), "" + lastRouteShowed.getDist() + " Km");
                    //progress.dismiss();
                    Uri imageUri = Uri.parse(path);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/jpeg");
                    share.putExtra(Intent.EXTRA_STREAM, imageUri);
                    startActivity(Intent.createChooser(share,getString(R.string.selectShare)));
                   Log.v("onSnapShot","Bitmap ready!");
                }
            };
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    Log.v("onSnapShot", "Map loaded");
                    mMap.snapshot(callback);
                }
            });
            LatLngBounds bounds = Utils.centerOnPath(pathShowed.getPoints());
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));
            globalToast.setText("generating share image");
            globalToast.setDuration(Toast.LENGTH_LONG);
            globalToast.show();
            //progress = ProgressDialog.show(this, "Loading","Generating image", true);
        }
    }

    public void actionCloseQuickInfo(View v){
        if (lastRouteShowed != null && lastRouteShowed.isActive) {
            lastRouteShowed.isActive = false;
            if (pathShowed != null)
                pathShowed.remove();
            closeBottomMenu();
            closeQuickInfo();
        }
    }

    private void getCurrentAddress(final LatLng myPos){
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (Geocoder.isPresent()){
                    Geocoder geocoder = new Geocoder(getApplicationContext());
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

        ListView drawerListMenu = (ListView)findViewById(R.id.left_drawer);
        ArrayList<DrawerItem> itemsMenu = new ArrayList<DrawerItem>();

        itemsMenu.add(new DrawerItem("Item1",R.drawable.icon_my_pos64,1));
        itemsMenu.add(new DrawerItem("Item2",R.drawable.map64,2));
        itemsMenu.add(new DrawerItem("Item3",R.drawable.simple_filter_64,3));
        itemsMenu.add(new DrawerItem("Item4",R.drawable.info64,4));
        itemsMenu.add(new DrawerItem("Item5",R.drawable.custom_share_64,5));
        itemsMenu.add(new DrawerItem("Item6", R.drawable.unlock,6));
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
                        } else {
                            globalToast.setDuration(Toast.LENGTH_LONG);
                            globalToast.setText(getString(R.string.error_my_position));
                            globalToast.show();
                        }
                        break;
                    case 1: //change map type
                        int type = mMap.getMapType();
                        type = (type % 3) + 1;
                        mMap.setMapType(type);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(getString(R.string.MAP_TYPE), type);
                        editor.apply();
                        String text = "";
                        switch (type) {
                            case 1:
                                text = getString(R.string.road_map);
                                break;
                            case 2:
                                text = getString(R.string.satellite_map);
                                break;
                            case 3:
                                text = getString(R.string.terrain_map);
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
                        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText) + "\n" + url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.selectShare)));
                        //Event google analytics
                        Tracker tracker = ((RutasTenerife) getApplication()).getTracker();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Menu")
                                .setAction("Share")
                                .setLabel("-")
                                .build());
                        break;
                    case 5:
                        if (getSupportFragmentManager().findFragmentByTag("unlock") == null) {
                            FragmentDialogUnlock dialogFilter = new FragmentDialogUnlock();
                            dialogFilter.setCancelable(true);

                            dialogFilter.show(getSupportFragmentManager(), "unlock");
                        }
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "" + position, Toast.LENGTH_SHORT).show();
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
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //txtName is a reference of an EditText Field
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
        drawerLayout.closeDrawers();
    }
    public void clearSearch(View v){
        if (!et_search.getText().toString().equals(""))
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
        closeBottomMenu();
    }

    public Route getLastRouteShowed(){
        return lastRouteShowed;
    }

    public ClusterManager<MyMarker> getClusterManager(){
        return clusterManager;
    }

    private void changeAlertDividerColor(Dialog dialog){
        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(getResources().getColor(R.color.lightGreen));
    }

    private boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
    return true;
    }

    private void loadAdRequest(){
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("CA2BE49BA2ED1F54697680DE461F4048")
                .addTestDevice("922A0081AE099EE4E45D7D8D868D9153")
                .addTestDevice("FC3F6D621E3F691163F3081D23209CD7")
                .addTestDevice("E31D8CAD5C7EC0199DF56FCDD1C8BACA")
                .build();
        //AdRequest ad = adRequest.build();
        mAdView.loadAd(adRequest);
    }

}

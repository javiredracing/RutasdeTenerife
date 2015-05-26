package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MapsActivity extends Activity implements OnMapReadyCallback, LocationListener,
        ConnectionCallbacks,
        OnConnectionFailedListener, SensorEventListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment fragmentMap;
    //private ArrayList<Marker> markerList = new ArrayList<Marker>();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        prefs = this.getSharedPreferences("options", Context.MODE_PRIVATE);
        quickInfo = (LinearLayout) findViewById(R.id.layoutQuickInfo);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.right_drawer);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(),routesList.get(position).getName(), Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawers();
                Route r = routesList.get(position);
                clickAction(r, r.getFirstPoint());
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
        }
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
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
        sensorManager.unregisterListener(this);
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
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU){
            //Toast.makeText(getApplicationContext(),"menu key", Toast.LENGTH_SHORT).show();
            if (drawerLayout.isDrawerOpen(Gravity.LEFT))
                drawerLayout.closeDrawers();
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
            drawerLayout.closeDrawers();
        }else{
            Dialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setIcon(R.drawable.icon_my_pos64)
                    .setMessage("Quit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapsActivity.super.onBackPressed();
                        }
                    }).create();
            d.show();
        }
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
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_pos))
                            .title("myPos")
                            .anchor(0.5f, 0.5f)
                            .flat(true)
                            .draggable(false)
            );
        else
            myPos.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        //myPos.setRotation(30);
        Log.v("Location", mCurrentLocation.toString());
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
                throw sqle;
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
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
                        Log.v("Orientation",""+azimuthFinal);
                    }

                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
        Cursor c = bd.getInfoMap(false, new String[]{"nombre", "inicX", "inicY", "finX", "finY", "duracion", "longitud", "dificultad", "kml", "id"}, null, null, null, null, null);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        if (c.getCount() > 0){
            String nombre;
            //String dificultad;
            String kml = "";
            //ArrayList<Route> items = new ArrayList<Route>();
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
                boundsBuilder.include(geopoint);
                //markerList.add(m);
                //Update List
               //items.add(new DrawerItem(nombre, R.drawable.my_pos));
                Route route = new Route(id,nombre,kml,dist,dific);
                route.setPoint(geopoint);
                if ((finX != 0) && (finY != 0)){
                    LatLng geopoint2 = new LatLng(finX, finY);
                    m = googleMap.addMarker(new MarkerOptions().
                            position(geopoint2).
                            title(nombre).snippet(""+id));
                    //markerList.add(m);
                    boundsBuilder.include(geopoint2);
                    route.setPoint(geopoint2);
                }
                routesList.add(route);
            }
            drawerList.setAdapter(new RouteListAdapter(getApplicationContext(),routesList));
        }
        c.close();
        //CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(28.299221, -16.525690), 10);
        latLngBounds = boundsBuilder.build();
        //CameraUpdate center = CameraUpdateFactory.newLatLngBounds(latLngBounds,0);
        //googleMap.moveCamera(center);
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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
        });
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
            Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();
        }
    }

    private void configureMenu(){

        drawerListMenu = (ListView)findViewById(R.id.left_drawer);
        ArrayList<DrawerItem> itemsMenu = new ArrayList<DrawerItem>();

        boolean hasGps = prefs.getBoolean("gps", false);
/*        int icon = R.drawable.gps_off64;
        if (hasGps)
            icon = R.drawable.gps_on64;
        itemsMenu.add(new DrawerItem("Item1",icon, 0));*/
        itemsMenu.add(new DrawerItem("Item2",R.drawable.icon_my_pos64,1));
        itemsMenu.add(new DrawerItem("Item3",R.drawable.map64,2));
        itemsMenu.add(new DrawerItem("Item4",R.drawable.filter64,3));
        itemsMenu.add(new DrawerItem("Item6",R.drawable.info64,4));
        itemsMenu.add(new DrawerItem("Item7",R.drawable.share64,5));
        drawerListMenu.setAdapter(new MenuListAdapter(getApplicationContext(), itemsMenu));
        TextView textView = new TextView(this);
        textView.setText("Options");
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        drawerListMenu.addHeaderView(textView);
        drawerListMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1:
                        if (myPos!= null){
                            getCurrentAddress(myPos.getPosition());
                            float zoom = mMap.getCameraPosition().zoom;
                            if (zoom < 14)
                                zoom = 14;
                            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(myPos.getPosition(), zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing));
                            mMap.animateCamera(cu);
                            drawerLayout.closeDrawers();
                        }
                        break;
                    case 2:
                        int type = mMap.getMapType();
                        type = (type%3) + 1;
                        mMap.setMapType(type);
                        break;
                    case 3://TODO FILTER
                        break;
                    case 4: //TODO MORE INFO
                        break;
                    case 5:
                        String url = "https://play.google.com/store/apps/details?id=com.rutas.java";
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Rutas de Tenerife"+"\n"+url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Comparte Rutas de Tenerife"));
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),""+position,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void configureSearch() {
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RouteListAdapter rla = (RouteListAdapter)drawerList.getAdapter();
                rla.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}

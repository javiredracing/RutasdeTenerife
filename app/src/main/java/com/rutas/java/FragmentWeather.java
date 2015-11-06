package com.rutas.java;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.Locale;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentWeather extends Fragment {
    private Handler handlerWeather;
    private TextView tvWeatherDesc, tvCloudCover,tvHumidity,tvPrecip,tvPressure,tvTemp,tvWindDir,tvWindVel, tvTimeZone;
    private ImageView iconWeather,imgVWinDir;
    private double[] myLatLng;
    private String jsonWeather;
    private View v, forecast1, forecast2, forecast3;
    private Button btNextDays;

    private boolean isPremium;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (v == null){
            //Log.v("OnCreate", "Recreating Weather Fragment");
            //Getting json weather from cache
            //isPremium = getArguments().getBoolean(getString(R.string.VALUE_IS_PREMIUM), false);
            MapsActivity mainActivity = (MapsActivity)getActivity();
            isPremium = mainActivity.isPremium();
            jsonWeather = mainActivity.getLastRouteShowed().getWeatherJson();

            Bundle arguments = getArguments();
            myLatLng = arguments.getDoubleArray(getString(R.string.VALUE_LATLNG));
            v = inflater.inflate(R.layout.infoweather2, container, false);
            tvTimeZone = (TextView) v.findViewById(R.id.tvCurrentCondTime);
            tvWeatherDesc = (TextView) v.findViewById(R.id.tvWeatherDesc);
            iconWeather = (ImageView) v.findViewById(R.id.iconWeather);
            tvCloudCover = (TextView) v.findViewById(R.id.tvCloudCover);
            tvHumidity = (TextView) v.findViewById(R.id.tvHumidity);
            tvPrecip = (TextView) v.findViewById(R.id.tvPrecip);
            tvPressure = (TextView) v.findViewById(R.id.tvPressure);
            tvTemp = (TextView) v.findViewById(R.id.tvTemperatura);
            tvWindDir = (TextView) v.findViewById(R.id.tvWindDirec);
            imgVWinDir = (ImageView)v.findViewById(R.id.iconWindDir);
            tvWindVel = (TextView) v.findViewById(R.id.tvWindVeloc);

            forecast1 = v.findViewById(R.id.llForecast1);
            forecast1.setVisibility(View.VISIBLE);

            btNextDays = (Button)v.findViewById(R.id.btWeatherNextDays);
            if (!isPremium){
                btNextDays.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MapsActivity mainActivity = (MapsActivity)getActivity();
                        isPremium = mainActivity.isPremium();


                        if (!isPremium){
                            if (getActivity().getSupportFragmentManager().findFragmentByTag("unlock") == null) {
                                FragmentDialogUnlock dialogFilter = new FragmentDialogUnlock();
                                dialogFilter.setCancelable(false);

                                dialogFilter.show(getActivity().getSupportFragmentManager(), "unlock");
                                Tracker tracker = ((RutasTenerife) getActivity().getApplication()).getTracker();
                                tracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("Extended-Info")
                                        .setAction("next_days_weather")
                                        .setLabel("purchase_flow")
                                        .build());
                            }
                        }else{
                            setPremiumVersion();
                            callThreadWeather();
                        }
                    }
                });
            }else{
                setPremiumVersion();
            }
            handlerWeather = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //super.handleMessage(msg);
                    String mensaje = (String) msg.obj;
                    try {
                        String language_field = "lang_"+ Locale.getDefault().getLanguage();

                        JSONObject jObject = new JSONObject(mensaje);
                        JSONObject dataJson = jObject.getJSONObject("data");

                        JSONObject timeZone = dataJson.getJSONArray("time_zone").getJSONObject(0);
                        String[] date = timeZone.getString("localtime").split(" ");
                        tvTimeZone.setText("("+date[1]+" h)");

                        JSONObject currentCond = dataJson.getJSONArray("current_condition").getJSONObject(0);

                        String desc = "";
                        JSONArray jsa;
                        try {
                            jsa = currentCond.getJSONArray(language_field);
                        }catch (JSONException e){
                            jsa = currentCond.getJSONArray("weatherDesc");
                        }
                        if (jsa != null) {
                            desc = jsa.getJSONObject(0).getString("value");
                            tvWeatherDesc.setText(desc);
                        }

                        Bitmap flechaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wind_direc);
                        String value = currentCond.getString("weatherCode");
                        iconWeather.setImageResource(getDrawableResourceByName("i"+value));
                        tvCloudCover.setText(getResources().getString(R.string.nubosidad)+currentCond.getString("cloudcover")+ "%");
                        tvHumidity.setText(" "+getResources().getString(R.string.humedad)+currentCond.getString("humidity")+"%");
                        tvPrecip.setText(" "+getResources().getString(R.string.precip)+currentCond.getString("precipMM")+" Lm2");
                        tvPressure.setText(" "+getResources().getString(R.string.presion)+currentCond.getString("pressure")+" mb");
                        String paramTemp = currentCond.getString("temp_C") + " ºC";
                        if (Locale.getDefault() == Locale.US)
                            paramTemp = currentCond.getString("temp_F")+" ºF";
                        tvTemp.setText(paramTemp);
                        tvWindDir.setText("(" + currentCond.getString("winddir16Point") + ")");
                        Drawable d1 = rotateIcon(currentCond.getInt("winddirDegree"), flechaBitmap);
                        //d1.setBounds(0, 0, d1.getIntrinsicWidth(), d1.getIntrinsicHeight());

                        imgVWinDir.setImageDrawable(d1);
                        //tvWindDir.setCompoundDrawables(d1, null, null, null);
                        String cadena = currentCond.getString("windspeedKmph")+ " km/h";
                        if ((Locale.getDefault() == Locale.UK) || (Locale.getDefault() == Locale.US))
                            cadena = currentCond.getString("windspeedMiles") + " miles/h";
                        tvWindVel.setText(cadena);

                        setPrevision(forecast1,dataJson, 0, language_field,flechaBitmap);
                        if (isPremium){
                            setPrevision(forecast2,dataJson, 1, language_field,flechaBitmap);
                            setPrevision(forecast3,dataJson, 2, language_field,flechaBitmap);
                        }

                        //Update weather Route
                        MapsActivity mainActivity = (MapsActivity)getActivity();
                        mainActivity.getLastRouteShowed().setWeatherJson(mensaje);
                        Tracker tracker = ((RutasTenerife)getActivity().getApplication()).getTracker();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Weather-Api")
                                .setAction("Success")
                                .setLabel("connection")
                                .build());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Tracker tracker = ((RutasTenerife)getActivity().getApplication()).getTracker();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Weather-Api")
                                .setAction("Fails")
                                .setLabel("Parsing-json")
                                .build());
                        //Log.e("Error!", e.toString());
                    }
                }
            };

            if (jsonWeather == null){
                callThreadWeather();
            }else{	//Si result existe en cache...
               // Log.v("onCreateView", "recicling view Weather");
                Message msg = new Message();
                msg.obj = jsonWeather;
                handlerWeather.sendMessage(msg);
            }
        }
        return v;
    }

   /* private String getStringResourceByName(String aString){
        String packageName = getActivity().getPackageName();
        int resId = this.getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }*/
    private int getDrawableResourceByName(String aString){
        String packageName = getActivity().getPackageName();
        int drawableResourceId = this.getResources().getIdentifier(aString, "drawable", packageName);
        return drawableResourceId;
    }

    private Drawable rotateIcon(float grados, Bitmap flechaBitmap){

        Matrix matrix = new Matrix();
        matrix.postRotate(grados, flechaBitmap.getWidth(), flechaBitmap.getHeight());
        Bitmap rotatedBitmap = Bitmap.createBitmap(flechaBitmap, 0, 0, flechaBitmap.getWidth(), flechaBitmap.getHeight(), matrix, true);
        return new BitmapDrawable(getResources(),rotatedBitmap);
    }

    private void setPrevision(View forecast, JSONObject data, int item, String language_field, Bitmap flechaBitmap){
        JSONObject prevision = null;
        try {
            prevision = data.getJSONArray("weather").getJSONObject(item);

            TextView tvDate = (TextView)forecast.findViewById(R.id.tvForecastDate);
            String day = "";
            switch (item){
                case 0:
                    day = getResources().getString(R.string.today);
                    break;
                case 1:
                    day = getResources().getString(R.string.tomorrow);
                    break;
                default:
                    day = "";
            }
            tvDate.setText("( "+ day + " " + prevision.getString("date") + " )");

            JSONObject previsionHourly = prevision.getJSONArray("hourly").getJSONObject(0);
            String valueWeatherCode = previsionHourly.getString("weatherCode");
            ImageView iconWeather = (ImageView)forecast.findViewById(R.id.iconWeather2);
            iconWeather.setImageResource(getDrawableResourceByName("i" + valueWeatherCode));
            TextView tvPrecip = (TextView)forecast.findViewById(R.id.tvPrecip2);
            tvPrecip.setText(previsionHourly.getString("precipMM") + " Lm2");

            String temperature = prevision.getString("maxtempC") + " ºC - "+prevision.getString("mintempC")+" ºC";
            if (Locale.getDefault() == Locale.US)
                temperature = prevision.getString("maxtempF")+ " ºF - "+prevision.getString("mintempF")+" ºF";
            TextView tvTemp = (TextView)forecast.findViewById(R.id.tvTemperatura2);
            tvTemp.setText(temperature);

            String description = "";
            JSONArray jsonArray;
            try {
                jsonArray = previsionHourly.getJSONArray(language_field);
            }catch (JSONException e){
                jsonArray = previsionHourly.getJSONArray("weatherDesc");
            }
            if (jsonArray != null){
                description =jsonArray.getJSONObject(0).getString("value");
                TextView tvWeatherDescription = (TextView) forecast.findViewById(R.id.tvWeatherDesc2);
                tvWeatherDescription.setText(description);
            }
            TextView tvWindDirection = (TextView) forecast.findViewById(R.id.tvWindDirec2);
            tvWindDirection.setText("(" + previsionHourly.getString("winddir16Point") + ")");

            Drawable d2 = rotateIcon(previsionHourly.getInt("winddirDegree"), flechaBitmap);
            //d2.setBounds(0, 0, d2.getIntrinsicWidth(), d2.getIntrinsicHeight());
            //tvWindDir2.setCompoundDrawables(d2, null, null, null);
            ImageView ivWindDirection = (ImageView)forecast.findViewById(R.id.iconWindDir2);
            ivWindDirection.setImageDrawable(d2);

            String cadena2 = previsionHourly.getString("windspeedKmph")+ " km/h";
            if ((Locale.getDefault() == Locale.UK) || (Locale.getDefault() == Locale.US))
                cadena2 = previsionHourly.getString("windspeedMiles") + " miles/h";
            TextView tvWindVeloc = (TextView)forecast.findViewById(R.id.tvWindVeloc2);
            tvWindVeloc.setText(cadena2);
            //Astronomy data
            JSONObject prevAstronomy = prevision.getJSONArray("astronomy").getJSONObject(0);
            TextView tvSunrise2 = (TextView)forecast.findViewById(R.id.tvSunrise);
            tvSunrise2.setText(prevAstronomy.getString("sunrise"));
            TextView tvSunset2 = (TextView)forecast.findViewById(R.id.tvSunset);
            tvSunset2.setText(prevAstronomy.getString("sunset"));
            TextView tvMoonrise2 = (TextView)forecast.findViewById(R.id.tvMoonrise);
            tvMoonrise2.setText(prevAstronomy.getString("moonrise"));
            TextView tvMoonset2 = (TextView)forecast.findViewById(R.id.tvMoonset);
            tvMoonset2.setText(prevAstronomy.getString("moonset"));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPremiumVersion(){
        btNextDays.setVisibility(View.GONE);
        forecast2 = v.findViewById(R.id.llForecast2);
        forecast2.setVisibility(View.VISIBLE);
        forecast3 = v.findViewById(R.id.llForecast3);
        forecast3.setVisibility(View.VISIBLE);
    }

    private void callThreadWeather(){
        Thread weatherThread = new Thread(){
            @Override
            public void run(){
                String languaje = Locale.getDefault().getLanguage();
                int nDays = 1;
                if (isPremium)
                    nDays = 3;
                String uri ="http://api.worldweatheronline.com/free/v2/weather.ashx?q="+ myLatLng[0] +","+ myLatLng[1] +"&format=json&num_of_days="+nDays+"&tp=24&key="+getString(R.string.KEY_WEATHER)+"&showlocaltime=yes&lang="+languaje;
                //String uri = "http://free.worldweatheronline.com/feed/weather.ashx?q="+ lat +","+ lon +"&format=json&num_of_days=1&key=da8292f4dd111341131401";
                try {
                    URL url = new URL(uri);

                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                    urlConnection.setConnectTimeout(2000);
                    urlConnection.setReadTimeout(2000);
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"),8);
                    jsonWeather = reader.readLine();
                    in.close();
                    if (jsonWeather != null){
                        //Log.v("gettingHttpRequest", jsonWeather);
                        Message msg = new Message();
                        msg.obj = jsonWeather;
                        //Log.v("Thread weather", "Info NO cacheada!!");
                        if (handlerWeather != null)
                            handlerWeather.sendMessage(msg);
                    }
                    urlConnection.disconnect();
                } catch (IOException e) {
                    Tracker tracker = ((RutasTenerife)getActivity().getApplication()).getTracker();
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Weather-Api")
                            .setAction("Fails")
                            .setLabel("connection")
                            .build());
                    e.printStackTrace();
                }
            }
        };
        weatherThread.start();
    }
}

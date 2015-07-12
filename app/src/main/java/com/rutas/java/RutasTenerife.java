package com.rutas.java;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by jfernandez on 30/06/2015.
 */
public class RutasTenerife extends Application {

    private Tracker tracker;

    public RutasTenerife(){
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    synchronized Tracker getTracker(){
        if (tracker == null){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(true);
            analytics.setLocalDispatchPeriod(240);  //in seconds
            tracker = analytics.newTracker("UA-64659514-1");
            tracker.enableAdvertisingIdCollection(true);
            //tracker.enableAutoActivityTracking(true);
            tracker.enableExceptionReporting(true);
            //Log.v("tracker", "GA INSIDE");
        }
        //Log.v("tracker", "GA outside");
        return tracker;
    }
}

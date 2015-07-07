package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;

/**
 * Created by jfernandez on 06/07/2015.
 */
public class AppRater {
    private final static String APP_PNAME = "com.rutas.java";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +(DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                AppRater.showRateDialog(mContext, editor);
            }
        }
        editor.apply();
    }

    private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final AlertDialog d = new AlertDialog.Builder(mContext)
                .setTitle(R.string.app_name)
                .setIcon(R.drawable.logo)
                .setMessage(mContext.getString(R.string.rateit))
                .setNegativeButton(mContext.getString(R.string.No), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editor != null) {
                            editor.putBoolean("dontshowagain", true);
                            editor.apply();
                        }
                        dialog.dismiss();
                    }
                }).setNeutralButton(mContext.getString(R.string.rate_later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editor != null) {
                            editor.clear().apply();
                        }
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(mContext.getString(R.string.rate), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("market://details?id=" + APP_PNAME);
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            mContext.startActivity(goToMarket);
                            if (editor != null) {
                                editor.putBoolean("dontshowagain", true);
                                editor.apply();
                            }
                            dialog.dismiss();
                        } catch (ActivityNotFoundException e) {
                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + APP_PNAME)));
                        }
                    }
                }).create();

        d.show();
        int titleDividerId = mContext.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = d.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(mContext.getResources().getColor(R.color.lightGreen));
        //changeAlertDividerColor(d);
    }
}
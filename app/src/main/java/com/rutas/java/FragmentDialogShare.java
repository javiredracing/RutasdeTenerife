package com.rutas.java;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appinvite.AppInviteInvitation;

/**
 * Created by jfernandez on 09/09/2015.
 */
public class FragmentDialogShare extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_share, null);
        Button btInvite = (Button) view.findViewById(R.id.btInvite);
        btInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v("Share Dialog", "Invite");
                //https://developers.google.com/app-invites/android/guides/app
                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.app_name))
                        .setMessage(getString(R.string.selectShare))
                                //.setDeepLink(Uri.parse("http://www.marca.com"))
                        .build();
                startActivityForResult(intent,Utils.REQUEST_INVITE);

                dismiss();
            }
        });
        Button btShareApp = (Button)view.findViewById(R.id.btShareApp);
        btShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://play.google.com/store/apps/details?id=com.rutas.java";
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText) + "\n" + url);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.selectShare)));
                //Event google analytics
                Tracker tracker = ((RutasTenerife) getActivity().getApplication()).getTracker();
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Menu")
                        .setAction("Share")
                        .setLabel("-")
                        .build());
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setIcon(R.drawable.logo)
                .setTitle(getString(R.string.share) + " " + getString(R.string.app_name));

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Resources res = getResources();
        final int lightGreen = res.getColor(R.color.lightGreen);
        final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = getDialog().findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(lightGreen);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        MapsActivity mapsActivity = (MapsActivity)getActivity();
        mapsActivity.closeNavigationDrawer();
        super.onDismiss(dialog);
    }
}

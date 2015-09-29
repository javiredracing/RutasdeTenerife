package com.rutas.java;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Javi on 18/08/2015.
 */
public class FragmentDialogUnlock extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.unlock_view, null))
        .setNegativeButton(getString(R.string.no_thanks), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .setPositiveButton(getString(R.string.donate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapsActivity activity = (MapsActivity)getActivity();
                        activity.mHelper.launchPurchaseFlow(getActivity(), Utils.SKU_PREMIUM, Utils.PURCHASE_CODE_REQUEST, activity.mPurchaseFinishedListener);
                        //activity.mHelper.launchSubscriptionPurchaseFlow(getActivity(), Utils.SKU_PREMIUM, Utils.PURCHASE_CODE_REQUEST, activity.mPurchaseFinishedListener);
                        dismiss();
                    }
                })
                .setIcon(R.drawable.logo)
                .setTitle(getString(R.string.app_name) + " " + "Premium " + getString(R.string.price));

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

package com.rutas.java;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.rutas.java.util.IabHelper;

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
        .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .setPositiveButton("Donate!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "DONATE!",Toast.LENGTH_SHORT).show();
                        MapsActivity activity = (MapsActivity)getActivity();
                        activity.mHelper.launchPurchaseFlow(getActivity(), Utils.SKU_PREMIUM, Utils.PURCHASE_CODE_REQUEST, activity.mPurchaseFinishedListener);
                        dismiss();
                    }
                })
                .setIcon(R.drawable.logo)
                .setTitle("Rutas de Tenerife Premium");

        return builder.create();
    }
}

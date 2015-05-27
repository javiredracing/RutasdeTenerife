package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by jfernandez on 27/05/2015.
 */
public class DialogFilter extends DialogFragment {
    private Spinner spinnerLong;
    private Spinner spinnerDurac;
    private Spinner spinnerDif;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_filter, null);
        SharedPreferences preferences = getActivity().getSharedPreferences("options", Context.MODE_PRIVATE);
        //preferences.get
        spinnerLong = (Spinner)v.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.opcionLong, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLong.setAdapter(adapter1);
        spinnerDurac =(Spinner)v.findViewById(R.id.spinner2);
        /*ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.opcionLong, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/

        spinnerDurac.setAdapter(adapter1);
        spinnerDif = (Spinner)v.findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.opcionDific, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDif.setAdapter(adapter2);

        builder.setView(v);
        builder.setTitle("Filtro");
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("DialogFilter", "OK");
                dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("DialogFilter", "Cancel");
            }
        });
        builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("DialogFilter", "Clear");
            }
        });
        AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(true);
       // ad.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return ad;
    }
}

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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by jfernandez on 27/05/2015.
 */
public class DialogFilter extends DialogFragment {
    private Spinner spinnerLong;
    private Spinner spinnerDurac;
    private Spinner spinnerDif;
    private SharedPreferences preferences;
    private MapsActivity mapsActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mapsActivity = (MapsActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_filter, null);
        preferences = getActivity().getSharedPreferences("options", Context.MODE_PRIVATE);
        //preferences.get
        spinnerLong = (Spinner)v.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.opcionLong, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLong.setAdapter(adapter1);
        spinnerLong.setSelection(preferences.getInt(getString(R.string.FILTER_LONG), 0));

        spinnerDurac =(Spinner)v.findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.opcionLong, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDurac.setAdapter(adapter2);
        spinnerDurac.setSelection(preferences.getInt(getString(R.string.FILTER_DURAC), 0));
        spinnerDif = (Spinner)v.findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getActivity(), R.array.opcionDific, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDif.setAdapter(adapter3);
        spinnerDif.setSelection(preferences.getInt(getString(R.string.FILTER_DIF), 0));
        builder.setView(v);
        builder.setTitle("Filtro");
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = preferences.edit();
                int spnLong = spinnerLong.getSelectedItemPosition();
                int spnDurac = spinnerDurac.getSelectedItemPosition();
                int spnDif = spinnerDif.getSelectedItemPosition();
                editor.putInt(getString(R.string.FILTER_LONG), spnLong);
                editor.putInt(getString(R.string.FILTER_DURAC), spnDurac);
                editor.putInt(getString(R.string.FILTER_DIF), spnDif);
                editor.commit();
                ArrayList<Route> list = mapsActivity.getRoutesList();
                filterRoute(spnLong, spnDif, spnDurac, list);

                //Log.v("DialogFilter", "OK " + spnLong + " " + spnDurac + " " + spnDif);
                dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.v("DialogFilter", "Cancel");
                dismiss();
            }
        });
        builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.v("DialogFilter", "Clear");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(getString(R.string.FILTER_LONG),0);
                editor.putInt(getString(R.string.FILTER_DURAC),0);
                editor.putInt(getString(R.string.FILTER_DIF), 0);
                editor.commit();

                ArrayList<Route> list = mapsActivity.getRoutesList();
                filterRoute(0, 0, 0, list);
                dismiss();
            }
        });
        AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(true);
       // ad.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return ad;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mapsActivity.closeNavigationDrawer();
        super.onDismiss(dialog);
    }

    private void filterRoute(int lon, int dif, int durac, ArrayList<Route> list){
        int size = list.size();
        for (int i = 0; i < size; i++){
            Route r = list.get(i);
            boolean lonCond = false;
            boolean difCond = false;
            boolean duracCond = false;
            switch (lon){
                case 0:
                    lonCond = true;
                    break;
                case 1:
                    if (r.getDist() < 11)
                        lonCond = true;
                    break;
                case 2:
                    if ((r.getDist() >= 11) && (r.getDist()<50))
                        lonCond = true;
                    break;
                default:
                    if (r.getDist() >= 50)
                        lonCond = true;
            }
            switch (dif){
                case 0:
                    difCond = true;
                    break;
                case 1:
                    if (r.getDifficulty() == 1)
                        difCond = true;
                    break;
                case 2:
                    if (r.getDifficulty() == 2)
                        difCond = true;
                    break;
                case 3:
                    if (r.getDifficulty() == 3)
                        difCond = true;
                    break;
                default:
                    if (r.getDifficulty() > 3)
                        difCond = true;
            }

            switch (durac){
                case 0:
                    duracCond = true;
                    break;
                case 1:
                    if (r.getDurac() < 2){
                        duracCond = true;
                    }
                    break;
                case 2:
                    if ((r.getDurac() >=2) && (r.getDurac()<6)){
                        duracCond = true;
                    }
                    break;
                case 3:
                    if (r.getDurac() >=6)
                        duracCond = true;
                    break;
                default:
                    duracCond = true;
            }

            boolean result = (difCond && duracCond && lonCond);
           /* if (result)
                Log.v("filterOK", ""+result + " difCond " + difCond+" duracCond "+ duracCond+" lonCond" +lonCond);
            else
                Log.v("filterFalse",""+result);*/
            r.setMarkersVisibility(result);
        }
    }
}

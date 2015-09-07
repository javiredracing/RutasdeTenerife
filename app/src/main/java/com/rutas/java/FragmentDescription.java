package com.rutas.java;

import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentDescription extends Fragment {
    private View v;
    private Bundle arguments;
    private String trackName;
    //private BaseDatos bdTab2;
    private Toast toast;
    private boolean isPremium;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null){
            BaseDatos bdTab2 = new BaseDatos(getActivity());
            try {
                bdTab2.abrirBD();
            }catch(SQLException sqle){
                throw sqle;
            }
            arguments = getArguments();
            v = inflater.inflate(R.layout.info_description, container, false);

            View viewNested = v.findViewById(R.id.fieldDist);
            TextView tvDist = (TextView)viewNested.findViewById(R.id.view_value);
            float dist = arguments.getFloat(getString(R.string.VALUE_DIST), 0);
            tvDist.setText("" + dist + " Km");
            TextView tvDistTitle = (TextView)viewNested.findViewById(R.id.view_title);
            tvDistTitle.setText(getString(R.string.longitude));
            ImageView tvDistIcon = (ImageView)viewNested.findViewById(R.id.view_image);
            tvDistIcon.setImageResource(R.drawable.distance32);

            viewNested = v.findViewById(R.id.fieldDific);
            TextView tvDif = (TextView)viewNested.findViewById(R.id.view_value);
            TextView tvDifTitle = (TextView)viewNested.findViewById(R.id.view_title);
            ImageView tvDifIcon = (ImageView)viewNested.findViewById(R.id.view_image);
            tvDifTitle.setText(getString(R.string.difficulty));
            int dific = arguments.getInt(getString(R.string.VALUE_DIF), 0);
            int iconDific;
            String[] dificArray = getResources().getStringArray(R.array.opcionDific);
            String text = dificArray[dific];
            switch (dific){
                case 1:
                    //text = "easy";
                    iconDific = R.drawable.nivel_facil;
                    break;
                case 2:
                    //text = "medium";
                    iconDific = R.drawable.nivel_intermedio;
                    break;
                case 3:
                    //text = "Difficult";
                    iconDific = R.drawable.nivel_dificil;
                    break;
                default:
                    iconDific = R.drawable.nivel_facil;
                    //text = "Closed";
            }
            tvDif.setText(text);
            tvDifIcon.setImageResource(iconDific);

            viewNested = v.findViewById(R.id.fieldTime);
            TextView tvTime = (TextView) viewNested.findViewById(R.id.view_value);
            tvTime.setText("" + arguments.getFloat(getString(R.string.VALUE_TIME), 0)+ " h");
            TextView tvTimeTitle = (TextView) viewNested.findViewById(R.id.view_title);
            tvTimeTitle.setText(getString(R.string.time));
            ImageView tvTimeIcon = (ImageView) viewNested.findViewById(R.id.view_image);
            tvTimeIcon.setImageResource(R.drawable.timer);

            viewNested = v.findViewById(R.id.fieldApproved);
            TextView tvApprovedTitle = (TextView) viewNested.findViewById(R.id.view_title);
            tvApprovedTitle.setText(getString(R.string.approved));
            TextView tvApproved = (TextView) viewNested.findViewById(R.id.view_value);
            int approved = arguments.getInt(getString(R.string.VALUE_APPROVED), 0);
            String mytext = getString(R.string.No);
            int icon = arguments.getInt(getString(R.string.VALUE_ICON),R.drawable.marker_sign_24_normal);
            if (approved != 0)
                mytext = getString(R.string.yes);
            tvApproved.setText(mytext);
            ImageView approvedIcon = (ImageView) viewNested.findViewById(R.id.view_image);
            ViewGroup.LayoutParams params = approvedIcon.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            approvedIcon.setImageResource(icon);

            //TODO detect languaje for getting description in BD
            int myId = arguments.getInt(getString(R.string.VALUE_ID), 0);
            trackName = bdTab2.getTrackNameById(myId);

            String desc = bdTab2.getDescriptionById(myId, "es");
            TextView tvDescription = (TextView)v.findViewById(R.id.tvTextDescriptor);
            tvDescription.setText(desc);
            bdTab2.close();

            Button btAction = (Button)v.findViewById(R.id.btAction);
            btAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double[] myLatLng = arguments.getDoubleArray(getString(R.string.VALUE_LATLNG_POS));
                    if (myLatLng != null){
                        double[] latLongPoint = arguments.getDoubleArray(getString(R.string.VALUE_LATLNG));
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?saddr=" + myLatLng[0] + "," + myLatLng[1] +
                                        "&daddr=" + latLongPoint[0] + "," + latLongPoint[1]));
                        startActivity(intent);
                    }else{
                        showToast(getString(R.string.error_my_position));
                    }
                }
            });
            isPremium = arguments.getBoolean(getString(R.string.VALUE_IS_PREMIUM), false);
            Button btGetTrack = (Button)v.findViewById(R.id.btGetTrack);
            btGetTrack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPremium){
                        if (trackName != ""){
                            File f = Utils.assetsToStorage(trackName, getActivity().getApplicationContext());
                            if (f != null){
                                //Log.v("Track", f.getAbsolutePath());
                                showToast(getString(R.string.file_saved) + " "+f.getAbsolutePath());
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                //intent.setDataAndType(Uri.fromFile(f),"application/vnd.google-earth.kml+xml");
                                intent.setDataAndType(Uri.fromFile(f),"application/xml");
                                Intent chooser = Intent.createChooser(intent, "Open the .kml file");
                                getActivity().startActivity(chooser);
                            }
                        }
                    }else{
                        if (getActivity().getSupportFragmentManager().findFragmentByTag("unlock") == null) {
                            FragmentDialogUnlock dialogFilter = new FragmentDialogUnlock();
                            dialogFilter.setCancelable(true);

                            dialogFilter.show(getActivity().getSupportFragmentManager(), "unlock");
                        }
                    }
                }
            });
        }
        return v;
    }

    private void showToast (String msg){
        if (toast == null){
            toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
            View vista = toast.getView();
            TextView tv = (TextView) vista.findViewById(android.R.id.message);
            if( tv != null) {
                tv.setGravity(Gravity.CENTER);
                tv.setShadowLayer(0,0,0,0);
                tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
            vista.setBackgroundResource(R.drawable.border_toast);
            toast.setView(vista);
        }else{
            toast.setText(msg);
        }
        toast.show();
    }
}

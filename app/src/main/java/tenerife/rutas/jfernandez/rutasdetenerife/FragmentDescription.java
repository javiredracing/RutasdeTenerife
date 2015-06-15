package tenerife.rutas.jfernandez.rutasdetenerife;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentDescription extends Fragment {
    private View v;
    private Bundle arguments;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null){
            arguments = getArguments();
            v = inflater.inflate(R.layout.info_description, container, false);
            TextView tvDist = (TextView)v.findViewById(R.id.tvDist);
            tvDist.setText(""+arguments.getFloat(getString(R.string.VALUE_DIST),0));
            TextView tvDif =(TextView)v.findViewById(R.id.tvDific);
            int dific = arguments.getInt(getString(R.string.VALUE_DIF),0);
            String text ="";
            switch (dific){
                case 1:
                    text = "easy";
                    break;
                case 2:
                    text = "medium";
                    break;
                case 3:
                    text = "Difficult";
                    break;
                default:
                    text = "Closed";
            }
            tvDif.setText(text);
            TextView tvTime = (TextView)v.findViewById(R.id.tvTime);
            tvTime.setText(""+arguments.getFloat(getString(R.string.VALUE_TIME),0));
           /* Button btAction = (Button)v.findViewById(R.id.btAction);*/
            ImageButton btAction = (ImageButton) v.findViewById(R.id.tvClickAction);
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
                        Toast toast = Toast.makeText(getActivity(), "My position not found, try enabling GPS!", Toast.LENGTH_SHORT);
                        View vista = toast.getView();
                        v.setBackgroundResource(R.drawable.border_toast);
                        toast.show();
                    }
                }
            });
        }
        return v;
    }
}

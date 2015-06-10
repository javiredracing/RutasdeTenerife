package tenerife.rutas.jfernandez.rutasdetenerife;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentWeather extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        double[] myLatLng = arguments.getDoubleArray(getString(R.string.VALUE_LATLNG));
        View v = inflater.inflate(R.layout.info_weather, container, false);
        TextView tvLatLng = (TextView) v.findViewById(R.id.tvLatLng);
        tvLatLng.setText(""+myLatLng[0] + " - "+myLatLng[1]);
        return v;
    }
}

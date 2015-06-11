package tenerife.rutas.jfernandez.rutasdetenerife;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentDescription extends Fragment {
    private View v;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null){
            Bundle arguments = getArguments();
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
        }
        return v;
    }
}

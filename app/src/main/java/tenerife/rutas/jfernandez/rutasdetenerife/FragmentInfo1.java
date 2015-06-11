package tenerife.rutas.jfernandez.rutasdetenerife;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jfernandez on 29/05/2015.
 */
public class FragmentInfo1 extends Fragment {
    private View v;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null){
            v = inflater.inflate(R.layout.info1, container, false);
        }
        return v;
    }
}

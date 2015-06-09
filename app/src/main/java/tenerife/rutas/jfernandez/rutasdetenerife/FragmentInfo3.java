package tenerife.rutas.jfernandez.rutasdetenerife;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by jfernandez on 29/05/2015.
 */
public class FragmentInfo3 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info3, container, false);
        Button b = (Button)view.findViewById(R.id.btContact);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "Contact", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}

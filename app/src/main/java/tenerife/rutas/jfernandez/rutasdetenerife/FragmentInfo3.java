package tenerife.rutas.jfernandez.rutasdetenerife;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by jfernandez on 29/05/2015.
 */
public class FragmentInfo3 extends Fragment {
    private View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null){
            v = inflater.inflate(R.layout.info3, container, false);
            Button b = (Button)v.findViewById(R.id.btContact);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tracker tracker = ((RutasTenerife)getActivity().getApplication()).getTracker();
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Menu")
                            .setAction("Send-Email")
                            .setLabel("-")
                            .build());
                    Intent i = new Intent(Intent.ACTION_SEND);
                    //i.setType("text/plain");
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"rutastenerife@gmail.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, "Rutas de Tenerife, User");
                    //i.putExtra(Intent.EXTRA_TEXT, "Texto");
                    try{
                        String texto = getString(R.string.selecciona_cliente);
                        startActivity(Intent.createChooser(i, texto));
                    }catch (android.content.ActivityNotFoundException ex){
                       Toast.makeText(getActivity(), getString(R.string.error_mail_client), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return v;
    }
}

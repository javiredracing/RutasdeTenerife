package tenerife.rutas.jfernandez.rutasdetenerife;

import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentDescription extends Fragment {
    private View v;
    private Bundle arguments;
    //private BaseDatos bdTab2;

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
            tvDistTitle.setText("Distance");
            ImageView tvDistIcon = (ImageView)viewNested.findViewById(R.id.view_image);
            tvDistIcon.setImageResource(R.drawable.distance32);

            viewNested = v.findViewById(R.id.fieldDific);
            TextView tvDif = (TextView)viewNested.findViewById(R.id.view_value);
            TextView tvDifTitle = (TextView)viewNested.findViewById(R.id.view_title);
            ImageView tvDifIcon = (ImageView)viewNested.findViewById(R.id.view_image);
            tvDifTitle.setText("Difficulty");
            int dific = arguments.getInt(getString(R.string.VALUE_DIF), 0);
            String text ="";
            int iconDific;
            switch (dific){
                case 1:
                    text = "easy";
                    iconDific = R.drawable.nivel_facil;
                    break;
                case 2:
                    text = "medium";
                    iconDific = R.drawable.nivel_intermedio;
                    break;
                case 3:
                    text = "Difficult";
                    iconDific = R.drawable.nivel_dificil;
                    break;
                default:
                    iconDific = R.drawable.nivel_facil;
                    text = "Closed";
            }
            tvDif.setText(text);
            tvDifIcon.setImageResource(iconDific);

            viewNested = v.findViewById(R.id.fieldTime);
            TextView tvTime = (TextView) viewNested.findViewById(R.id.view_value);
            tvTime.setText("" + arguments.getFloat(getString(R.string.VALUE_TIME), 0)+ " h");
            TextView tvTimeTitle = (TextView) viewNested.findViewById(R.id.view_title);
            tvTimeTitle.setText("Time");
            ImageView tvTimeIcon = (ImageView) viewNested.findViewById(R.id.view_image);
            tvTimeIcon.setImageResource(R.drawable.timer);

            viewNested = v.findViewById(R.id.fieldApproved);
            TextView tvApprovedTitle = (TextView) viewNested.findViewById(R.id.view_title);
            tvApprovedTitle.setText("Approved");
            TextView tvApproved = (TextView) viewNested.findViewById(R.id.view_value);
            boolean isApproved = arguments.getBoolean(getString(R.string.VALUE_APPROVED), false);
            String mytext = "No";
            int icon = arguments.getInt(getString(R.string.VALUE_ICON),R.drawable.marker_sign_24_normal);
            if (isApproved)
                mytext = "Yes";
            tvApproved.setText(mytext);
            ImageView approvedIcon = (ImageView) viewNested.findViewById(R.id.view_image);
            ViewGroup.LayoutParams params = approvedIcon.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;;
            approvedIcon.setImageResource(icon);

            String desc = bdTab2.getDescriptionById(arguments.getInt(getString(R.string.VALUE_ID),0), "es");
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
                        Toast toast = Toast.makeText(getActivity(), "My position not found, try enabling GPS!", Toast.LENGTH_SHORT);
                        View vista = toast.getView();
                        vista.setBackgroundResource(R.drawable.border_toast);
                        toast.show();
                    }
                }
            });
        }
        return v;
    }
}

package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by jfernandez on 29/05/2015.
 */
public class FragmentDialogInfo extends DialogFragment {
    //https://github.com/kiteflo/Android_Tabbed_Dialog
    private PagerAdapterInfo pagerAdapterInfo;
    private ViewPager viewPager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_info, container);
        pagerAdapterInfo = new PagerAdapterInfo(getChildFragmentManager());
        viewPager =(ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapterInfo);
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }
}

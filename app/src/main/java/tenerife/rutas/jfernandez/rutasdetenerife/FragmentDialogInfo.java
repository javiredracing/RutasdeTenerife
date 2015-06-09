package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;

/**
 * Created by jfernandez on 29/05/2015.
 */
public class FragmentDialogInfo extends DialogFragment {
    //https://github.com/kiteflo/Android_Tabbed_Dialog
    private ViewPager viewPager;
    private FragmentTabHost tabHost;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_tabs_info, container);

        tabHost = (FragmentTabHost) view.findViewById(R.id.tabs);
        tabHost.setup(getActivity(), getChildFragmentManager());
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("indicator1"), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("indicator2"), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("indicator3"), Fragment.class, null);

        PagerAdapterInfo pagerAdapterInfo = new PagerAdapterInfo(getChildFragmentManager());

        viewPager =(ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapterInfo);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int pos = tabHost.getCurrentTab();
                viewPager.setCurrentItem(pos);
            }
        });
        return view;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        MapsActivity mapsActivity = (MapsActivity) getActivity();
        mapsActivity.closeNavigationDrawer();
        super.onDismiss(dialog);
    }
}
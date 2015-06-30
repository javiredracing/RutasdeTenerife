package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(getString(R.string.info_path)), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(getString(R.string.info_recomendation)), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(getString(R.string.info_credits)), Fragment.class, null);
        TabWidget widget = tabHost.getTabWidget();
        int count = widget.getTabCount();
        for (int i = 0; i < count; i++){
            View v = widget.getChildAt(i);
            v.setBackgroundResource(R.drawable.apptheme_tab_indicator_holo);
            TextView tv = (TextView)v.findViewById(android.R.id.title);
            if(tv != null) {
                tv.setTextColor(getResources().getColor(R.color.gris));
            }
        }

        PagerAdapterInfo pagerAdapterInfo = new PagerAdapterInfo(getChildFragmentManager());

        viewPager =(ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapterInfo);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        Tracker tracker = ((RutasTenerife)getActivity().getApplication()).getTracker();
        tracker.setScreenName("Info");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        MapsActivity mapsActivity = (MapsActivity) getActivity();
        mapsActivity.closeNavigationDrawer();
        super.onDismiss(dialog);
    }

    /**********/
    protected class PagerAdapterInfo extends FragmentPagerAdapter {

        public PagerAdapterInfo(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            FragmentInfo1 f;
            switch (position){
                case 0:
                    FragmentInfo1 f1 = new FragmentInfo1();
                    return f1;
                case 1:
                    FragmentInfo2 f2 = new FragmentInfo2();
                    return f2;
                //break;
                case 2:
                    FragmentInfo3 f3 = new FragmentInfo3();
                    return f3;
                default:
                    f = new FragmentInfo1();

            }
            return f;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position){
                case 0:
                    title = getString(R.string.info_path);
                    break;
                case 1:
                    title = getString(R.string.info_recomendation);
                    break;
                case 2:
                    title = getString(R.string.info_credits);
                    break;
            }
            return title;
        }
    }
}

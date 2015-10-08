package com.rutas.java;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentDialogExtendedInfo extends DialogFragment {
    private FragmentTabHost tabHost;
    private ViewPager viewPager;

    private boolean isConnected;
    private int lastTab;
    private Toast toast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        View view = inflater.inflate(R.layout.dialog_extended_info, container);

        ConnectivityManager cm = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        arguments.putBoolean(getString(R.string.VALUE_IS_CONNECTED), isConnected);

        TextView tvName = (TextView) view.findViewById(R.id.tvPathNameInfo);
        tvName.setText(arguments.getString(getString(R.string.VALUE_NAME), "NAME"));
        tabHost = (FragmentTabHost)view.findViewById(R.id.tabsExtendedInfo);

        tabHost.setup(getActivity(), getChildFragmentManager());
        tabHost.addTab(tabHost.newTabSpec("spect1").setIndicator(getString(R.string.description)), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("spect2").setIndicator(getString(R.string.altitude)), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("spect3").setIndicator(getString(R.string.weather)), Fragment.class, null);
        TabWidget widget = tabHost.getTabWidget();
       /*http://android-holo-colors.com
       http://stackoverflow.com/questions/14722654/tabwidget-current-tab-bottom-line-color*/
        int count = widget.getTabCount();
        for (int i = 0; i < count; i++){
            View v = widget.getChildAt(i);
            v.setBackgroundResource(R.drawable.apptheme_tab_indicator_holo);
            TextView tv = (TextView)v.findViewById(android.R.id.title);
            if(tv != null) {
                tv.setTextColor(getResources().getColor(R.color.gris));
            }
        }
        PageAdapterExtendedInfo pagerAdapter = new PageAdapterExtendedInfo(getChildFragmentManager(), arguments);

        viewPager = (ViewPager) view.findViewById(R.id.pagerExtendedInfo);
        viewPager.setAdapter(pagerAdapter);
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
                int tab = tabHost.getCurrentTab();
                if ((!isConnected) && (tab == 2)) {
                    tabHost.setCurrentTab(lastTab);
                    toast = Toast.makeText(getActivity(), getString(R.string.error_no_internet), Toast.LENGTH_SHORT);
                    View v = toast.getView();
                    TextView tv = (TextView) v.findViewById(android.R.id.message);
                    if( tv != null) {
                        tv.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
                        tv.setGravity(Gravity.CENTER);
                        tv.setShadowLayer(0,0,0,0);
                        tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                    v.setBackgroundResource(R.drawable.border_toast);
                    toast.setView(v);
                    toast.show();
                } else {
                    if (toast != null)
                        toast.cancel();
                    lastTab = tab;
                    viewPager.setCurrentItem(tab);
                }
            }
        });
        Tracker tracker = ((RutasTenerife)getActivity().getApplication()).getTracker();
        tracker.setScreenName("Extended-Info");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setTitle(getString(R.string.title_activity_maps));

        return dialog;
    }

    /********************/
    protected class PageAdapterExtendedInfo extends FragmentPagerAdapter {
        private Bundle arguments;
        private int pageNumber;

        public PageAdapterExtendedInfo(FragmentManager fm, Bundle b){
            super(fm);
            arguments = b;
            if (b.getBoolean(getString(R.string.VALUE_IS_CONNECTED),true)){
                pageNumber = 3;
            }else
                pageNumber = 2; //prevent loading fragment weather if no connection available
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    FragmentDescription fd = new FragmentDescription();
                    fd.setArguments(arguments);
                    return fd;
                case 1:
                    FragmentChart fc = new FragmentChart();
                    fc.setArguments(arguments);
                    return fc;
                case 2:
                    FragmentWeather fw = new FragmentWeather();
                    fw.setArguments(arguments);
                    return fw;
                default:
                    FragmentDescription fd1 = new FragmentDescription();
                    fd1.setArguments(arguments);
                    return fd1;
                }
            //return null;
        }

        @Override
        public int getCount() {
            return pageNumber;
        }
    }
}



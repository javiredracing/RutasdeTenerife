package tenerife.rutas.jfernandez.rutasdetenerife;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentDialogExtendedInfo extends DialogFragment {
    private FragmentTabHost tabHost;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        View view = inflater.inflate(R.layout.dialog_extended_info, container);

        TextView tvName = (TextView) view.findViewById(R.id.tvPathNameInfo);
        tvName.setText(arguments.getString(getString(R.string.VALUE_NAME), "NAME"));
        tabHost = (FragmentTabHost)view.findViewById(R.id.tabsExtendedInfo);

        tabHost.setup(getActivity(), getChildFragmentManager());
        tabHost.addTab(tabHost.newTabSpec("spect1").setIndicator("Descript"), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("spect2").setIndicator("Chart"), Fragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("spect3").setIndicator("Weather"), Fragment.class, null);

        PageAdapterExtendedInfo pagerAdapter = new PageAdapterExtendedInfo(getChildFragmentManager(), arguments);

        viewPager = (ViewPager) view.findViewById(R.id.pagerExtendedInfo);
        viewPager.setAdapter(pagerAdapter);
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
                viewPager.setCurrentItem(tabHost.getCurrentTab());
            }
        });
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
       /* public PageAdapterExtendedInfo(FragmentManager fragmentManager){
            super(fragmentManager);
        }*/

        public PageAdapterExtendedInfo(FragmentManager fm, Bundle b){
            super(fm);
            arguments = b;
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
            return 3;
        }
    }
}



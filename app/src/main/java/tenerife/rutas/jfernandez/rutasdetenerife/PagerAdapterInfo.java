package tenerife.rutas.jfernandez.rutasdetenerife;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by jfernandez on 29/05/2015.
 */
public class PagerAdapterInfo extends FragmentPagerAdapter{

    public PagerAdapterInfo(FragmentManager fragmentManager){
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f;
        switch (position){
            case 0:
                f = new FragmentInfo1();
                break;
            case 1:
                f = new FragmentInfo2();
                break;
            case 2:
                f = new FragmentInfo3();
                break;
            default:
                f = new FragmentInfo1();
        }
        return null;
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
                title = "Page1";
                break;
            case 1:
                title = "Page2";
                break;
            case 2:
                title = "Page3";
                break;
        }
        return title;
    }
}


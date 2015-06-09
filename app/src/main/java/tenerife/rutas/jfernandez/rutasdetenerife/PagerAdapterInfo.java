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


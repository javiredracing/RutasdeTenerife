package tenerife.rutas.jfernandez.rutasdetenerife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Javi on 24/05/2015.
 */
public class MenuListAdapter extends ArrayAdapter {
    public MenuListAdapter(Context context, List objects) {
        super(context, 0,objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_menu_item, null);
        }
        ImageView icon = (ImageView) convertView.findViewById(R.id.imageview_menu);

        DrawerItem item = (DrawerItem) getItem(position);
        icon.setImageResource(item.getIconId());
        return convertView;
    }
}

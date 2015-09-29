package com.rutas.java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Javi on 24/05/2015.
 */
public class MenuListAdapter extends ArrayAdapter {
    private LayoutInflater inflater;

    public MenuListAdapter(Context context, List objects) {
        super(context, 0,objects);
        inflater = inflater.from(context);
    }

    protected class ViewHolder2 {
        protected ImageView icon;
        protected TextView description;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder2 viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder2();
            //LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_menu_item, null);
            viewHolder.icon = (ImageView)convertView.findViewById(R.id.imageview_menu);
            viewHolder.description = (TextView)convertView.findViewById(R.id.tvDescMenuItem);
            convertView.setTag(viewHolder);
            convertView.setOnLongClickListener(null);
            convertView.setLongClickable(false);
        }else
            viewHolder = (ViewHolder2)convertView.getTag();

        //ImageView icon = viewHolder.icon;

        DrawerItem item = (DrawerItem) getItem(position);
        viewHolder.icon.setImageResource(item.getIconId());
        viewHolder.description.setText(item.getName());
        return convertView;
    }
}

package com.rutas.java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfernandez on 26/05/2015.
 */
public class RouteListAdapter extends BaseAdapter {

    //private Context mContext;
    private static final int NUMBER_OF_REGIONS = 6;

    private LayoutInflater inflater;
    private ArrayList<Route> arrayList;
    //private Context ctx;

    private ArrayList<ListItem> routesList;
    private String zonaN, zonaS;

    public RouteListAdapter(Context _ctx, List<Route> routes){

        routesList = new ArrayList<>();
        insertItems(routes);
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(routes);
        //ctx = _ctx;
        this.inflater = inflater.from(_ctx);
        zonaN = _ctx.getResources().getString(R.string.north_zone);
        zonaS = _ctx.getResources().getString(R.string.south_zone);
;
    }
    protected class ViewHolder {
        public TextView name;
        public ImageView icon;
        public TextView distance;
    }

    @Override
    public int getCount() {
        return routesList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (routesList.get(position).isSection())
            return 1;
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
            return routesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
//http://bartinger.at/listview-with-sectionsseparators/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final ListItem listItem = routesList.get(position);
        if (listItem != null){
            if (listItem.isSection()){
                SectionListItem sectionListItem = (SectionListItem)listItem;
                if (convertView == null){
                    viewHolder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.list_section_header,null);
                    viewHolder.name = (TextView)convertView.findViewById(R.id.textSeparator);
                    convertView.setTag(viewHolder);
                    convertView.setOnClickListener(null);
                    convertView.setOnLongClickListener(null);
                    convertView.setLongClickable(false);
                }else
                    viewHolder = (ViewHolder)convertView.getTag();
                viewHolder.name.setText(sectionListItem.getName());

            }else {
                EntryListItem entryListItem = (EntryListItem)listItem;
                if (convertView == null){
                    viewHolder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.drawer_list_item,null);
                    viewHolder.name = (TextView)convertView.findViewById(R.id.name);
                    viewHolder.icon = (ImageView)convertView.findViewById(R.id.icon);
                    viewHolder.distance = (TextView) convertView.findViewById(R.id.tvListItemDistance);
                    convertView.setTag(viewHolder);
                }else
                    viewHolder = (ViewHolder)convertView.getTag();
                viewHolder.name.setText(entryListItem.getName());
                viewHolder.distance.setText("(" + entryListItem.getDistance() + " Km)");
                int icon;
                switch (entryListItem.getDifficulty()){
                    case 1:
                        icon = R.drawable.nivel_facil;
                        break;
                    case 2:
                        icon = R.drawable.nivel_intermedio;
                        break;
                    case 3:
                        icon = R.drawable.nivel_dificil;
                        break;
                    default:
                        icon = R.drawable.nivel_intermedio;
                }
                viewHolder.icon.setImageResource(icon);
            }
        }
        return convertView;
    }
    //http://www.androidbegin.com/tutorial/android-search-listview-using-filter/

    /**
     * Filter listView items by Name
     * @param charText String
     */
    public void filter(String charText){
        charText = charText.toLowerCase();
        routesList.clear();
        if (charText.length() == 0){
            //routesList.addAll(arrayList);
            insertItems(arrayList);
        }else{
            if (charText.length() > 0 && charText.length()< 2){
                for (int region = 0; region < NUMBER_OF_REGIONS; region++) {
                    SectionListItem section = new SectionListItem(getRegionName(region), region);
                    routesList.add(section);
                    for (Route r : arrayList) {
                        if (r.getName().toLowerCase().startsWith(charText) && (r.getRegion() == region)) {
                            EntryListItem entry = new EntryListItem(r.getId(),r.getName(), r.getDist(), r.getDifficulty());
                            routesList.add(entry);
                        }
                    }
                }
            }else{
                for (int region = 0; region < NUMBER_OF_REGIONS; region++) {
                    SectionListItem section = new SectionListItem(getRegionName(region), region);
                    routesList.add(section);
                    for (Route r : arrayList) {
                        if (r.getName().toLowerCase().contains(charText)&& (r.getRegion() == region)) {
                            EntryListItem entry = new EntryListItem(r.getId(),r.getName(), r.getDist(), r.getDifficulty());
                            routesList.add(entry);
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    /*public ArrayList<ListItem> getArrayList() {
        return routesList;
    }*/

    public int getItemIdAtPosition(int position){
        ListItem item = routesList.get(position);
        if (!item.isSection()){
            EntryListItem entry = (EntryListItem)item;
            return entry.getId();
        }
        return -1;
    }

    private void insertItems(List<Route> list){
        int size = list.size();
        for (int region = 0; region < NUMBER_OF_REGIONS; region++) {
            SectionListItem section = new SectionListItem(getRegionName(region), region);
            routesList.add(section);
            for (int i = 0; i < size; i++) {
                Route r = list.get(i);
                if (r.getRegion() == region) {
                    EntryListItem entry = new EntryListItem(r.getId(),r.getName(), r.getDist(), r.getDifficulty());
                    routesList.add(entry);
                }
            }
        }
    }

    private String getRegionName(int region){
        String regionName = "";
        switch (region){
            case 0:
                regionName = "P. R. Anaga";
                break;
            case 1:
                regionName = zonaN;
                break;
            case 2:
                regionName = "P. R. Teno";
                break;
            case 3:
                regionName = zonaS;
                break;
            case 4:
                regionName = "P. N. Teide";
                break;
            case 5:
                regionName = "GR-131";
                break;
            default:
                regionName= "no region";
        }
        return regionName;
    }
}

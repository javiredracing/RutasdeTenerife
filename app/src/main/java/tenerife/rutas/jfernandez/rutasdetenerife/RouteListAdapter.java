package tenerife.rutas.jfernandez.rutasdetenerife;

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
    private LayoutInflater inflater;
    private List<Route> routesList;
    private ArrayList<Route> arrayList;

    public RouteListAdapter(Context ctx, List<Route> routes){
        //mContext = ctx;
        this.routesList = routes;
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(routes);
        this.inflater = inflater.from(ctx);
    }
    public class ViewHolder {
        TextView name;
        ImageView icon;
        //TextView population;
    }

    @Override
    public int getCount() {
        return routesList.size();
    }

    @Override
    public Object getItem(int position) {
        return routesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.drawer_list_item,null);
            viewHolder.name = (TextView)convertView.findViewById(R.id.name);
            viewHolder.icon = (ImageView)convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder)convertView.getTag();
        viewHolder.name.setText(routesList.get(position).getName());
        int icon;
        switch (routesList.get(position).getDifficulty()){
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
            routesList.addAll(arrayList);
        }else{
            if (charText.length() > 0 && charText.length()< 3){
                for (Route r:arrayList){
                    if (r.getName().toLowerCase().startsWith(charText)){
                        routesList.add(r);
                    }
                }
            }else{
                for (Route r:arrayList){
                    if (r.getName().toLowerCase().contains(charText)){
                        routesList.add(r);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }
}

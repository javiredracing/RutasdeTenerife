package com.rutas.java;

/**
 * Created by Javi on 12/06/2015.
 */
public class SectionListItem implements ListItem {
    private String name;
    private int region;

    public SectionListItem(String _name, int _region){
        name = _name;
        region = _region;
    }

    @Override
    public boolean isSection() {
        return true;
    }
    public String getName(){
        return name;
    }
    /*public int getRegion(){
        return region;
    }*/
}

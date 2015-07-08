package com.rutas.java;

/**
 * Created by Javi on 12/06/2015.
 */
public class EntryListItem implements ListItem {
    private int id;
    private String name;
    private float distance;
    private int difficulty;

    public EntryListItem(int _id, String _name, float _distance, int dif){
        id = _id;
        name = _name;
        distance = _distance;
        difficulty = dif;
    }
    @Override
    public boolean isSection() {
        return false;
    }
    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public float getDistance(){
        return distance;
    }

    public int getDifficulty() {
        return difficulty;
    }
}

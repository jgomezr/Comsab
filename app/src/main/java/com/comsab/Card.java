package com.comsab;

/**
 * Created by julian_dev on 2/18/2018.
 * this class is for create the cards to display in the main activity
 */

public class Card {
    private long id;
    private String name;
    private String location;
    private int color_resource;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getLocation(){
        return location;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public int getColorResource() {
        return color_resource;
    }

    public void setColorResource(int color_resource) {
        this.color_resource = color_resource;
    }
}

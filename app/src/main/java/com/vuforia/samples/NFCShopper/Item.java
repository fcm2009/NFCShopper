package com.vuforia.samples.NFCShopper;

/**
 * Created by semsem on 06/01/17.
 */

public class Item {
    public int id;
    public String name;
    public String image;

    public Item(int id, String name){
        this.id = id;
        this.name = name;
    }

    public String toString(){
        return name;
    }

    public boolean equal(Item item){
        return this.id == item.id;
    }
}

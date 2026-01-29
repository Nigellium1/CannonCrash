package com.example.furiousflingers;

//So I don't pass around strings
public enum ProjectileType implements LoadoutType{
    DEFAULT("default",1000),
    BOMB("bomb",1000),
    MAGNETIC("magnetic",1000),
    NERD("nerd",-1000 );

    private final String name;
    private final int gravity;

    ProjectileType(String name,int gravity) {
        this.name = name;
        this.gravity =gravity;
    }

    public String getName() {
        return name;
    }

    public int getGravity() {
        return gravity;
    }
}

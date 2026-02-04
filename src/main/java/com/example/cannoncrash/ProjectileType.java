package com.example.cannoncrash;

//So I don't pass around strings
public enum ProjectileType implements LoadoutType{
    DEFAULT("default"),
    BOMB("bomb"),
    MAGNETIC("magnetic"),
    NERD("nerd");

    private final String name;

    ProjectileType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

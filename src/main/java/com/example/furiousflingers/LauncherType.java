package com.example.furiousflingers;

public enum LauncherType implements LoadoutType{
    DEFAULT("default"),
    RAILGUN("railgun");

    private final String name;

    LauncherType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

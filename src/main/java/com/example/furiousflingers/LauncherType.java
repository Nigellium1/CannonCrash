package com.example.furiousflingers;

public enum LauncherType implements LoadoutType{
    DEFAULT("default",1000),
    RAILGUN("railgun",0);

    private final String name;
    private final int gravity;

    LauncherType(String name, int gravity) {
        this.name = name;
        this.gravity = gravity;
    }

    public String getName() {
        return name;
    }

    public int getGravity() {
        return gravity;
    }
}

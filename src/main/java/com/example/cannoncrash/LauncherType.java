package com.example.cannoncrash;

public enum LauncherType implements LoadoutType{
    DEFAULT("default",1000,1),
    RAILGUN("railgun",0,1.5),
    INVERTED("inverted",-1000,1);

    private final String name;
    private final int gravity;
    private final double velocityMultiplier;

    LauncherType(String name, int gravity, double velocityMultiplier) {
        this.name = name;
        this.gravity = gravity;
        this.velocityMultiplier = velocityMultiplier;
    }

    public String getName() {
        return name;
    }

    public int getGravity() {
        return gravity;
    }

    public double getVelocityMultiplier() {
        return velocityMultiplier;
    }
}

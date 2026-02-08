package com.example.cannoncrash;

public enum LauncherType implements LoadoutType{
    DEFAULT("default",1000,1,440),
    RAILGUN("railgun",0,1.5,440),
    INVERTED("inverted",-1000,1,0);

    private final String name;
    private final int gravity;
    private final double velocityMultiplier;
    private final double borderYValue; //Used for aim line

    LauncherType(String name, int gravity, double velocityMultiplier, double borderYValue) {
        this.name = name;
        this.gravity = gravity;
        this.velocityMultiplier = velocityMultiplier;
        this.borderYValue = borderYValue;
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

    public double getBorderYValue() {
        return borderYValue;
    }
}

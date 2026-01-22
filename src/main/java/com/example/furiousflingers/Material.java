package com.example.furiousflingers;

public enum Material {
    WOOD(0.1,0.4,0.2,1.6,1100,"woodRectangle"),
    STONE(0.2,0.3,0.3,2.0,1200, "stoneRectangle"),
    IMMOVABLE(0,0.3,0.2,0.1,0,"");

    private final double mass;
    private final double restitution;
    private final double staticFriction;
    private final double dynamicFriction;
    private final double momentOfInertia;
    private final String name;

    Material(double mass, double restitution, double staticFriction, double dynamicFriction, double momentOfInertia, String name) {
        this.mass = mass;
        this.restitution = restitution;
        this.staticFriction = staticFriction;
        this.dynamicFriction = dynamicFriction;
        this.momentOfInertia = momentOfInertia;
        this.name = name;
    }

    public double getMass() {
        return mass;
    }

    public double getRestitution() {
        return restitution;
    }

    public double getStaticFriction() {
        return staticFriction;
    }

    public double getDynamicFriction() {
        return dynamicFriction;
    }

    public double getMomentOfInertia() {
        return momentOfInertia;
    }

    public String getImageName() {
        return name+".png";
    }
}

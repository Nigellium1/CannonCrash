package com.example.furiousflingers;

public enum Material {
    WOOD(0.1,0.4,0.2,1.6,1100,"woodRectangle.png"),
    STONE(0.2,0.3,0.3,2.0,1200, "stoneRectangle.png"),
    IMMOVABLE(0,0.3,0.2,0.1,0,"");

    private final double mass;
    private final double restitution;
    private final double staticFriction;
    private final double dynamicFriction;
    private final double momentOfInertia;
    private final String imageName;

    Material(double mass, double restitution, double staticFriction, double dynamicFriction, double momentOfInertia, String imageName) {
        this.mass = mass;
        this.restitution = restitution;
        this.staticFriction = staticFriction;
        this.dynamicFriction = dynamicFriction;
        this.momentOfInertia = momentOfInertia;
        this.imageName = imageName;
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
        return imageName;
    }
}

package com.example.cannoncrash;

public enum Material {
    WOOD(0.1,0.4,0.2,0.6,200),
    STONE(0.2,0.3,0.3,0.8,500),
    IMMOVABLE(0,0.3,0,0,0);

    private final double mass;
    private final double restitution;
    private final double staticFriction;
    private final double dynamicFriction;
    private final double momentOfInertia;

    Material(double mass, double restitution, double staticFriction, double dynamicFriction, double momentOfInertia) {
        this.mass = mass;
        this.restitution = restitution;
        this.staticFriction = staticFriction;
        this.dynamicFriction = dynamicFriction;
        this.momentOfInertia = momentOfInertia;
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
}

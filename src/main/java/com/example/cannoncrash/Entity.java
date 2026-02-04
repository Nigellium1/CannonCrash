package com.example.cannoncrash;

public abstract class Entity {
    protected Vector position;
    protected Vector velocity;
    protected double restitution;
    protected double mass;
    protected double reciprocalMass;
    protected double staticFriction;
    protected double dynamicFriction;
    protected double angularVelocity; //Anticlockwise
    protected double momentOfInertia;
    protected double reciprocalMomentOfInertia;
    protected double orientation; //Radians anticlockwise from right horizontal (like maths)

    public Entity(Vector position, double orientation, Material material) {
        this.position = position;
        this.orientation = orientation;
        this.restitution = material.getRestitution();
        this.staticFriction = material.getStaticFriction();
        this.dynamicFriction = material.getDynamicFriction();
        this.mass = material.getMass();
        this.momentOfInertia = material.getMomentOfInertia();
        if (mass == 0) {
            reciprocalMass = 0; //To avoid division by 0, setting reciprocal mass to 0 works in calculations.
        } else {
            reciprocalMass = 1 / mass;
        }
        if (momentOfInertia == 0) {
            reciprocalMomentOfInertia = 0; //Same for moment of inertia (basically "mass" for rotations)
        } else {
            reciprocalMomentOfInertia = 1 / momentOfInertia;
        }
    }
}

package com.example.furiousflingers;

public abstract class Immovable extends Entity {
    public Immovable(Vector position, double orientation, Material material) {
        super(position, orientation, material);
        this.velocity = new Vector(0,0);
        this.angularVelocity = 0;
    }
}

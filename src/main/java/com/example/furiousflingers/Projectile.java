package com.example.furiousflingers;

public class Projectile extends MovableCircle{
    public Projectile(Vector position, Vector velocity, Vector acceleration, double radius, double orientation, double angularVelocity, Material material, String imageName) {
        super(position, velocity, acceleration, radius, orientation, angularVelocity, material, imageName);
    }
}

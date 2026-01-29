package com.example.furiousflingers;

public class Projectile extends MovableCircle{

    private final ProjectileType projectileType;

    public Projectile(Vector position, Vector velocity, Vector acceleration, double radius, double orientation,
                      double angularVelocity, Material material, ProjectileType projectileType) {
        super(position, velocity, acceleration, radius, orientation, angularVelocity, material, projectileType.getName()+"Projectile.png");
        this.projectileType = projectileType;
    }

    public void addGravity () {
        acceleration = new Vector(0, projectileType.getGravity());
    }
}

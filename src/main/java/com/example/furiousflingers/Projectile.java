package com.example.furiousflingers;

public class Projectile extends MovableCircle{

    private final ProjectileType projectileType;
    private boolean hasCollided;
    private boolean hasLaunched;

    public Projectile(Vector position, Vector velocity, Vector acceleration, double radius, double orientation,
                      double angularVelocity, Material material, ProjectileType projectileType) {
        super(position, velocity, acceleration, radius, orientation, angularVelocity, material, projectileType.getName()+"Projectile.png");
        this.projectileType = projectileType;
        this.hasCollided = false;
        this.hasLaunched = false;
    }

    public void addGravity () {
        acceleration = new Vector(0, projectileType.getGravity());
    }

    public boolean isHasCollided() {
        return hasCollided;
    }

    public void setHasCollided() {
        this.hasCollided = true;
    }

    public boolean isHasLaunched () {
        return hasLaunched;
    }

    public void setHasLaunched () {
        this.hasLaunched = true;
    }
}

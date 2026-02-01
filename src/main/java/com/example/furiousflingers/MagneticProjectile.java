package com.example.furiousflingers;

import java.util.ArrayList;

public class MagneticProjectile extends Projectile {
    private final double magneticFieldRadius = 250;

    public MagneticProjectile(Vector position, Vector velocity, Vector acceleration, double radius, double orientation,
                              double angularVelocity, Material material, ProjectileType projectileType) {
        super(position, velocity, acceleration, radius, orientation, angularVelocity, material, projectileType);
    }

    public void attractMovables(ArrayList<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof Movable) {
                if (entity.equals(this)){
                    continue;
                }
                Vector thisToEntityVector = entity.position.subtract(position);
                double thisToEntityMagnitude = thisToEntityVector.magnitude();

                if (thisToEntityMagnitude < magneticFieldRadius) {
                    entity.velocity = entity.velocity.add(thisToEntityVector.unitVector().scalarMultiply(-35));
                }
            }
        }
    }
}

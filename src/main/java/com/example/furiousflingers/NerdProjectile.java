package com.example.furiousflingers;

import java.util.ArrayList;

public class NerdProjectile extends Projectile{
    public NerdProjectile(Vector position, Vector velocity, Vector acceleration, double radius, double orientation,
                          double angularVelocity, Material material, ProjectileType projectileType) {
        super(position, velocity, acceleration, radius, orientation, angularVelocity, material, projectileType);
    }

    @Override
    public ArrayList<CollisionInformation> detectCollisions(ArrayList<Entity> entities) {
        ArrayList<CollisionInformation> collisionInformationList = super.detectCollisions(entities);

        //Make anything that nerd projectile touches have no gravity
        for (CollisionInformation collisionInformation : collisionInformationList){
            //Entity 1 is this projectile so only need to do entity 2 (the entities its touched)
            if (collisionInformation.getEntity2() instanceof Movable){
                ((Movable) collisionInformation.getEntity2()).acceleration = new Vector(0,0);
            }
        }
        return collisionInformationList;
    }
}

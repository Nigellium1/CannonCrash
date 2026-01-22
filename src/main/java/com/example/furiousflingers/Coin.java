package com.example.furiousflingers;

public class Coin extends MovableCircle{
    private boolean removeThisInstance;

    public Coin(Vector position, Vector velocity, Vector acceleration, double radius, double orientation, double angularVelocity,Material material) {
        super(position, velocity, acceleration, radius, orientation, angularVelocity, material, "Coin.png");
        removeThisInstance = false;
    }

    @Override
    public void resolveCollisions(CollisionInformation collisionInformation) {
        super.resolveCollisions(collisionInformation);
        //Coin "collected" if impulse is too large
        if (collisionInformation.getNormalImpulseApplied()>=10){
            removeThisInstance = true;
        }
        //Coin "collected" if direct collision with projectile
        if (collisionInformation.getEntity1() instanceof Projectile || collisionInformation.getEntity2() instanceof Projectile){
            removeThisInstance = true;
        }
    }

    public boolean isRemoveThisInstance() {
        return removeThisInstance;
    }
}

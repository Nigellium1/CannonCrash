package com.example.furiousflingers;

import java.util.ArrayList;

public class BombProjectile extends Projectile {

    private final double explosionRadius = 150;
    private boolean removeThisInstance;

    public BombProjectile(Vector position, Vector velocity, Vector acceleration, double radius,
                          double orientation, double angularVelocity, Material material,ProjectileType projectileType) {
        super(position, velocity, acceleration, radius, orientation, angularVelocity, material,projectileType);
        removeThisInstance = false;
    }

    @Override
    public ArrayList<CollisionInformation> detectCollisions(ArrayList<Entity> entities) {
        ArrayList<CollisionInformation> collisionInformationList = super.detectCollisions(entities);
        //If collided
        if (!collisionInformationList.isEmpty()){
            for (Entity entity:entities){
                if (entity.equals(this)){
                    continue; //Skip itself the bomb
                }
                //Push all movables within radius away
                if (entity instanceof Movable){
                    Vector thisToEntityVector = entity.position.subtract(position);
                    double thisToEntityMagnitude = thisToEntityVector.magnitude();
                    //If within radius

                    if (thisToEntityMagnitude<explosionRadius){
                        entity.velocity = entity.velocity.add(thisToEntityVector.scalarMultiply(
                                5*thisToEntityMagnitude/explosionRadius));
                    }
                }
            }
            removeThisInstance = true;//Tell main loop to remove this
            return new ArrayList<>();
            //Return empty list cus object that was collided with would've been exploded away
        }
        return collisionInformationList;
    }

    @Override
    public void updateAppearance() {
        //Updates the visible position of the node on the screen
        circleView.setX(position.getX()-41); //Cus imageview coords are top left, bomb has little sticky out bit so not radius
        circleView.setY(position.getY()-41);
        circleView.setRotate(-orientation*(180/Math.PI));
    }

    public boolean isRemoveThisInstance() {
        return removeThisInstance;
    }
}

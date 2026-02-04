package com.example.cannoncrash;

public class CollisionInformation {
    private Movable entity1;
    private Entity entity2;
    private Vector collisionNormal; //The unit vector from the movable to the entity it is colliding with
    private double penetrationDepth; //Always positive
    private double normalImpulseApplied;
    private Vector collisionPoint; //Position of collision, used to calculate collision vectors.
    private Vector momentArm1;
    private Vector momentArm2;

    public CollisionInformation() {
    }

    public void setEntity2(Entity entity2) {
        this.entity2 = entity2;
    }

    public Entity getEntity2() {
        return entity2;
    }

    public void setCollisionNormal(Vector collisionNormal) {
        this.collisionNormal = collisionNormal;
    }

    public Vector getCollisionNormal() {
        return collisionNormal;
    }

    public double getPenetrationDepth() {
        return penetrationDepth;
    }

    public void setPenetrationDepth(double penetrationDepth) {
        this.penetrationDepth = penetrationDepth;
    }

    public Movable getEntity1() {
        return entity1;
    }

    public void setEntity1(Movable entity1) {
        this.entity1 = entity1;
    }

    public double getNormalImpulseApplied() {
        return normalImpulseApplied;
    }

    public void setNormalImpulseApplied(double normalImpulseApplied) {
        this.normalImpulseApplied = normalImpulseApplied;
    }

    public Vector getCollisionPoint() {
        return collisionPoint;
    }

    public void setCollisionPoint(Vector collisionPoint) {
        this.collisionPoint = collisionPoint;
    }

    public Vector getMomentArm2() {
        return momentArm2;
    }

    public void setMomentArm2(Vector momentArm2) {
        this.momentArm2 = momentArm2;
    }

    public Vector getMomentArm1() {
        return momentArm1;
    }

    public void setMomentArm1(Vector momentArm1) {
        this.momentArm1 = momentArm1;
    }
}
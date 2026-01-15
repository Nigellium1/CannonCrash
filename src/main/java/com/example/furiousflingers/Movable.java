package com.example.furiousflingers;

import java.util.ArrayList;

public abstract class Movable extends Entity {

    protected Vector acceleration;
    private final double pi = Math.PI;
    protected int numberOfDormantCycles;

    public Movable(Vector position, Vector velocity, Vector acceleration,
                   double orientation, double angularVelocity, Material material) {
        super(position, orientation, material);
        this.velocity = velocity;
        this.angularVelocity = angularVelocity; //in rad/s, usually pretty big because impulse is usually large (lots of pixels), but around 6 rad is a full rotation so large MOI needed to cancel out
        this.acceleration = acceleration;
        numberOfDormantCycles = 0;
    }

    public void standardUpdate(double time) {

//        if (velocity.magnitude()<15&&Math.abs(angularVelocity)<0.2){
//            numberOfDormantCycles++;
//        } else {
//            numberOfDormantCycles = 0;
//            acceleration = new Vector(0,1000);
//        }
//
//        if (numberOfDormantCycles>9){
//            velocity = new Vector(0,0);
//            angularVelocity = 0;
//            acceleration = new Vector(0,0);
//        }
        //NEED EXTRA CHECK, DURING COLLISION DETECTION, STORE "SUPPORT AREA", THEN HERE CHECK IF COM PROJECTED ONTO THAT LINE IS INSIDE, IF INSIDE THEN STABLE OTHERWISE ITS A DIAMOND PIVOT

        //Updates velocity and position by multiplying their rate of change by the time passed
        //MAGIC GUY ON YT TOLD ME TO ADD HALF ON EITHER SIDE https://www.youtube.com/watch?v=ZOfMA8h4qO8 AT 2:00
        //nvm i get it now, it interpolates the average velocity by just taking the halfway mark and updating the position using that
        velocity = velocity.add(acceleration.scalarMultiply(time * 0.5));
        position = position.add(velocity.scalarMultiply(time));
        velocity = velocity.add(acceleration.scalarMultiply(time * 0.5));

        //Regenerate vertices in reference to the COM
        if (this instanceof Rectangle){
            Vector[] newVertices = new Vector[4];
            newVertices[0] = position.add(new Vector(-((Rectangle) this).getWidth()/2,-((Rectangle) this).getHeight()/2));
            newVertices[1] = position.add(new Vector(((Rectangle) this).getWidth()/2,-((Rectangle) this).getHeight()/2));
            newVertices[2] = position.add(new Vector(((Rectangle) this).getWidth()/2,((Rectangle) this).getHeight()/2));
            newVertices[3] = position.add(new Vector(-((Rectangle) this).getWidth()/2,((Rectangle) this).getHeight()/2));
            ((Rectangle) this).setVertices(newVertices);
        }


        //Rotation stuff
        orientation = ((orientation+angularVelocity*time)%(2*pi));

        if (this instanceof Polygon){
            ((Polygon) this).updateVerticesOrientations(orientation);
        }
    }

    public abstract void updateAppearance();

    public abstract ArrayList detectCollisions(ArrayList<Entity> entities);

    public void resolveCollisions(CollisionInformation collisionInformation) {
        Entity entity1 = collisionInformation.getEntity1();
        Entity entity2 = collisionInformation.getEntity2();
        Vector momentArm1 = collisionInformation.getMomentArm1();
        Vector momentArm2 = collisionInformation.getMomentArm2();
        Vector collisionNormal = collisionInformation.getCollisionNormal();

        //Calculate relative velocity between entities
        Vector relativeVelocity = findRelativeVelocity(collisionInformation);
        //Calculate component of relative velocity in normal direction
        double velocityInNormalDirection = relativeVelocity.dotProduct(collisionNormal);
        //If velocities are separating do nothing
        if (velocityInNormalDirection < 0) {
            return;
        }
        //Use lowest restitution value in a collision (toast with jam on it still splats on bounciest ball in the world)
        double restitution = Math.min(entity1.restitution, entity2.restitution);
        double rotationalTerms = calculateRotationalTerms(collisionInformation, collisionNormal);
        //Formula for impulseScalar: \[ (V^B - V^A) \cdot n + j * (\frac{j * n}{mass^A} + \frac{j * n}{mass^B}) * n + e * (V^B - V^A) \cdot n = 0 \\ \
        // therefore \\ (1 + e)((V^B - V^A) \cdot n) + j * (\frac{j * n}{mass^A} + \frac{j * n}{mass^B}) * n = 0 \\ \
        // therefore \\ j = \frac{-(1 + e)((V^B - V^A) \cdot n)}{\frac{1}{mass^A} + \frac{1}{mass^B}} \]
        double impulseScalar = ((1 + restitution) * velocityInNormalDirection) / (entity1.reciprocalMass + entity2.reciprocalMass+rotationalTerms);
        //Convert impulseScalar and collisionNormal (unit vector in impulse direction) to an impulse vector
        Vector impulseVector = collisionNormal.scalarMultiply(impulseScalar);
        //Formula for new velocity: \[ Impulse = mass * Velocity \\ Velocity = \frac{Impulse}{mass} \therefore V' = V + \frac{j * n}{mass}\]
//        addFriction();
        entity1.velocity = entity1.velocity.subtract(impulseVector.scalarMultiply(entity1.reciprocalMass));
        //Same for entity2 but subtract velocity so opposite direction
        entity2.velocity = entity2.velocity.add(impulseVector.scalarMultiply(entity2.reciprocalMass));
        collisionInformation.setNormalImpulseApplied(impulseScalar);

        entity1.angularVelocity = entity1.angularVelocity + (momentArm1.vectorVectorCrossProduct(impulseVector)*entity1.reciprocalMomentOfInertia);
        entity2.angularVelocity = entity2.angularVelocity - (momentArm2.vectorVectorCrossProduct(impulseVector)*entity2.reciprocalMomentOfInertia);
    }

    //stops stuff sinking into the floor
    public void positionCorrection (Entity entity1,Entity entity2, double penetrationDepth, Vector collisionNormal){
        final double correctionRatio = 0.2;
        double correctionScalar = penetrationDepth*correctionRatio/
                (entity1.reciprocalMass+entity2.reciprocalMass);
        Vector correctionVector = collisionNormal.scalarMultiply (correctionScalar);
        if (entity1 instanceof Movable) {
            entity1.position =  entity1.position.subtract(correctionVector.scalarMultiply(entity1.reciprocalMass));
        }
        if (entity2 instanceof Movable) {
            entity2.position = entity2.position.add(correctionVector.scalarMultiply(entity2.reciprocalMass));
        }
    }

    public void resolveFriction (CollisionInformation collisionInformation){
        Entity entity1 = collisionInformation.getEntity1();
        Entity entity2 = collisionInformation.getEntity2();
        Vector collisionNormal = collisionInformation.getCollisionNormal();
        double impulseScalar = Math.abs(collisionInformation.getNormalImpulseApplied());
        Vector momentArm1 = collisionInformation.getMomentArm1();
        Vector momentArm2 = collisionInformation.getMomentArm2();

        //Get tangent (perpendicular to normal)
        Vector tangent = collisionNormal.perpendicularise();
        //Calculate average friction
        double staticFriction = (entity1.staticFriction+entity2.staticFriction)/2;
        double dynamicFriction = (entity1.dynamicFriction+entity2.dynamicFriction)/2;
        //Relative velocity between entities (after normal impulse applied)
        Vector relativeVelocity = findRelativeVelocity(collisionInformation);
        double velocityInTangentDirection = relativeVelocity.dotProduct(tangent);
        double rotationalTerms = calculateRotationalTerms(collisionInformation,tangent);
        double frictionImpulseScalar = velocityInTangentDirection/(entity1.reciprocalMass + entity2.reciprocalMass+rotationalTerms);
        //Calculate maximum static friction (dependant on normal impulse)
        double maxStaticFriction = staticFriction*impulseScalar;
        double maxDynamicFriction = dynamicFriction*impulseScalar;
        //If impulse is less than the maximum static, the objects are not sliding and we use the higher friction value
        if (!(Math.abs(frictionImpulseScalar) <= maxStaticFriction)){
            //If not static, use either the frictionImpulseScalar or maxDynamicFriction (whichever's smaller)
            frictionImpulseScalar = Math.min(frictionImpulseScalar, maxDynamicFriction);
        }

        Vector frictionImpulseVector =  tangent.scalarMultiply(frictionImpulseScalar);
        entity1.velocity = entity1.velocity.subtract(frictionImpulseVector.scalarMultiply(entity1.reciprocalMass));
        entity2.velocity = entity2.velocity.add(frictionImpulseVector.scalarMultiply(entity2.reciprocalMass));
        entity1.angularVelocity = entity1.angularVelocity + (momentArm1.vectorVectorCrossProduct(frictionImpulseVector)*entity1.reciprocalMomentOfInertia);
        entity2.angularVelocity = entity2.angularVelocity - (momentArm2.vectorVectorCrossProduct(frictionImpulseVector)*entity2.reciprocalMomentOfInertia);
    }

    //Calculates relative velocities at point of contact (spinny thing is faster than non spinny thing even if the whole object is at same speed)
    public Vector findRelativeVelocity (CollisionInformation collisionInformation){
        Entity entity1 = collisionInformation.getEntity1();
        Entity entity2 = collisionInformation.getEntity2();
        Vector momentArm1 = collisionInformation.getMomentArm1();
        Vector momentArm2 = collisionInformation.getMomentArm2();
        Vector entity1PointVelocity = entity1.velocity.add(momentArm1.vectorScalarCrossProduct(entity1.angularVelocity));
        Vector entity2PointVelocity = entity2.velocity.add(momentArm2.vectorScalarCrossProduct(entity2.angularVelocity));
        Vector relativeVelocity = entity1PointVelocity.subtract(entity2PointVelocity);
//        System.out.println("entity 1:"+ entity1.velocity.getX()+"other thing:"+momentArm1.vectorScalarcrossProduct(entity1.angularVelocity).getX());
        return relativeVelocity;
    }

    public double calculateRotationalTerms(CollisionInformation collisionInformation, Vector vectorToCalculateWith){
        double scalar1 = collisionInformation.getMomentArm1().vectorVectorCrossProduct(vectorToCalculateWith);
        double scalar2 = collisionInformation.getMomentArm2().vectorVectorCrossProduct(vectorToCalculateWith);
        double rotationalTerm1 = scalar1*scalar1*collisionInformation.getEntity1().reciprocalMomentOfInertia;
        double rotationalTerm2 = scalar2*scalar2*collisionInformation.getEntity2().reciprocalMomentOfInertia;
        return rotationalTerm1 + rotationalTerm2;
    }
}

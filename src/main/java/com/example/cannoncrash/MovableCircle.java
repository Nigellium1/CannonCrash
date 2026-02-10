package com.example.cannoncrash;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.ArrayList;

public class MovableCircle extends Movable {

    protected final ImageView circleView; //Visible node
    protected final double radius;

    public MovableCircle(Vector position, Vector velocity, Vector acceleration, double radius,
                         double orientation, double angularVelocity, Material material, String imageName) {
        super(position,velocity,acceleration,orientation,angularVelocity, material);
        this.radius = radius;
        this.circleView = importImage(imageName);
    }

    public ImageView getView() {
        return circleView;
    }

    public void updateAppearance() {
        //Updates the visible position of the node on the screen
        circleView.setX(position.getX()-radius); //Cus imageview coords are top left
        circleView.setY(position.getY()-radius);
        circleView.setRotate(-orientation*(180/Math.PI));
    }

    public ArrayList<CollisionInformation> detectCollisions(ArrayList<Entity> entities) {
        ArrayList<CollisionInformation> collisionInformationList = new ArrayList<CollisionInformation>();
        //Body to body checks
        for (Entity entity : entities){
            //Don't check with itself
            if (entities.indexOf(entity)==entities.indexOf(this)){
                continue;
            }
            //Circle checks
            if (entity instanceof MovableCircle){
                //Pythagoras check for distance (keep squares because sqrt is bad for performance)
                    if ((radius+((MovableCircle) entity).radius)*(radius+((MovableCircle) entity).radius)>=
                            (position.getX()-entity.position.getX())*(position.getX()-entity.position.getX())
                        +(position.getY()-entity.position.getY())*(position.getY()-entity.position.getY())){
                    CollisionInformation collisionInformation = new CollisionInformation();
                    collisionInformation.setEntity2(entity);
                    collisionInformation.setCollisionNormal(entity.position.subtract(position).unitVector());
                    collisionInformation.setPenetrationDepth(((MovableCircle) entity).radius+radius-
                            entity.position.subtract(position).magnitude());
                    collisionInformation.setCollisionPoint(entity.position.add(
                            collisionInformation.getCollisionNormal().scalarMultiply(radius)));
                    //Position + radius*collision normal = collision point
                    collisionInformationList.add(collisionInformation);
                }
            }

            if (entity instanceof Rectangle) {
                //Project circle centre onto each axis of rectangle, choose closest,
                // if none then check all vertices for closest
                double closestDistance = Double.MAX_VALUE;
                Vector closestPoint = new Vector(0,0); //GETS OVERRIDDEN
                for (int i = 0; i < 4; i++) {
                    Vector facePoint1 = ((Rectangle)entity).getVertices()[i]; //One end of line
                    Vector facePoint2 = ((Rectangle)entity).getVertices()[(i+1)%4]; //Other end of line
                    Vector faceVector = facePoint2.subtract(facePoint1);
                    Vector point1ToCircleCentre = position.subtract(facePoint1);

                    //Vector projection formula
                    double scalarCoefficient = faceVector.dotProduct(point1ToCircleCentre)/
                            (faceVector.magnitude()*faceVector.magnitude());
                    if (scalarCoefficient>=0&&scalarCoefficient<=1){
                        Vector point1ToProjection = faceVector.scalarMultiply(scalarCoefficient);
                        double distance = point1ToProjection.subtract(point1ToCircleCentre).magnitude();
                        if (distance<closestDistance){
                           closestDistance = distance;
                           closestPoint = facePoint1.add(point1ToProjection);
                       }
                    }
                }
                //If no valid projection found (must be a corner),
                // face to circle will always be closer if there is a valid projection
                if (closestDistance == Double.MAX_VALUE){
                    for (int i = 0; i < 4; i++) {
                        Vector vertex = ((Rectangle)entity).getVertices()[i];
                        double distance = vertex.subtract(position).magnitude();
                        if (distance<closestDistance){
                            closestDistance = distance;
                            closestPoint = vertex;
                        }
                    }
                }

                if (closestDistance<=radius){
                    CollisionInformation collisionInformation = new CollisionInformation();
                    collisionInformation.setEntity2(entity);
                    collisionInformation.setCollisionNormal(closestPoint.subtract(position).unitVector());
                    collisionInformation.setPenetrationDepth(radius-closestDistance);
                    collisionInformation.setCollisionPoint(closestPoint);
                    collisionInformationList.add(collisionInformation);
                }
            }
        }
        //ADD MORE CHECKS LATER

        //Things for every collision
        for (CollisionInformation collisionInformation : collisionInformationList){
            collisionInformation.setEntity1(this);
            collisionInformation.setMomentArm1(collisionInformation.getCollisionPoint()
                    .subtract(collisionInformation.getEntity1().position));
            collisionInformation.setMomentArm2(collisionInformation.getCollisionPoint()
                    .subtract(collisionInformation.getEntity2().position));
        }
        return collisionInformationList;
    }

    public ImageView importImage(String imageName) {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(imageName);
            Image image = new Image(inputStream);
            return new ImageView(image);
        } catch (Exception importingCircleImage) {
            System.out.println("Error importing circle image");
            importingCircleImage.printStackTrace();
            return null;
        }
    }
}
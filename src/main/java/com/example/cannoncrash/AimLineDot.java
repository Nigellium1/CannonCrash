package com.example.cannoncrash;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.ArrayList;

public class AimLineDot {
    private final double  radius = 5.5;
    private final ImageView dotView = importImage("aimLineDot.png");
    private final Vector position;

    public AimLineDot(Vector position) {
        this.position = position;
        dotView.setX(position.getX()-radius); //Cus imageview coords are top left
        dotView.setY(position.getY()-radius);
    }

    //Same as MovableCircle but removed collisionInformation (only yes or no for colliding needed)
    public boolean isColliding (ArrayList<Entity> entities) {
        for (Entity entity : entities){
            //Circle checks
            if (entity instanceof MovableCircle){
                //Pythagoras check for distance (keep squares because sqrt is bad for performance)
                if ((radius+((MovableCircle) entity).radius)*(radius+((MovableCircle) entity).radius)>=
                        (position.getX()-entity.position.getX())*(position.getX()-entity.position.getX())
                                +(position.getY()-entity.position.getY())*(position.getY()-entity.position.getY())){
                    return true;
                }
            }

            if (entity instanceof Rectangle) {
                //Project circle centre onto each axis of rectangle, choose closest,
                // if none then check all vertices for closest
                double closestDistance = Double.MAX_VALUE;
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
                        }
                    }
                }

                if (closestDistance<=radius){
                    return true;
                }
            }
        }
        return false;
    }

    public ImageView importImage(String imageName) {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(imageName);
            Image image = new Image(inputStream);
            return new ImageView(image);
        } catch (Exception importingCircleImage) {
            System.out.println("Error importing dot image");
            importingCircleImage.printStackTrace();
            return null;
        }
    }

    public ImageView getDotView() {
        return dotView;
    }
}

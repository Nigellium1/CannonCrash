package com.example.furiousflingers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.ArrayList;

public class MovableRectangle extends Movable implements Rectangle {

    private final ImageView rectangleView;
    private final double width;
    private final double height;
    private Vector[] vertices = new Vector[4]; //0 - top left, 1 top right,
    // 2 bottom right, 3 bottom left

    public MovableRectangle(Vector position, Vector velocity, Vector acceleration,
                            double orientation, double angularVelocity, double width, double height,
                            Material material, String imageName) {
        super(position,velocity,acceleration, orientation, angularVelocity, material);
        this.width = width;
        this.height = height;
        rectangleView = importImage(imageName);
        vertices[0] = position.add(new Vector(-width/2,-height/2));
        vertices[1] = position.add(new Vector(width/2,-height/2));
        vertices[2] = position.add(new Vector(width/2,height/2));
        vertices[3] = position.add(new Vector(-width/2,height/2));
    }

    public ImageView getRectangle() {
        return rectangleView;
    }

    public void updateAppearance (){
        rectangleView.setX(position.getX()-width/2); //Rectangle shape position is top left corner
        // so I take my center of mass and subtract half width and height to get top left corner
        rectangleView.setY(position.getY()-height/2);
        rectangleView.setRotate(-orientation*(180/Math.PI));
    }

    public ArrayList detectCollisions(ArrayList<Entity> entities){
        ArrayList<CollisionInformation> collisionInformationList = new ArrayList<CollisionInformation>();

        for (Entity entity : entities){
            if (entities.indexOf(entity)==entities.indexOf(this)){
                continue;
            }
            ArrayList<Vector> axes = new ArrayList<Vector>(); //Axes to be used for SAT
            //Add axes of shape 1, (kinda cheated cus ur supposed to use the normals but since its a rectangle, the normals are just the perpendicular sides)
            axes = addAxis(vertices,axes);

            //Rectangle checks (MIGHT EXTEND THIS TO POLYGON CHECKS)
            if (entity instanceof Rectangle){
                boolean isColliding = true;
                double smallestOverlap = Double.MAX_VALUE; //This might be bad but declare as largest possible double value so first overlap overwrites it
                Vector smallestOverlapAxis = new Vector(0,0); //This will get overridden the values mean nothing
                axes = addAxis(((Polygon) entity).getVertices(),axes);
                boolean thisIsReferenceEntity = true;
                for (int i = 0; i < axes.size(); i++) {
                    //Dot product to find how far along the point is when projected onto the vector
                    Vector entity1MaxVertex = findFurthestVertexAlongAxis(vertices,axes.get(i)).getVertex();
                    Vector entity2MaxVertex = findFurthestVertexAlongAxis(((Polygon)entity).getVertices(),axes.get(i)).getVertex();
                    Vector entity1MinVertex = findFurthestVertexAlongAxis(vertices,axes.get(i).scalarMultiply(-1)).getVertex();
                    Vector entity2MinVertex = findFurthestVertexAlongAxis(((Polygon)entity).getVertices(),axes.get(i).scalarMultiply(-1)).getVertex();

                    double entity1Max = entity1MaxVertex.dotProduct(axes.get(i));
                    double entity1Min = entity1MinVertex.dotProduct(axes.get(i));
                    double entity2Max = entity2MaxVertex.dotProduct(axes.get(i));
                    double entity2Min = entity2MinVertex.dotProduct(axes.get(i));

                    //Check max and mins for overlap, if entity1 is completely left of entity2, or entity1 is completley right of entity2 - no collision
                    if (entity1Max < entity2Min || entity1Min > entity2Max){
                        isColliding = false;
                        break;
                    }

                    //There are two possible overlaps so compute both then choose the smallest
                    double overlap = Math.min(entity1Max-entity2Min,entity2Max-entity1Min);
                    if (overlap < smallestOverlap){
                        smallestOverlap = overlap;
                        smallestOverlapAxis = axes.get(i);
                        //Since this is a rectangle, if the axis was one of the first 4, then the reference entity will be this, otherwise it is entity
                        if (i<=3){
                            thisIsReferenceEntity = true;
                        } else {
                            thisIsReferenceEntity = false;
                        }
                    }
                }
                //If not colliding, continue and check against next entity
                if (!isColliding){
                    continue;
                }

                //If thisIsReferenceEntity is false, then we need to reverse the direction of collisionNormal
                // when checking furthest along normal
                int collisionNormalDirectionMultiplier = 1;
                if (!thisIsReferenceEntity){
                    collisionNormalDirectionMultiplier = -1;
                }

                //Because rectangles have 2 identical sides and normals (just signs inverted), to make sure normal points in correct direction (entity1 to entity2)
                //Dot product the current "normal" with vector from centre1 to centre2, if negative then flip the sign of normal
                Vector vectorFromCentre1ToCentre2 = entity.position.subtract(this.position);
                if (vectorFromCentre1ToCentre2.dotProduct(smallestOverlapAxis)<0){
                    smallestOverlapAxis = smallestOverlapAxis.scalarMultiply(-1);
                }

                //Add information if collision is detected
                CollisionInformation collisionInformation = new CollisionInformation();
                collisionInformation.setEntity2(entity);
                collisionInformation.setPenetrationDepth(smallestOverlap);
                collisionInformation.setCollisionNormal(smallestOverlapAxis);

                //add wacky collision point stuff
                //Allocate reference and incident vertices
                Vector[] referenceVertices;
                Vector[] incidentVertices;
                if (thisIsReferenceEntity){
                    referenceVertices = vertices;
                    incidentVertices = ((Polygon) entity).getVertices();
                } else {
                    referenceVertices = ((Polygon) entity).getVertices();
                    incidentVertices = vertices;
                }
                //Find furthest vertex of entity1 in direction of collisionNormal
                SignificantFaceInformation significantFaceInformation1 =
                        findFurthestVertexAlongAxis(referenceVertices,
                                smallestOverlapAxis.scalarMultiply(collisionNormalDirectionMultiplier));
                SignificantFaceInformation significantFaceInformation2 =
                        findFurthestVertexAlongAxis(incidentVertices,
                                smallestOverlapAxis.scalarMultiply(-1*collisionNormalDirectionMultiplier));
                //Reverse direction of normal to find furthest but in opposite direction

                //Find correct incident and reference faces by comparing the faces of adjacent points with collision normal
                Vector [] referenceFace = new Vector[2]; //Two points to hold start and end of line
                referenceFace[0] = significantFaceInformation1.getVertex();
                //Original furthest vertex must be on line
                referenceFace[1] = findOtherVertexOfMostPerpendicularFace(significantFaceInformation1.getVertex(),
                        referenceVertices[Math.floorMod(significantFaceInformation1.getIndex()+1,4)],
                        referenceVertices[Math.floorMod(significantFaceInformation1.getIndex()-1,4)],
                        collisionInformation.getCollisionNormal()); //MOD STUFF SO IF vertices[0-1] doesnt blow up

                //Compare with the two adjacent points to decide which should create the face
                Vector [] incidentFace = new Vector[2];
                incidentFace[0] = significantFaceInformation2.getVertex();
                incidentFace[1] = findOtherVertexOfMostPerpendicularFace(significantFaceInformation2.getVertex(),
                        incidentVertices[Math.floorMod(significantFaceInformation2.getIndex() + 1, 4)],
                        incidentVertices[Math.floorMod(significantFaceInformation2.getIndex() - 1, 4)],
                        collisionInformation.getCollisionNormal()); //THIS IS BAD PROBS BUT IVE JUST ADDED 4 TO MAKE IT THE SECOND SHAPE IN VERTICES

                //Adjacent face clipping
                incidentFace = clipFace(referenceFace[1].subtract(referenceFace[0]),referenceFace[0],incidentFace); //"Left" side clipping
                incidentFace = clipFace(referenceFace[0].subtract(referenceFace[1]),referenceFace[1],incidentFace); //Other adjacent side clipping

                //Final face clipping
                //NOT FOLLOWING THE TUTORIAL HERE, IM SCARED I COULD BE LE  FT WITH TWO POINTS IF THE WHOLE FACE IS INSIDE SO INSTEAD IM JUST CHOOSING THE FURTHEST VERTEX ALONG THE NORMAL
                //Since collision normal points from this to entity, if this is the reference, then I want the point that is furthest towards the reference so -1*collisionNormalDirectionMultiplier (which is like thisIsReference but 0 or 1)
                SignificantFaceInformation collisionPointInformation = findFurthestVertexAlongAxis(incidentFace,collisionInformation.getCollisionNormal().scalarMultiply(-1*collisionNormalDirectionMultiplier));
                Vector collisionPoint = collisionPointInformation.getVertex();
                if (collisionPointInformation.isEquidistant()){
                    collisionPoint = incidentFace[0].add((incidentFace[1].subtract(incidentFace[0]).scalarMultiply(0.5))); //Halfway point between both points is collision point if two parallel faces collide
                }
                collisionInformation.setCollisionPoint(collisionPoint);

                collisionInformationList.add(collisionInformation);
            }
        }

        for (CollisionInformation collisionInformation : collisionInformationList){
            collisionInformation.setEntity1(this);
            collisionInformation.setMomentArm1(collisionInformation.getCollisionPoint().subtract(collisionInformation.getEntity1().position)); //THIS IS WRONG, POSITION ISN'T CENTER OF MASS, YES IT IS NOW
            collisionInformation.setMomentArm2(collisionInformation.getCollisionPoint().subtract(collisionInformation.getEntity2().position));
        }
        return collisionInformationList;
    };

    public Vector[] getVertices() {
        return vertices;
    }

    public void setVertices(Vector[] vertices) {
        this.vertices = vertices;
    }

    public void updateVerticesOrientations(double changeInAngle){
        for (int i = 0; i < vertices.length; i++) {
            Vector vectorFromCOMToVertex = vertices[i].subtract(position);
            vectorFromCOMToVertex = vectorFromCOMToVertex.rotate(changeInAngle);
            vertices[i] = position.add(vectorFromCOMToVertex);
        }
         //THIS NEEDS TO BE DEBUGGED BUT IMA JUST HOPE AND PRAY
    }

    public SignificantFaceInformation findFurthestVertexAlongAxis (Vector[]vertices, Vector axis){
        double furthestDistanceAlongNormal = vertices[0].dotProduct(axis);
        SignificantFaceInformation significantFaceInformation = new SignificantFaceInformation(vertices[0],0);
        for (int i = 1; i < vertices.length; i++) {
            if (vertices[i].dotProduct(axis)>furthestDistanceAlongNormal){
                furthestDistanceAlongNormal = vertices[i].dotProduct(axis);
                significantFaceInformation.setIndex(i);
                significantFaceInformation.setVertex(vertices[i]);
                continue;
            }
            //If we ever have another vertex that is also the furthest distance along normal, then there are multiple points furthest points, so set equidistant = true (used for setting collision point to be halfway between the two)
            if (vertices[i].dotProduct(axis)==furthestDistanceAlongNormal){
                significantFaceInformation.setEquidistant(true);
            }
        }
        return significantFaceInformation;
    }

    public Vector findOtherVertexOfMostPerpendicularFace (Vector fixedPoint, Vector pointToTest1,
                                                          Vector pointToTest2, Vector collisionNormal){
        Vector faceAxis1 = fixedPoint.subtract(pointToTest1);
        double perpendicularness1 = faceAxis1.dotProduct(collisionNormal);
        Vector faceAxis2 = fixedPoint.subtract(pointToTest2);
        double perpendicularness2 = faceAxis2.dotProduct(collisionNormal);
        //Find which one has closest dot product to 0 (most perpendicular)
        if (Math.abs(perpendicularness1)<Math.abs(perpendicularness2)){
            return pointToTest1;
        }
        else {
            return pointToTest2;
        }
    }

    public ArrayList<Vector> addAxis (Vector[] vertices, ArrayList<Vector> axes){
        for (int i = 0; i < vertices.length; i++) {
            axes.add((vertices[(i+1)%vertices.length].subtract(vertices[i%vertices.length])).unitVector());
        }
        return axes;
    }

    public Vector findIntersection (Vector point1, Vector directionVector1,Vector point2,
                                    Vector directionVector2){
        double scalarMultiplier = (point2.subtract(point1).
                vectorVectorCrossProduct(directionVector2))/
                directionVector1.vectorVectorCrossProduct(directionVector2);
        //Like lambda in maths for vectors
        return point1.add(directionVector1.scalarMultiply(scalarMultiplier));
    }

    public Vector[] clipFace (Vector clippingNormal, Vector referenceClippingPoint, Vector[] incidentFace){
        Double referenceProjection = referenceClippingPoint.dotProduct(clippingNormal);
        for (int i = 0; i < 2; i++) {
            Double incidentProjection = incidentFace[i].dotProduct(clippingNormal);
            if (incidentProjection<referenceProjection){
                Vector referenceDirectionVector = clippingNormal.perpendicularise();
                Vector incidentDirectionVector = incidentFace[1].subtract(incidentFace[0]);
                incidentFace[i] = findIntersection(referenceClippingPoint,referenceDirectionVector,
                        incidentFace[i], incidentDirectionVector);
            }
        }
        return incidentFace;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    public ImageView importImage(String imageName) {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(imageName);
            Image image = new Image(inputStream);
            return new ImageView(image);
        } catch (Exception importingCircleImage) {
            System.out.println("Error importing rectangle image");
            importingCircleImage.printStackTrace();
            return null;
        }
    }
}

//TO DO LIT: ADD COLLISION POINT TO COLLISION INFORMATION THEN, STONKS?? YOU ALSO GOTTA UPDATE THE SHAPE VERTICES YOU BUFOON, YOU DIDNT UPDATE THE VERTICES, NOW LOOK WHAT YOUVE DONE IT DOESNT WORK CUS IT THINKS THE RECTANGLES HAVEN'T MOVEDS
//WHEN YOU ROTATE RECTANGLE, IT ROTATES ABOUT ITS CENTRE BUT POSITION STAYS THE TOP LEFT CORNER
//REMEMBER WHEN I GENERATE RECTANGLES, IT ASSUMES THE RECTANGLE IS NOT ORIENTATED, IF I WANT TO SPAWN IT ORIENTATED THEN VERTICES[] WILL BREAK

//SQUARES CAN STILL PENETRATE INTO FLOOR, ONLY RESOLVES WHEN IT ROTATES ONTO TIP LIKE DIAMOND???
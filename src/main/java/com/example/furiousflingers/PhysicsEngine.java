//STUFF TO DO: FRICTION, LAUNCHER, NOT BALLS, ROTATIONS

package com.example.furiousflingers;

import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class PhysicsEngine implements ProjectileSpawner{
    private final double sceneWidth;
    private final double sceneHeight;
    private Pane physicsEnginePane;
    private ArrayList<Entity> entities;
    private LauncherType launcher;
    private ProjectileType projectile;
    private int coinsRemaining;

    public PhysicsEngine (double sceneWidth,double sceneHeight, LoadoutInformation loadoutInformation){
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        entities = new ArrayList<>();
        launcher = loadoutInformation.getLauncher();
        projectile = loadoutInformation.getProjectile();
        physicsEnginePane = new Pane();
        setupPane();
    }

    public Pane getPhysicsEnginePane() {
        return physicsEnginePane;
    }

    public void start(int levelNumber) throws Exception {
        addGround();
        addLevel(levelNumber);

        AnimationTimer animationTimer = new AnimationTimer() {
            //Placeholder till start time is set
            double startTime = -1;
            //Variable for time of last cycle
            double lastTime = 0;

            @Override
            public void handle(long now) {
                //Set start time in seconds (from nanoseconds) on first cycle
                if (startTime == -1) {
                    startTime = now / 1000000000.0;
                }
                //Current time in relation to when first cycle started
                double timeInSecs = now / 1000000000.0 - startTime;

                //Standard update all entities
                for (Entity entity : entities) {
                    //Updates velocity and displacement using time passed
                    if (entity instanceof Movable) {
                        ((Movable) entity).standardUpdate(timeInSecs - lastTime);
                    }
                }

                //Despawn entities outside of window (except above cus they fall) with 100 margin
                for (int i = 0; i < entities.size(); i++) {
                    if (entities.get(i).position.getX() - 100 > sceneWidth ||
                            entities.get(i).position.getX() + 100 < 0 ||
                            entities.get(i).position.getY() - 100 > sceneHeight ||
                            entities.get(i).position.getY()<-500) { //Add roof but really high up only for railgun launcher)
                        removeEntity(i);
                        i--; //Removed an entity so keep on same i by decrementing
                    }
                }

                //Get collision information
                ArrayList<CollisionInformation> collisionInformationList = new ArrayList<>();
                for (Entity entity : entities) {
                    //CollisionInformations contains boolean isColliding, Entity collidingEntity, Vector collisionNormal, and double penetrationDepth
                    if (entity instanceof Movable) {
                        collisionInformationList.addAll(((Movable) entity).detectCollisions(entities));
                    }
                }

                //Check for entities (projectiles) that need removing
                for (int i = 0; i < entities.size(); i++) {
                    //Remove exploded bombs
                    if (entities.get(i) instanceof BombProjectile) {
                        if (((BombProjectile) entities.get(i)).isRemoveThisInstance()) {
                            removeEntity(i);
                            i--;
                        }
                    }
                    //Remove collected coins
                    if (entities.get(i) instanceof Coin){
                        if (((Coin)entities.get(i)).isRemoveThisInstance()){
                            removeEntity(i);
                            i--;
                            coinsRemaining--;
                            if (coinsRemaining==0){
                                gameEnd();
                            }
                        }
                    }
                }

                //Resolve collisions
                for (int i = 0; i < 10; i++) {
                    for (CollisionInformation collisionInformation : collisionInformationList) {
                        collisionInformation.getEntity1().resolveCollisions(collisionInformation);
                    }
                }

                //Friction
                for (CollisionInformation collisionInformation : collisionInformationList){
                    collisionInformation.getEntity1().resolveFriction(collisionInformation);
                }

                //Position correction
                for (CollisionInformation collisionInformation : collisionInformationList){
                    collisionInformation.getEntity1().positionCorrection(collisionInformation.getEntity1(),collisionInformation.getEntity2()
                            , collisionInformation.getPenetrationDepth(),collisionInformation.getCollisionNormal());
                }

                //Update appearance of entities using their position vector
                for (Entity entity : entities){
                    if (entity instanceof Movable) {
                        ((Movable) entity).updateAppearance();
                    }
                }

                //Set last time for the next cycle
                lastTime = timeInSecs;
            }
        };
        animationTimer.start();
    }

    public void setupPane() {
        Launcher launcher = addLauncher();
        addClickFunctionality(launcher);
    }

//    public void addMovableCircle(Vector position, Vector velocity, Vector acceleration,
//                                 double orientation, double angularVelocity, Material material) {
//        MovableCircle circle = new MovableCircle(position,velocity,acceleration,
//                 32,orientation,angularVelocity,material);
//        physicsEnginePane.getChildren().add(circle.getCircleView());
//        entities.add(circle);
//    }
    //FOR LEFT CLICK SPAWNING CIRLCES TESTING

    //So launcher can have the circle it creates
    public void addMovableCircle (MovableCircle circle){
        physicsEnginePane.getChildren().add(circle.getCircleView());
        entities.add(circle);
    }

    public void addClickFunctionality (Launcher launcher){
        physicsEnginePane.setOnMousePressed(event -> {
            launcher.handleMousePress(event,projectile);
//            if (event.isPrimaryButtonDown()){
//                addMovableRectangle(new Vector(event.getX(),event.getY()),new Vector(0,0),new Vector(0,1000),
//                        120,60,0,Material.STONE);
//            }
            if (event.isSecondaryButtonDown()){
                addMovableRectangle(new Vector(event.getX(),event.getY()),new Vector(0,0),new Vector(0,1000),
                        120,60,0,Material.WOOD);
            }
        });

        physicsEnginePane.setOnMouseReleased(event -> {
            launcher.handleMouseRelease(event);
        });

        physicsEnginePane.setOnMouseDragged(event -> {
            launcher.handleMouseDragged(event);
        });
    }

    public void addMovableRectangle(Vector position, Vector velocity, Vector acceleration,
                                    double width, double height, double orientation, Material material) {
        MovableRectangle rectangle = new MovableRectangle(position,velocity,acceleration,orientation,0,width,height,material);
        physicsEnginePane.getChildren().add(rectangle.getRectangle());
        entities.add(rectangle);
    }

    public void addImmovableRectangle(ArrayList<Entity> entities, Vector position, double width, double height, Material material, String imagePath) {
        ImmovableRectangle rectangle = new ImmovableRectangle(position,0,width,height,material,imagePath);
        ImageView rectangleView = rectangle.getRectangleView();
        rectangleView.setY(440); //Shift downwards
        physicsEnginePane.getChildren().add(rectangleView);
        entities.add(rectangle);
    }

    public void addGround (){
        addImmovableRectangle(entities,new Vector(sceneWidth/2,sceneHeight-30),sceneWidth,60,Material.IMMOVABLE, "floor.png");
    }

    public Launcher addLauncher (){
        ProjectileSpawner projectileSpawner = this;
        return new Launcher(new Vector(150,300), physicsEnginePane, projectileSpawner,launcher);
    }

    public void removeEntity (int index) {
        int differenceInListSizes = Math.abs(physicsEnginePane.getChildren().size()-entities.size());
        physicsEnginePane.getChildren().remove(index+differenceInListSizes); //Pane includes launcher which isn't part of entities so lists are out of sync by 1
        entities.remove(index);
    }

    public void addCoin (Vector position, Vector velocity, Vector acceleration, double orientation, double angularVelocity, Material material) {
        Coin coin = new Coin(position,velocity,acceleration,27,orientation,angularVelocity, material);
        physicsEnginePane.getChildren().add(coin.getCircleView());
        entities.add(coin);
    }

    public void addLevel (int levelNumber) {
        String fileName = levelNumber+".txt";
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        //First line will be number of coins
        //Line structure: shape, material (includes immovable), position x, position y, orientation (IN DEGREES CUS I DONT KNOW HOW TO WRITE PI)
        try (Scanner scan = new Scanner(inputStream)){
            coinsRemaining = Integer.valueOf(scan.nextLine());
            while (scan.hasNext()){
                String instructionLine = scan.nextLine();
                String [] instructionArray = instructionLine.split(",");
                String shape = instructionArray[0];
                Material material = Material.valueOf(instructionArray[1].toUpperCase());
                double xPosition = Double.valueOf(instructionArray[2]);
                double yPosition = Double.valueOf(instructionArray[3]);
                double orientation = Double.valueOf(instructionArray[4])*(Math.PI/180); //Convert back to radians

                if (material == Material.IMMOVABLE){

                } else if (shape.equals("coin")) {
                    addCoin(new Vector(xPosition,yPosition),new Vector(0,0),new Vector(0,1000),orientation,0, material);
                } else { //Else movable
                    if (shape.equals("circle")){

                    } else { //Else rectangle
                        addMovableRectangle(new Vector(xPosition,yPosition),new Vector(0,0),new Vector(0,1000),
                            120,60,orientation, material);
                    }
                }
            }
        } catch (Exception readingLevel){
            System.out.println("Error reading level");
            readingLevel.printStackTrace();
        }
    }

    public void gameEnd () {

    }
}

//DO LEVELS
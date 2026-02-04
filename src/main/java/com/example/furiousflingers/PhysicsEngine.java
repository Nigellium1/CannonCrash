//STUFF TO DO: FRICTION, LAUNCHER, NOT BALLS, ROTATIONS

package com.example.furiousflingers;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

public class PhysicsEngine implements ProjectileSpawner {
    private final double sceneWidth;
    private final double sceneHeight;
    private final Pane physicsEnginePane;
    private final ArrayList<Entity> entities;
    private final LauncherType launcher;
    private final ProjectileType projectile;
    private final LevelInformation levelInformation;
    private HBox levelInformationDisplay;
    private LevelInformationUpdater levelInformationUpdater;
    private StackPane endScreen;
    private boolean levelEnded;

    public PhysicsEngine(double sceneWidth, double sceneHeight, LoadoutInformation loadoutInformation,
                         LevelInformation levelInformation) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        entities = new ArrayList<>();
        launcher = loadoutInformation.getLauncher();
        projectile = loadoutInformation.getProjectile();
        physicsEnginePane = new Pane();
        this.levelInformation = levelInformation;
        setupPane();
    }

    public Pane getPhysicsEnginePane() {
        return physicsEnginePane;
    }

    public void start() {
        addGround();
        addLevel(levelInformation.getLevelNumber());

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

                //Sleep checks for all entities
                for (Entity entity : entities) {
                    //Only movables need to sleep
                    if (entity instanceof Movable) {
                        if (entity instanceof Projectile) {
                            if (((Projectile) entity).isHasCollided()) {
                                ((Projectile) entity).sleepChecks();
                            }
                        } else {
                            ((Movable) entity).sleepChecks();
                        }
                    }
                }

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
                            entities.get(i).position.getY() < -500) { //Add roof but really high up only for railgun launcher)
                        if (entities.get(i) instanceof Coin) {
                            levelInformation.addCoinCollected();
                            levelInformationUpdater.updateLevelInformationDisplay(levelInformation, levelInformationDisplay);
                            if (levelInformation.getCoinsCollected() == levelInformation.getCoinsInLevel()) {
                                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                                delay.setOnFinished(endGame -> {
                                    gameEnd(true);
                                });
                                delay.play();
                            }
                        }

                        removeEntity(i);
                        i--; //Removed an entity so keep on same i by decrementing
                    }
                }

                //Implement magnetic projectile effect
                for (Entity entity : entities) {
                    if (entity instanceof MagneticProjectile && !((MagneticProjectile) entity).isHasCollided() &&
                            ((MagneticProjectile) entity).isHasLaunched()) {
                        ((MagneticProjectile) entity).attractMovables(entities);
                    }
                }

                //Get collision information
                ArrayList<CollisionInformation> collisionInformationList = new ArrayList<>();
                for (Entity entity : entities) {
                    //CollisionInformations contains boolean isColliding, Entity collidingEntity, Vector collisionNormal, and double penetrationDepth
                    if (entity instanceof Movable) {
                        //Don't do collision detection for unlaunched projectiles-
                        if (!(entity instanceof Projectile && !((Projectile) entity).isHasLaunched())) {
                            collisionInformationList.addAll(((Movable) entity).detectCollisions(entities));
                        }
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
                    if (entities.get(i) instanceof Coin) {
                        if (((Coin) entities.get(i)).isRemoveThisInstance()) {
                            removeEntity(i);
                            i--;
                            levelInformation.addCoinCollected();
                            levelInformationUpdater.updateLevelInformationDisplay(levelInformation, levelInformationDisplay);
                            if (levelInformation.getCoinsCollected() == levelInformation.getCoinsInLevel()) {
                                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                                delay.setOnFinished(endGame -> {
                                    gameEnd(true);
                                });
                                delay.play();
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
                for (CollisionInformation collisionInformation : collisionInformationList) {
                    collisionInformation.getEntity1().resolveFriction(collisionInformation);
                }

                //Position correction
                for (CollisionInformation collisionInformation : collisionInformationList) {
                    collisionInformation.getEntity1().positionCorrection(collisionInformation.getEntity1(), collisionInformation.getEntity2()
                            , collisionInformation.getPenetrationDepth(), collisionInformation.getCollisionNormal());
                }

                //Update appearance of entities using their position vector
                for (Entity entity : entities) {
                    if (entity instanceof Movable) {
                        ((Movable) entity).updateAppearance();
                    }
                }

                //Check projectiles to see if gravity needs to be changed (railgun or inverted)
                for (CollisionInformation collisionInformation : collisionInformationList) {
                    if (collisionInformation.getEntity1() instanceof Projectile) {
                        collisionInformation.getEntity1().acceleration = new Vector(0,1000);
                        ((Projectile) collisionInformation.getEntity1()).setHasCollided();
                    }
                    if (collisionInformation.getEntity2() instanceof Projectile) {
                        ((Projectile) collisionInformation.getEntity2()).acceleration = new Vector(0,1000);
                        ((Projectile) collisionInformation.getEntity2()).setHasCollided();
                    }
                }

                //Save collision points x value (used to check if a Movable should be put to sleep or not)
                for (CollisionInformation collisionInformation : collisionInformationList) {
                    collisionInformation.getEntity1().manageCollisionPointsQueue(collisionInformation.getCollisionPoint().getX());
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

    public void addMovableCircle(Vector position, Vector velocity, Vector acceleration,
                                 double orientation, double angularVelocity, Material material, String imageName) {
        MovableCircle circle = new MovableCircle(position, velocity, acceleration,
                27, orientation, angularVelocity, material, imageName);
        physicsEnginePane.getChildren().add(circle.getCircleView());
        entities.add(circle);
    }
    //FOR LEFT CLICK SPAWNING CIRLCES TESTING

    //So launcher can have the circle it creates
    public void addMovableCircle(MovableCircle circle) {
        physicsEnginePane.getChildren().add(circle.getCircleView());
        entities.add(circle);
    }

    public void addClickFunctionality(Launcher launcher) {
        physicsEnginePane.setOnMousePressed(event -> {
            if (levelInformation.getProjectilesRemaining() == 0) {
                return; //If all projectiles used, don't let anymore
            }
            launcher.handleMousePress(event, projectile);
//            if (event.isPrimaryButtonDown()){
//                addMovableRectangle(new Vector(event.getX(),event.getY()),new Vector(0,0),new Vector(0,1000),
//                        120,60,0,Material.STONE);
//            }
//            if (event.isSecondaryButtonDown()) {
//                addMovableRectangle(new Vector(event.getX(), event.getY()), new Vector(0, 0), new Vector(0, 1000),
//                        120, 60, 0, Material.WOOD);
//            }
        });

        physicsEnginePane.setOnMouseReleased(event -> {
            launcher.handleMouseRelease(levelInformation, levelInformationDisplay, levelInformationUpdater);
            if (levelInformation.getProjectilesRemaining() == 0) {
                //5 second delay then end
                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(endGame -> {
                    if (!levelEnded) gameEnd(false);
                });
                delay.play();
            }
        });

        physicsEnginePane.setOnMouseDragged(event -> {
            launcher.handleMouseDragged(event);
        });
    }

    public void addMovableRectangle(Vector position, Vector velocity, Vector acceleration,
                                    double width, double height, double orientation, Material material,
                                    String imageName) {
        MovableRectangle rectangle = new MovableRectangle(position, velocity, acceleration, orientation, 0, width, height, material, imageName);
        physicsEnginePane.getChildren().add(rectangle.getRectangle());
        entities.add(rectangle);
    }

    public void addImmovableRectangle(Vector position, double width, double height, Material material,
                                      String imagePath) {
        ImmovableRectangle rectangle = new ImmovableRectangle(position, 0, width, height, material, imagePath);
        ImageView rectangleView = rectangle.getRectangleView();
        rectangleView.setX(position.getX()-width/2); //Rectangle shape position is top left corner
        // so I take my center of mass and subtract half width and height to get top left corner
        rectangleView.setY(position.getY()-height/2);
        physicsEnginePane.getChildren().add(rectangleView);
        entities.add(rectangle);
    }

    public void addGround() {
        addImmovableRectangle(new Vector(sceneWidth / 2, sceneHeight - 30), sceneWidth, 60, Material.IMMOVABLE, "floor.png");
    }

    public Launcher addLauncher() {
        ProjectileSpawner projectileSpawner = this;
        return new Launcher(new Vector(150, 300), physicsEnginePane, projectileSpawner, launcher);
    }

    public void removeEntity(int index) {
        int differenceInListSizes = Math.abs(physicsEnginePane.getChildren().size() - entities.size());
        physicsEnginePane.getChildren().remove(index + differenceInListSizes); //Pane includes launcher which isn't part of entities so lists are out of sync by 1
        entities.remove(index);
    }

    public void addCoin(Vector position, Vector velocity, Vector acceleration, double orientation, double angularVelocity, Material material) {
        Coin coin = new Coin(position, velocity, acceleration, 27, orientation, angularVelocity, material);
        physicsEnginePane.getChildren().add(coin.getCircleView());
        entities.add(coin);
    }

    public void addLevel(int levelNumber) {
        String fileName = levelNumber + ".txt";
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        //First line will be number of coins
        //Second line will be number of projectiles
        //Line structure: shape, material (includes immovable), position x, position y, orientation (IN DEGREES CUS I DONT KNOW HOW TO WRITE PI)
        try (Scanner scan = new Scanner(inputStream)) {
            levelInformation.setCoinsInLevel(Integer.valueOf(scan.nextLine()));
            levelInformation.setProjectilesRemaining(Integer.valueOf(scan.nextLine()));
            levelInformationUpdater.updateLevelInformationDisplay(levelInformation, levelInformationDisplay);
            while (scan.hasNext()) {
                String instructionLine = scan.nextLine();
                String[] instructionArray = instructionLine.split(",");
                String shape = instructionArray[0];
                Material material = Material.valueOf(instructionArray[1].toUpperCase());
                double xPosition = Double.valueOf(instructionArray[2]);
                double yPosition = Double.valueOf(instructionArray[3]);
                double orientation = Double.valueOf(instructionArray[4]) * (Math.PI / 180); //Convert back to radians

                if (material == Material.IMMOVABLE) {
                    if (shape.equals("square")) {
                        addImmovableRectangle(new Vector(xPosition, yPosition), 60, 60,
                                material, instructionArray[1] + "Square.png");
                    } else { //Else rectangle
                        addImmovableRectangle(new Vector(xPosition, yPosition), 120, 60,
                                material, instructionArray[1] + "Square.png");
                    }
                } else if (shape.equals("coin")) {
                    addCoin(new Vector(xPosition, yPosition), new Vector(0, 0), new Vector(0, 1000), orientation, 0, material);
                } else { //Else movable
                    if (shape.equals("circle")) {
                        addMovableCircle(new Vector(xPosition, yPosition), new Vector(0, 0), new Vector(0, 1000),
                                orientation, 0, material, instructionArray[1] + "Circle.png");
                    } else if (shape.equals("square")) {
                        addMovableRectangle(new Vector(xPosition, yPosition), new Vector(0, 0), new Vector(0, 1000),
                                60, 60, orientation, material, instructionArray[1] + "Square.png");
                    } else { //Else rectangle
                        addMovableRectangle(new Vector(xPosition, yPosition), new Vector(0, 0), new Vector(0, 1000),
                                120, 60, orientation, material, instructionArray[1] + "Rectangle.png");
                    }
                }
            }
        } catch (Exception readingLevel) {
            System.out.println("Error reading level");
            readingLevel.printStackTrace();
        }
    }

    public void gameEnd(boolean isWin) {
        levelEnded = true;
        endScreen.setVisible(true);
        endScreen.setMouseTransparent(false);
        if (isWin) {
            endScreen.getChildren().get(0).setVisible(true);
            endScreen.getChildren().get(0).setMouseTransparent(false);
            int levelsCompleted = importLevelsCompleted();
            writeToLevelsCompleted(Math.max(levelInformation.getLevelNumber() + 1, levelsCompleted));
        } else {
            endScreen.getChildren().get(1).setVisible(true);
            endScreen.getChildren().get(1).setMouseTransparent(false);
        }
    }

    public void setLevelInformationDisplay(HBox levelInformationDisplay) {
        this.levelInformationDisplay = levelInformationDisplay;
    }

    public void setLevelInformationUpdater(LevelInformationUpdater levelInformationUpdater) {
        this.levelInformationUpdater = levelInformationUpdater;
    }

    public void setEndScreen(StackPane endScreen) {
        this.endScreen = endScreen;
    }

    public void writeToLevelsCompleted(int levelsCompleted) {
        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("FuriousFlingers");
        Path levelsCompletedFilePath = furiousFlingersFolder.resolve("levelsCompleted.txt");

        try (FileWriter fileWriter = new FileWriter(levelsCompletedFilePath.toFile())) {
            fileWriter.write(String.valueOf(levelsCompleted));
        } catch (IOException writingToLevelsCompletedFile) {
            System.out.println("Error writing to levels completed file");
            writingToLevelsCompletedFile.printStackTrace();
        }
    }

    public int importLevelsCompleted() {
        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("FuriousFlingers");
        Path levelsCompletedFilePath = furiousFlingersFolder.resolve("levelsCompleted.txt");

        if (!Files.exists(furiousFlingersFolder)) {
            try {
                Files.createDirectory(furiousFlingersFolder);
                Files.createFile(levelsCompletedFilePath);
                writeToLevelsCompleted(0);
            } catch (IOException creatingFuriousFlingersFolder) {
                System.out.println("Error creating Furious Flingers folder");
                creatingFuriousFlingersFolder.printStackTrace();
            }
        }

        if (Files.notExists(levelsCompletedFilePath)) {
            try {
                Files.createFile(levelsCompletedFilePath);
                writeToLevelsCompleted(0);
            } catch (IOException creatingLevelsCompletedFile) {
                System.out.println("Error creating levels completed file");
                creatingLevelsCompletedFile.printStackTrace();
            }
        }

        int levelsComplete = 0;
        try (Scanner scan = new Scanner(levelsCompletedFilePath)) {
            levelsComplete = Integer.parseInt(scan.nextLine());
        } catch (IOException readingLevelsCompletedFile) {
            System.out.println("Error reading levels completed file");
            readingLevelsCompletedFile.printStackTrace();
        }

        return levelsComplete;
    }
}

//DO LEVELS
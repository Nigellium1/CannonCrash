package com.example.furiousflingers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.InputStream;

public class Launcher {
    private final Vector position;
    private final LauncherType launcherType;
    private ImageView launcherTurretView;
    private boolean isProjectileLive; //Is there a valid projectile being dragged right now
    private ProjectileSpawner projectileSpawner;
    private Projectile currentProjectile;
    double allowedRadius = 100; //Might change this

    public Launcher(Vector position, Pane physicsEnginePane, ProjectileSpawner projectileSpawner,
                    LauncherType launcherType) {
        this.position = position;
        this.launcherType = launcherType;
        ImageView launcherBaseView = importImage(launcherType.getName() + "LauncherBase.png");
        launcherTurretView = importImage(launcherType.getName() + "LauncherTurret.png");

        //ImageView position is top left whereas position we are defining to be centre
        launcherTurretView.setX(position.getX() - launcherTurretView.getImage().getWidth() / 2);
        launcherTurretView.setY(position.getY() - launcherTurretView.getImage().getHeight() / 2);
        physicsEnginePane.getChildren().add(launcherTurretView);

        launcherBaseView.setX(position.getX() - launcherBaseView.getImage().getWidth() / 2);
        launcherBaseView.setY(position.getY() - launcherBaseView.getImage().getHeight() / 2);
        physicsEnginePane.getChildren().add(launcherBaseView);

        isProjectileLive = false;
        this.projectileSpawner = projectileSpawner;
    }

    public void handleMousePress(MouseEvent event, ProjectileType projectile) {
        Vector eventPosition = new Vector(event.getX(), event.getY());
        //If click position is outside of circle (approximate circle with radius of half the largest dimension of image
        if (eventPosition.subtract(position).magnitude() > allowedRadius) {
            return;
        }
        isProjectileLive = true;
        if (projectile == ProjectileType.BOMB) {
            //Actual image radius is 28 but it looks better if hitbox is slightly smaller than image (no gaps)
            currentProjectile = new BombProjectile(eventPosition, new Vector(0, 0), new Vector(0, 0), 27,
                    0, 0, Material.WOOD, ProjectileType.BOMB);
        } else if (projectile == ProjectileType.MAGNETIC) {
            currentProjectile = new MagneticProjectile(eventPosition, new Vector(0,0), new Vector(0,0),27,
                    0,0, Material.WOOD, ProjectileType.MAGNETIC);
        } else {
            currentProjectile = new Projectile(eventPosition, new Vector(0, 0), new Vector(0, 0), 27, 0, 0,
                    Material.WOOD, ProjectileType.DEFAULT);
        }
        projectileSpawner.addMovableCircle(currentProjectile);

        rotateLauncherTurret(eventPosition);
    }

    public void handleMouseRelease(LevelInformation levelInformation, HBox levelInformationDisplay, LevelInformationUpdater levelInformationUpdater) {
        if (!isProjectileLive) {
            return;
        }
        isProjectileLive = false;
        currentProjectile.setHasLaunched();
        levelInformation.minusProjectilesRemaining();
        levelInformationUpdater.updateLevelInformationDisplay(levelInformation, levelInformationDisplay);
        currentProjectile.acceleration = new Vector(0, launcherType.getGravity());
        double velocityMultiplier = this.launcherType.getVelocityMultiplier() *
                currentProjectile.position.subtract(position).magnitude() / allowedRadius;
        currentProjectile.velocity = position.subtract(currentProjectile.position).unitVector()
                .scalarMultiply(1000 * velocityMultiplier);
        currentProjectile.angularVelocity = -Math.signum(currentProjectile.velocity.getX()) * currentProjectile.velocity.magnitude() * 0.002; //Make it spin clockwise/anticlockwise depending on which way it was launched
    }

    public void handleMouseDragged(MouseEvent event) {
        if (!isProjectileLive) {
            return;
        }

        Vector eventPosition = new Vector(event.getX(), event.getY());
        //If outside circle radius 50
        if (eventPosition.subtract(position).magnitude() > allowedRadius) {
            Vector correctedVector = eventPosition.subtract(position).unitVector().scalarMultiply(allowedRadius); //Unit vector then scale by radius so still same direction
            currentProjectile.position = position.add(correctedVector);
        } else {
            currentProjectile.position = eventPosition;
        }

        rotateLauncherTurret(eventPosition);
    }

    public void rotateLauncherTurret(Vector mousePosition) {
        double angle = Math.atan2(mousePosition.getY() - position.getY(), mousePosition.getX() - position.getX());
        //Calculate angle using sohcahtoa (THIS IS CLOCKWISE BECAUSE Y IS DOWNWARDS)
        launcherTurretView.setRotate(180 + angle * (180 / Math.PI));
        //Angle already clockwise so no need to mulitiply by -1
        //Convert radians to degrees
        //Add 180 (half rotation) because we want cannon to point away from mouse not towards
    }

    public ImageView importImage(String imageName) {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(imageName);
            Image image = new Image(inputStream);
            return new ImageView(image);
        } catch (Exception importingLauncherImage) {
            System.out.println("Error importing launcher image");
            importingLauncherImage.printStackTrace();
            return null;
        }
    }
}

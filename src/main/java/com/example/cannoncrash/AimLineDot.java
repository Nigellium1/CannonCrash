package com.example.cannoncrash;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class AimLineDot {
    private final double  radius = 5;
    private final ImageView dotView = importImage("aimLineDot.png");
    private final Vector position;

    public AimLineDot(Vector position) {
        this.position = position;
        dotView.setX(position.getX()-radius); //Cus imageview coords are top left
        dotView.setY(position.getY()-radius);
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

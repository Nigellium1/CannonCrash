package com.example.furiousflingers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class ImmovableRectangle extends Immovable implements Rectangle {
    private ImageView rectangleView;
    private double width;
    private double height;
    private Vector[] vertices = new Vector[4]; //0 - top left, 1 top right, 2 bottom right, 3 bottom left

    public Vector[] getVertices() {
        return vertices;
    }

    public ImmovableRectangle(Vector position, double orientation, double width, double height, Material material, String imagePath) {
        super(position, orientation, material);
        this.width = width;
        this.height = height;
        rectangleView = importImage(imagePath); //Imageview is top left corner
        vertices[0] = position.add(new Vector(-width/2,-height/2));
        vertices[1] = position.add(new Vector(width/2,-height/2));
        vertices[2] = position.add(new Vector(width/2,height/2));
        vertices[3] = position.add(new Vector(-width/2,height/2));
    }

    public ImageView getRectangleView() {
        return rectangleView;
    }

    public void updateVerticesOrientations(double changeInAngle){
        //This does nothing cus update vertices is just for movables, might be a better way to do this??
    }

    public void setVertices(Vector[] vertices){
        //Also does nothing
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
        } catch (Exception importingRectangleImage) {
            System.out.println("Error importing rectangle image");
            importingRectangleImage.printStackTrace();
            return null;
        }
    }
}

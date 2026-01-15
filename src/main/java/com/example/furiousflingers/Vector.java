package com.example.furiousflingers;

public class Vector {

    private double x;
    private double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector add(Vector vector2) {
        return new Vector(x + vector2.x, y + vector2.y);
    }

    public Vector subtract(Vector vector2) {
        return new Vector(x - vector2.x, y - vector2.y);
    }

    public Vector scalarMultiply(double multiplier) {
        return new Vector(x * multiplier, y * multiplier);
    }

    public Vector unitVector() {
        double magnitude = magnitude();
        return new Vector(getX()/magnitude, getY()/magnitude);
    }

    public double dotProduct (Vector vector){
        return x*vector.x+y*vector.y;
    }

    //rotates by 90 degrees ANTICLOCKWISE (using javafx logic)
    public Vector perpendicularise (){
        return new Vector(y,-x);
    }

    public double vectorVectorCrossProduct (Vector vector2){
        return x*vector2.y-y*vector2.x;
    }

    public Vector vectorScalarCrossProduct (double constant){
        return new Vector(constant*y,-constant*x);
    }

    //Rotates anticlockwise about the origin in radians (matrix multiplication)
    public Vector rotate (double angle){
        return new Vector(Math.cos(angle)*x+Math.sin(angle)*y,-Math.sin(angle)*x+Math.cos(angle)*y);
        //IF THIS IS MESSED UP WERID SHIT HAPPENS
    }
}
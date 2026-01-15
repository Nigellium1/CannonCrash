package com.example.furiousflingers;

public interface Polygon {
    Vector[] getVertices();
    void setVertices(Vector[] vertices);
    void updateVerticesOrientations(double changeInAngle);
}

package com.example.cannoncrash;

public interface Polygon {
    Vector[] getVertices();
    void setVertices(Vector[] vertices);
    void updateVerticesOrientations(double changeInAngle);
}

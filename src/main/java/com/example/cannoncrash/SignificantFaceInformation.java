package com.example.cannoncrash;

public class SignificantFaceInformation {
    private Vector vertex;
    private int index;
    private boolean isEquidistant;

    public SignificantFaceInformation(Vector vertex, int index) {
        this.vertex = vertex;
        this.index = index;
        isEquidistant = false;
    }

    public Vector getVertex() {
        return vertex;
    }

    public int getIndex() {
        return index;
    }

    public void setVertex(Vector vertex) {
        this.vertex = vertex;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isEquidistant() {
        return isEquidistant;
    }

    public void setEquidistant(boolean equidistant) {
        isEquidistant = equidistant;
    }
}

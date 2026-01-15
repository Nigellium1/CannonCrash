package com.example.furiousflingers;

public enum ProjectileType {
    DEFAULT(0),
    MAGNETIC(1),
    BOMB(2),
    NERD(3);

    private final int index;

    ProjectileType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

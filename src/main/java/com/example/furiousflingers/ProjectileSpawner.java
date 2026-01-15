package com.example.furiousflingers;

//Created so launcher doesn't need to be passed the entirety of physics engine, just addMovableCircle
public interface ProjectileSpawner {
    void addMovableCircle(MovableCircle circle);
}

package com.example.cannoncrash;

//Created so launcher doesn't need to be passed the entirety of physics engine, just addMovableCircle
public interface ProjectileSpawner {
    void addMovableCircle(MovableCircle circle);
}

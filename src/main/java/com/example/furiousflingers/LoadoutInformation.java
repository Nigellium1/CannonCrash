package com.example.furiousflingers;

public class LoadoutInformation {

    private String launcher;
    private ProjectileType projectile;

    public LoadoutInformation(String launcher, ProjectileType projectile) {
        this.launcher = launcher;
        this.projectile = projectile;
    }

    public String getLauncher() {
        return launcher;
    }

    public void setLauncher(String launcher) {
        this.launcher = launcher;
    }

    public ProjectileType getProjectile() {
        return projectile;
    }

    public void setProjectile(ProjectileType projectile) {
        this.projectile = projectile;
    }
}

package com.example.cannoncrash;

public class LoadoutInformation {

    private LauncherType launcher;
    private ProjectileType projectile;

    public LoadoutInformation(LauncherType launcher, ProjectileType projectile) {
        this.launcher = launcher;
        this.projectile = projectile;
    }

    public LauncherType getLauncher() {
        return launcher;
    }

    public void setLauncher(LauncherType launcher) {
        this.launcher = launcher;
    }

    public ProjectileType getProjectile() {
        return projectile;
    }

    public void setProjectile(ProjectileType projectile) {
        this.projectile = projectile;
    }
}

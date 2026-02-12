package com.example.cannoncrash;

public class LoadoutInformation {

    private LauncherType launcherType;
    private ProjectileType projectileType;

    public LoadoutInformation(LauncherType launcherType, ProjectileType projectileType) {
        this.launcherType = launcherType;
        this.projectileType = projectileType;
    }

    public LauncherType getLauncherType() {
        return launcherType;
    }

    public void setLauncherType(LauncherType launcherType) {
        this.launcherType = launcherType;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public void setProjectileType(ProjectileType projectileType) {
        this.projectileType = projectileType;
    }
}

package com.example.furiousflingers;

public class LevelInformationCounter {
    private int projectilesRemaining;
    private int coinsCollected;
    private int coinsInLevel;

    public int getProjectilesRemaining() {
        return projectilesRemaining;
    }

    public void minusProjectilesRemaining() {
        projectilesRemaining--;
    }

    public void setProjectilesRemaining(int projectilesRemaining) {
        this.projectilesRemaining = projectilesRemaining;
    }

    public int getCoinsCollected() {
        return coinsCollected;
    }

    public void addCoinCollected() {
        coinsCollected++;
    }

    public int getCoinsInLevel() {
        return coinsInLevel;
    }

    public void setCoinsInLevel(int coinsInLevel) {
        this.coinsInLevel = coinsInLevel;
    }
}

package com.example.furiousflingers;

public class LevelInformation {
    private int projectilesRemaining;
    private int coinsCollected;
    private int coinsInLevel;
    private int levelNumber;

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

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }
}

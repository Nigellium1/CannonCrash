package com.example.cannoncrash;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class CannonCrash extends Application implements LevelInformationUpdater {
    private final double gameWidth = 1000;
    private final double gameHeight = 500;

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Cannon Crash");
        primaryStage.show();
        Scene startScene = new Scene(setupBasePane(), gameWidth, gameHeight);
        primaryStage.setScene(startScene);
    }

    public StackPane setupBasePane() {
        StackPane basePane = new StackPane();
        ImageView background = importImage("background.png");
        basePane.getChildren().add(background);
        basePane.getChildren().add(setupStartPane(basePane));
        return basePane;
    }

    public BorderPane setupStartPane(StackPane basePane) {
        BorderPane startPane = new BorderPane();
        Button playButton = createButton("playButton.png");
        playButton.setOnAction(event -> {
                    LoadoutInformation loadoutInformation = importLoadoutInformation();
                    BorderPane menuPane = setupMenuPane(basePane, loadoutInformation);
                    basePane.getChildren().set(1, menuPane); //Change current pane for new menu pane
                }
        );
        startPane.setCenter(playButton);
        return startPane;
    }

    public BorderPane setupMenuPane(StackPane basePane, LoadoutInformation loadoutInformation) {
        BorderPane menuPane = new BorderPane();

        Button backButton = createButton("backButton.png");
        backButton.setOnAction(event -> {
            BorderPane startPane = setupStartPane(basePane);
            basePane.getChildren().set(1, startPane);
        });
        menuPane.setTop(backButton);

        HBox buttonsWindow = new HBox(); //So both buttons are side by side
        buttonsWindow.setAlignment(Pos.CENTER);
        Button levelsButton = createButton("levelsButton.png");
        levelsButton.setOnAction(event -> {
            BorderPane levelsPane = setupLevelsPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, levelsPane);
        });
        Button loadoutButton = createButton("loadoutButton.png");
        loadoutButton.setOnAction(event -> {
            BorderPane loadoutPane = setupLoadoutPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, loadoutPane);
        });
        buttonsWindow.getChildren().addAll(levelsButton, loadoutButton);

        menuPane.setCenter(buttonsWindow);
        return menuPane;
    }

    public BorderPane setupLevelsPane(StackPane basePane, LoadoutInformation loadoutInformation) {
        int levelsUnlocked = importLevelsCompleted();

        Button backButton = createButton("backButton.png");
        backButton.setOnAction(event -> {
            BorderPane menuPane = setupMenuPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, menuPane);
        });

        TilePane buttonsWindow = new TilePane();
        buttonsWindow.setHgap(10);
        buttonsWindow.setVgap(10);
        buttonsWindow.setAlignment(Pos.CENTER);
        buttonsWindow.setPrefColumns(5);
        buttonsWindow.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        int levelCounter = 0;
        for (int i = 0; i < 10; i++) {
            if (levelCounter > levelsUnlocked) {
                //Stack lock image over level image
                ImageView lock = importImage("lock.png");
                ImageView level = importImage("Level" + levelCounter + ".png");
                level.setOpacity(0.70); //Fade the level
                StackPane.setAlignment(lock, Pos.TOP_RIGHT); //Put lock in top right
                StackPane combinedImages = new StackPane(level, lock);
                buttonsWindow.getChildren().add(combinedImages);
                levelCounter++;
                continue;
            }

            Button levelButton = createButton("Level" + levelCounter + ".png");
            int levelNumber = levelCounter; //Lambda expression doesn't let me use levelCounter below for some reason?
            levelButton.setOnAction(event -> {
                LevelInformation levelInformation = new LevelInformation();
                levelInformation.setLevelNumber(levelNumber);
                PhysicsEngine physicsEngine = new PhysicsEngine(gameWidth,
                        gameHeight, loadoutInformation, levelInformation);

                StackPane gamePane = setupGamePane(physicsEngine, basePane, loadoutInformation, levelInformation);

                basePane.getChildren().set(1, gamePane);
                try {
                    physicsEngine.start();
                } catch (Exception startingPhysicsEngine) {
                    System.out.println("Error starting physics engine");
                    startingPhysicsEngine.printStackTrace();
                }
            });
            buttonsWindow.getChildren().add(levelButton);
            levelCounter++;
        }

        BorderPane levelsPane = new BorderPane();
        levelsPane.setTop(backButton);
        levelsPane.setCenter(buttonsWindow);
        return levelsPane;
    }

    public StackPane setupGamePane(PhysicsEngine physicsEngine, StackPane basePane, LoadoutInformation loadoutInformation
            , LevelInformation levelInformation) {
        StackPane gamePane = new StackPane();
        Pane physicsEnginePane = physicsEngine.getPhysicsEnginePane();
        gamePane.getChildren().add(physicsEnginePane);

        Button backButton = createButton("backButton.png");
        backButton.setOnAction(event -> {
            BorderPane levelsPane = setupLevelsPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, levelsPane);
        });
        gamePane.getChildren().add(backButton);
        gamePane.setAlignment(backButton, Pos.TOP_LEFT);

        HBox levelInformationDisplay = createLevelInformation(loadoutInformation);
        gamePane.getChildren().add(levelInformationDisplay);
        gamePane.setAlignment(levelInformationDisplay, Pos.TOP_RIGHT);

        StackPane levelEndPane = setupLevelEndPane(basePane, loadoutInformation, levelInformation.getLevelNumber());
        gamePane.getChildren().add(levelEndPane);
        gamePane.setAlignment(levelEndPane, Pos.CENTER);

        physicsEngine.setLevelInformationUpdater(this::updateLevelInformationDisplay);
        physicsEngine.setLevelInformationDisplay(levelInformationDisplay);
        physicsEngine.setEndScreen(levelEndPane);

        return gamePane;
    }

    public StackPane setupLevelEndPane(StackPane basePane, LoadoutInformation loadoutInformation, int levelNumber) {
        StackPane levelEndPane = new StackPane();
        levelEndPane.setMouseTransparent(true);
        levelEndPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane victoryPane = new StackPane();
        victoryPane.setMouseTransparent(true);
        victoryPane.setVisible(false);
        victoryPane.getChildren().add(importImage("victoryEndScreen.png"));

        HBox victoryButtonRow = new HBox();
        victoryButtonRow.setPadding(new Insets(50)); //So not stuck to bottom
        Button victoryBackButton = createButton("backButton.png");
        victoryBackButton.setOnAction(event -> {
            BorderPane levelsPane = setupLevelsPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, levelsPane);
        });
        victoryButtonRow.getChildren().add(victoryBackButton);

        Button victoryReplayButton = createButton("replayButton.png");
        victoryReplayButton.setOnAction(event -> {
            LevelInformation levelInformation = new LevelInformation();
            levelInformation.setLevelNumber(levelNumber);
            PhysicsEngine physicsEngine = new PhysicsEngine(gameWidth,
                    gameHeight, loadoutInformation, levelInformation);

            StackPane gamePane = setupGamePane(physicsEngine, basePane, loadoutInformation,
                    levelInformation);
            basePane.getChildren().set(1, gamePane);
            try {
                physicsEngine.start();
            } catch (Exception startingPhysicsEngine) {
                System.out.println("Error starting physics engine");
                startingPhysicsEngine.printStackTrace();
            }
        });
        victoryButtonRow.getChildren().add(victoryReplayButton);

        //Last level shouldn't have next level button
        if (levelNumber != 9) {
            Button nextLevelButton = createButton("nextLevelButton.png");
            nextLevelButton.setOnAction(event -> {
                LevelInformation nextLevelInformation = new LevelInformation();
                nextLevelInformation.setLevelNumber(levelNumber + 1);
                PhysicsEngine physicsEngine = new PhysicsEngine(gameWidth, gameHeight, loadoutInformation, nextLevelInformation);
                StackPane gamePane = setupGamePane(physicsEngine, basePane, loadoutInformation,
                        nextLevelInformation);
                basePane.getChildren().set(1, gamePane);
                try {
                    physicsEngine.start();
                } catch (Exception startingPhysicsEngine) {
                    System.out.println("Error starting physics engine");
                    startingPhysicsEngine.printStackTrace();
                }
            });
            victoryButtonRow.getChildren().add(nextLevelButton);
        }

        victoryPane.getChildren().add(victoryButtonRow);
        victoryButtonRow.setAlignment(Pos.BOTTOM_CENTER);

        StackPane defeatPane = new StackPane();
        defeatPane.setMouseTransparent(true);
        defeatPane.setVisible(false);
        defeatPane.getChildren().add(importImage("defeatEndScreen.png"));

        HBox defeatButtonRow = new HBox();
        defeatButtonRow.setPadding(new Insets(50)); //So not stuck to bottom
        Button defeatBackButton = createButton("backButton.png");
        defeatBackButton.setOnAction(event -> {
            BorderPane levelsPane = setupLevelsPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, levelsPane);
        });
        defeatButtonRow.getChildren().add(defeatBackButton);

        Button defeatReplayButton = createButton("replayButton.png");
        defeatReplayButton.setOnAction(event -> {
            LevelInformation levelInformation = new LevelInformation();
            levelInformation.setLevelNumber(levelNumber);
            PhysicsEngine physicsEngine = new PhysicsEngine(gameWidth,
                    gameHeight, loadoutInformation, levelInformation);

            StackPane gamePane = setupGamePane(physicsEngine, basePane, loadoutInformation,
                    levelInformation);
            basePane.getChildren().set(1, gamePane);
            try {
                physicsEngine.start();
            } catch (Exception startingPhysicsEngine) {
                System.out.println("Error starting physics engine");
                startingPhysicsEngine.printStackTrace();
            }
        });
        defeatButtonRow.getChildren().add(defeatReplayButton);

        defeatPane.getChildren().add(defeatButtonRow);
        defeatButtonRow.setAlignment(Pos.BOTTOM_CENTER);

        levelEndPane.getChildren().addAll(victoryPane, defeatPane);
        return levelEndPane;
    }

    public HBox createLevelInformation(LoadoutInformation loadoutInformation) {
        HBox levelInformationDisplay = new HBox();
        levelInformationDisplay.setMouseTransparent(true); //Stop it blocking mouse clicks of below layers
        levelInformationDisplay.setAlignment(Pos.CENTER); //Make children go in middle
        levelInformationDisplay.setPadding(new Insets(10)); //Add padding on inside so its not right against the window
        levelInformationDisplay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); //Makes it not take up the whole screen
        levelInformationDisplay.setSpacing(5); //Add spacing between each child

        ImageView projectileView = importImage(loadoutInformation.getProjectileType().getName() + "Projectile.png");
        double projectileOriginalDimension = projectileView.getImage().getHeight();
        projectileView.setFitHeight(projectileOriginalDimension / 2);
        projectileView.setFitWidth(projectileOriginalDimension / 2);

        levelInformationDisplay.getChildren().add(projectileView);
        levelInformationDisplay.getChildren().add(importImage("X.png"));
        levelInformationDisplay.getChildren().add(makeNumberIntoSingleNode(0)); //Temp value

        ImageView coinView = importImage("coin.png");
        double coinOriginalDimension = coinView.getImage().getHeight();
        coinView.setFitHeight(coinOriginalDimension / 2);
        coinView.setFitWidth(coinOriginalDimension / 2);

        levelInformationDisplay.getChildren().add(coinView);
        levelInformationDisplay.getChildren().add(importImage("X.png"));
        levelInformationDisplay.getChildren().add(makeNumberIntoSingleNode(0)); //Temp value
        levelInformationDisplay.getChildren().add(importImage("slash.png"));
        levelInformationDisplay.getChildren().add(makeNumberIntoSingleNode(0)); //Temp value

        return levelInformationDisplay;
    }

    //Takes in int and returns images combined to make that number as a node
    public HBox makeNumberIntoSingleNode(int number) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        String numberString = String.valueOf(number);
        for (int i = 0; i < numberString.length(); i++) {
            hBox.getChildren().add(importImage(numberString.charAt(i) + ".png"));
        }
        return hBox;
    }

    public void updateLevelInformationDisplay(LevelInformation levelInformation, HBox hBox) {
        hBox.getChildren().set(2, makeNumberIntoSingleNode(levelInformation.getProjectilesRemaining()));
        hBox.getChildren().set(5, makeNumberIntoSingleNode(levelInformation.getCoinsCollected()));
        hBox.getChildren().set(7, makeNumberIntoSingleNode(levelInformation.getCoinsInLevel()));
    }

    public BorderPane setupLoadoutPane(StackPane basePane, LoadoutInformation loadoutInformation) {
        Button backButton = createButton("backButton.png");
        backButton.setOnAction(event -> {
            BorderPane menuPane = setupMenuPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, menuPane);
        });

        int levelsUnlocked = importLevelsCompleted();

        TilePane launcherGrid = new TilePane();
        launcherGrid.setPrefColumns(2);

        addLoadoutSelection(LauncherType.DEFAULT, launcherGrid, loadoutInformation,
                "defaultLauncher.png", true);

        boolean railGunUnlocked = false;
        if (levelsUnlocked >= 5) railGunUnlocked = true;
        addLoadoutSelection(LauncherType.RAILGUN, launcherGrid, loadoutInformation,
                "railgunLauncher.png", railGunUnlocked);

        boolean invertedUnlocked = false;
        if (levelsUnlocked >= 6) invertedUnlocked = true;
        addLoadoutSelection(LauncherType.INVERTED, launcherGrid, loadoutInformation,
                "invertedLauncher.png", invertedUnlocked);

        changeSelectedFrame(loadoutInformation.getLauncherType(), launcherGrid);

        TilePane projectileGrid = new TilePane();
        projectileGrid.setPrefColumns(2);

        addLoadoutSelection(ProjectileType.DEFAULT, projectileGrid, loadoutInformation,
                "defaultProjectile.png", true);

        boolean bombUnlocked = false;
        if (levelsUnlocked >= 4) bombUnlocked = true;
        addLoadoutSelection(ProjectileType.BOMB, projectileGrid, loadoutInformation,
                "bombProjectile.png", bombUnlocked);

        boolean magneticUnlocked = false;
        if (levelsUnlocked >= 6) magneticUnlocked = true;
        addLoadoutSelection(ProjectileType.MAGNETIC, projectileGrid, loadoutInformation,
                "magneticProjectile.png", magneticUnlocked);

        boolean nerdUnlocked = false;
        if (levelsUnlocked >= 10) nerdUnlocked = true;
        addLoadoutSelection(ProjectileType.NERD, projectileGrid, loadoutInformation,
                "nerdProjectile.png", nerdUnlocked);

        changeSelectedFrame(loadoutInformation.getProjectileType(), projectileGrid); //Original set frame

        HBox gridsWindow = new HBox();
        gridsWindow.setAlignment(Pos.CENTER);
        gridsWindow.getChildren().addAll(launcherGrid, projectileGrid);

        BorderPane loadoutPane = new BorderPane();
        loadoutPane.setTop(backButton);
        loadoutPane.setCenter(gridsWindow);
        return loadoutPane;
    }

    public void addLoadoutSelection(LoadoutType loadoutOption, TilePane grid,
                                    LoadoutInformation loadoutInformation, String imagePath, boolean unlocked) {
        ImageView background = importImage("loadoutBackground.png"); //Same background for projectiles and launchers
        ImageView loadoutChoiceView = importImage(imagePath);

        if (unlocked) {
            StackPane combinedImages = new StackPane(background, loadoutChoiceView);
            Button button = createButton(combinedImages);
            button.setOnAction(event -> {
                if (loadoutOption instanceof ProjectileType) {
                    loadoutInformation.setProjectileType(((ProjectileType) loadoutOption));
                } //Else, has to be a launcher
                else {
                    loadoutInformation.setLauncherType(((LauncherType) loadoutOption));
                }
                writeToLoadoutInformation(loadoutInformation);
                changeSelectedFrame(loadoutOption, grid); //Change frame whenever any button is pressed
            });
            grid.getChildren().add(button);
        } else {
            background.setOpacity(0.7);
            loadoutChoiceView.setOpacity(0.7);
            ImageView lock = importImage("lock.png");
            StackPane.setAlignment(lock, Pos.TOP_RIGHT);
            StackPane combinedImages = new StackPane(background, loadoutChoiceView, lock);
            grid.getChildren().add(combinedImages);
        }
    }

    public Button createButton(String imageName) {
        ImageView buttonTexture = importImage(imageName);
        Button button = new Button("", buttonTexture);
        button.setStyle("-fx-background-color: transparent;");
        addShadows(button);
        return button;
    }

    public Button createButton(Node buttonImage) {
        Button button = new Button("", buttonImage);
        button.setStyle("-fx-background-color: transparent;");
        addShadows(button);
        return button;
    }

    public ImageView importImage(String imageName) {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(imageName);
            Image image = new Image(inputStream);
            return new ImageView(image);
        } catch (Exception importingImage) {
            System.out.println("Error importing image");
            importingImage.printStackTrace();
            return null;
        }
    }

    public void addShadows(Button button) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(0);   //No smooth shadow
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(80, 80, 80));

        button.setOnMouseEntered(e -> {
            button.setEffect(shadow);
            button.setTranslateX(-2);
            button.setTranslateY(-2);
        });
        button.setOnMouseExited(e -> {
            button.setEffect(null);
            button.setTranslateX(0);
            button.setTranslateY(0);
        });
    }
    //^Google AI

    public LoadoutInformation importLoadoutInformation() {
        LoadoutInformation loadoutInformation = new LoadoutInformation(LauncherType.DEFAULT, ProjectileType.DEFAULT);

        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("CannonCrash");
        Path loadoutFilePath = furiousFlingersFolder.resolve("loadout.txt");

        if (!Files.exists(furiousFlingersFolder)) {
            try {
                Files.createDirectory(furiousFlingersFolder);
                Files.createFile(loadoutFilePath);
                writeToLoadoutInformation(new LoadoutInformation(LauncherType.DEFAULT,
                        ProjectileType.DEFAULT));
            } catch (IOException creatingFuriousFlingersFolder) {
                System.out.println("Error creating Furious Flingers folder");
                creatingFuriousFlingersFolder.printStackTrace();
            }
        }

        if (Files.notExists(loadoutFilePath)) {
            try {
                Files.createFile(loadoutFilePath);
                writeToLoadoutInformation(new LoadoutInformation(LauncherType.DEFAULT,
                        ProjectileType.DEFAULT));
            } catch (IOException creatingLoadoutFile) {
                System.out.println("Error creating loadout file");
                creatingLoadoutFile.printStackTrace();
            }
        }

        String launcher = "default";
        String projectile = "default";
        try (Scanner scan = new Scanner(loadoutFilePath)) {
            launcher = scan.nextLine();
            projectile = scan.nextLine();
        } catch (IOException readingLoadoutFile) {
            System.out.println("Error reading loadout file");
            readingLoadoutFile.printStackTrace();
        }

        switch (launcher) {
            case "railgun":
                loadoutInformation.setLauncherType(LauncherType.RAILGUN);
                break;
            case "inverted":
                loadoutInformation.setLauncherType(LauncherType.INVERTED);
                break;
            default:
                loadoutInformation.setLauncherType(LauncherType.DEFAULT);
        }

        switch (projectile) {
            case "bomb":
                loadoutInformation.setProjectileType(ProjectileType.BOMB);
                break;
            case "magnetic":
                loadoutInformation.setProjectileType(ProjectileType.MAGNETIC);
                break;
            case "nerd":
                loadoutInformation.setProjectileType(ProjectileType.NERD);
                break;
            default:
                loadoutInformation.setProjectileType(ProjectileType.DEFAULT);
        }

        return loadoutInformation;
    }

    public void changeSelectedFrame(LoadoutType loadoutOption, TilePane grid) {
        //Reset all buttons
        for (Node button : grid.getChildren()) {
            if (button instanceof Button) {
                //Check to see if it has the 3rd layer in the stackpane (outline)
                if (((StackPane) ((Button) button).getGraphic()).getChildren().size() > 2) {
                    ((StackPane) ((Button) button).getGraphic()).getChildren().remove(2);
                }
            }
        }

        //Add white frame on selected
        ImageView loadoutSelectedFrame = importImage("loadoutSelectedFrame.png");
        if (loadoutOption == LauncherType.DEFAULT) {
            ((StackPane) ((Button) grid.getChildren().get(0)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == LauncherType.RAILGUN) {
            ((StackPane) ((Button) grid.getChildren().get(1)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == LauncherType.INVERTED) {
            ((StackPane) ((Button) grid.getChildren().get(2)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }

        if (loadoutOption == ProjectileType.DEFAULT) {
            ((StackPane) ((Button) grid.getChildren().get(0)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == ProjectileType.BOMB) {
            ((StackPane) ((Button) grid.getChildren().get(1)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == ProjectileType.MAGNETIC) {
            ((StackPane) ((Button) grid.getChildren().get(2)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == ProjectileType.NERD) {
            ((StackPane) ((Button) grid.getChildren().get(3)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
    }

    public void writeToLoadoutInformation(LoadoutInformation loadoutInformation) {
        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("CannonCrash");
        Path loadoutFilePath = furiousFlingersFolder.resolve("loadout.txt");

        try (FileWriter fileWriter = new FileWriter(loadoutFilePath.toFile())) {
            fileWriter.write(loadoutInformation.getLauncherType().getName() + "\n");
            fileWriter.write(loadoutInformation.getProjectileType().getName());
        } catch (IOException writingToLoadoutFile) {
            System.out.println("Error writing to loadout file");
            writingToLoadoutFile.printStackTrace();
        }
    }

    public int importLevelsCompleted() {
        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("CannonCrash");
        Path levelsCompletedFilePath = furiousFlingersFolder.resolve("levelsCompleted.txt");

        if (!Files.exists(furiousFlingersFolder)) {
            try {
                Files.createDirectory(furiousFlingersFolder);
                Files.createFile(levelsCompletedFilePath);
                writeToLevelsCompleted(0);
            } catch (IOException creatingFuriousFlingersFolder) {
                System.out.println("Error creating Furious Flingers folder");
                creatingFuriousFlingersFolder.printStackTrace();
            }
        }

        if (Files.notExists(levelsCompletedFilePath)) {
            try {
                Files.createFile(levelsCompletedFilePath);
                writeToLevelsCompleted(0);
            } catch (IOException creatingLevelsCompletedFile) {
                System.out.println("Error creating levels completed file");
                creatingLevelsCompletedFile.printStackTrace();
            }
        }

        int levelsComplete = 0;
        try (Scanner scan = new Scanner(levelsCompletedFilePath)) {
            levelsComplete = Integer.parseInt(scan.nextLine());
        } catch (IOException readingLevelsCompletedFile) {
            System.out.println("Error reading levels completed file");
            readingLevelsCompletedFile.printStackTrace();
        }

        return levelsComplete;
    }

    public void writeToLevelsCompleted(int levelsCompleted) {
        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("CannonCrash");
        Path levelsCompletedFilePath = furiousFlingersFolder.resolve("levelsCompleted.txt");

        try (FileWriter fileWriter = new FileWriter(levelsCompletedFilePath.toFile())) {
            fileWriter.write(String.valueOf(levelsCompleted));
        } catch (IOException writingToLevelsCompletedFile) {
            System.out.println("Error writing to levels completed file");
            writingToLevelsCompletedFile.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

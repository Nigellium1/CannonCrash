package com.example.furiousflingers;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class FuriousFlingers extends Application {

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Furious Flingers");
        primaryStage.show();
        Scene startScene = new Scene(setupBasePane(), 1000, 500);
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
        int levelsUnlocked = 5; //THIS SHOULD BE PASSED TO THE ROUTINE

        Button backButton = createButton("backButton.png");
        backButton.setOnAction(event -> {
            BorderPane menuPane = setupMenuPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, menuPane);
        });

        GridPane buttonsWindow = new GridPane();
        buttonsWindow.setAlignment(Pos.CENTER);
        int levelCounter = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                if (levelCounter > levelsUnlocked) {
                    //Stack lock image over level image
                    ImageView lock = importImage("lock.png");
                    ImageView level = importImage(levelCounter + ".png");
                    level.setOpacity(0.70); //Fade the level
                    StackPane.setAlignment(lock, Pos.TOP_RIGHT); //Put lock in top right
                    //Small margin
                    StackPane combinedImages = new StackPane(level, lock);
                    buttonsWindow.add(combinedImages, j, i);
                    levelCounter++;
                    continue;
                }

                Button levelButton = createButton(levelCounter + ".png");
                levelCounter++;
                int levelNumber = levelCounter; //Lambda expression doesn't let me use levelCounter below for some reason?
                levelButton.setOnAction(event -> {
                    PhysicsEngine physicsEngine = new PhysicsEngine(levelButton.getScene().getWidth(),
                            levelButton.getScene().getHeight(), loadoutInformation);
                    basePane.getChildren().set(1, physicsEngine.getPhysicsEnginePane());
                    try {
                        physicsEngine.start(levelNumber);
                    } catch (Exception startingPhysicsEngine) {
                        System.out.println("Error starting physics engine");
                        startingPhysicsEngine.printStackTrace();
                    }
                });
                buttonsWindow.add(levelButton, j, i);
            }
        }

        BorderPane levelsPane = new BorderPane();
        levelsPane.setTop(backButton);
        levelsPane.setCenter(buttonsWindow);
        return levelsPane;
    }

    public BorderPane setupLoadoutPane(StackPane basePane, LoadoutInformation loadoutInformation) {
        Button backButton = createButton("backButton.png");
        backButton.setOnAction(event -> {
            BorderPane menuPane = setupMenuPane(basePane, loadoutInformation);
            basePane.getChildren().set(1, menuPane);
        });

        GridPane launcherGrid = new GridPane();
        addLoadoutSelection(LauncherType.DEFAULT,launcherGrid,0,0,loadoutInformation,"defaultLauncher.png");
        addLoadoutSelection(LauncherType.RAILGUN,launcherGrid,1,0,loadoutInformation,"railgunLauncher.png");
        changeSelectedFrame(loadoutInformation.getLauncher(),launcherGrid);

        GridPane projectileGrid = new GridPane();
        addLoadoutSelection(ProjectileType.DEFAULT, projectileGrid, 0, 0, loadoutInformation, "defaultProjectile.png");
        addLoadoutSelection(ProjectileType.BOMB, projectileGrid, 1, 0, loadoutInformation, "bombProjectile.png");
        addLoadoutSelection(ProjectileType.MAGNETIC, projectileGrid, 0, 1, loadoutInformation, "magneticProjectile.png");
        addLoadoutSelection(ProjectileType.NERD, projectileGrid, 1, 1, loadoutInformation, "nerdProjectile.png");
        changeSelectedFrame(loadoutInformation.getProjectile(),projectileGrid); //Original set frame

        HBox gridsWindow = new HBox();
        gridsWindow.setAlignment(Pos.CENTER);
        gridsWindow.getChildren().addAll(launcherGrid, projectileGrid);

        BorderPane loadoutPane = new BorderPane();
        loadoutPane.setTop(backButton);
        loadoutPane.setCenter(gridsWindow);
        return loadoutPane;
    }

    public void addLoadoutSelection(LoadoutType loadoutOption, GridPane grid, int i, int j, LoadoutInformation loadoutInformation, String imagePath) {
        ImageView background = importImage("loadoutBackground.png"); //Same background for projectiles and launchers
        ImageView loadoutChoiceView = importImage(imagePath);
        StackPane combinedImages = new StackPane(background, loadoutChoiceView);
        Button button = createButton(combinedImages);
        button.setOnAction(event -> {
            if (loadoutOption instanceof ProjectileType){
                loadoutInformation.setProjectile(((ProjectileType)loadoutOption));
            } //Else, has to be a launcher
            else {
                loadoutInformation.setLauncher(((LauncherType) loadoutOption));
            }
            writeToLoadoutInformation(loadoutInformation);
            changeSelectedFrame(loadoutOption,grid); //Change frame whenever any button is pressed
        });
        grid.add(button, i, j);
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
            button.setTranslateX(2);
            button.setTranslateY(2);
        });
    }
    //^AI, google

    public LoadoutInformation importLoadoutInformation (){
        LoadoutInformation loadoutInformation = new LoadoutInformation(LauncherType.DEFAULT,ProjectileType.DEFAULT);

        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("FuriousFlingers");
        Path loadoutFilePath = furiousFlingersFolder.resolve("loadout.txt");

        if (!Files.exists(furiousFlingersFolder)){
            try {
                Files.createDirectory(furiousFlingersFolder);
                Files.createFile(loadoutFilePath);
                writeToLoadoutInformation(new LoadoutInformation(LauncherType.DEFAULT,
                        ProjectileType.DEFAULT));
            } catch (IOException creatingFuriousFlingersFolder){
                System.out.println("Error creating Furious Flingers folder");
                creatingFuriousFlingersFolder.printStackTrace();
            }
        }

        if (Files.notExists(loadoutFilePath)){
            try {
                Files.createFile(loadoutFilePath);
                writeToLoadoutInformation(new LoadoutInformation(LauncherType.DEFAULT,
                        ProjectileType.DEFAULT));
            } catch (IOException creatingLoadoutFile){
                System.out.println("Error creating loadout file");
                creatingLoadoutFile.printStackTrace();
            }
        }

        String launcher = "default";
        String projectile = "default";
        try (Scanner scan = new Scanner(loadoutFilePath)){
            launcher = scan.nextLine();
            projectile = scan.nextLine();
        } catch (IOException readingLoadoutFile){
            System.out.println("Error reading loadout file");
            readingLoadoutFile.printStackTrace();
        }

        switch (launcher){
            case "railgun":
                loadoutInformation.setLauncher(LauncherType.RAILGUN);
                break;
            default:
                loadoutInformation.setLauncher(LauncherType.DEFAULT);
        }

        switch (projectile){
            case "bomb":
                loadoutInformation.setProjectile(ProjectileType.BOMB);
                break;
            case "magnetic":
                loadoutInformation.setProjectile(ProjectileType.MAGNETIC);
                break;
            case "nerd":
                loadoutInformation.setProjectile(ProjectileType.NERD);
                break;
            default:
                loadoutInformation.setProjectile(ProjectileType.DEFAULT);
        }

        return loadoutInformation;
    }

    public void changeSelectedFrame (LoadoutType loadoutOption , GridPane grid){
        //Reset all buttons
        for (Node button:grid.getChildren()){
            //Check to see if it has the 3rd layer in the stackpane (outline)
            if (((StackPane)((Button)button).getGraphic()).getChildren().size()>2){
                ((StackPane)((Button)button).getGraphic()).getChildren().remove(2);
            }
        }

        //Add white frame on selected
        ImageView loadoutSelectedFrame = importImage("loadoutSelectedFrame.png");
        if (loadoutOption == LauncherType.DEFAULT){
            ((StackPane)((Button)grid.getChildren().get(0)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == LauncherType.RAILGUN){
            ((StackPane)((Button)grid.getChildren().get(1)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }

        if (loadoutOption == ProjectileType.DEFAULT){
            ((StackPane)((Button)grid.getChildren().get(0)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == ProjectileType.BOMB){
            ((StackPane)((Button)grid.getChildren().get(1)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == ProjectileType.MAGNETIC){
            ((StackPane)((Button)grid.getChildren().get(2)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
        if (loadoutOption == ProjectileType.NERD){
            ((StackPane)((Button)grid.getChildren().get(3)).getGraphic()).getChildren().add(loadoutSelectedFrame);
        }
    }

    public void writeToLoadoutInformation (LoadoutInformation loadoutInformation){
        String localAppDataFolderPathString = System.getenv("localappdata");
        Path localAppDataPath = Path.of(localAppDataFolderPathString);
        Path furiousFlingersFolder = localAppDataPath.resolve("FuriousFlingers");
        Path loadoutFilePath = furiousFlingersFolder.resolve("loadout.txt");

        try (FileWriter fileWriter = new FileWriter(loadoutFilePath.toFile())){
            fileWriter.write(loadoutInformation.getLauncher().getName()+"\n");
            fileWriter.write(loadoutInformation.getProjectile().getName());
        }catch (IOException writingToLoadoutFile){
            System.out.println("Error writing to loadout file");
            writingToLoadoutFile.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.game.minesweeper;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;


public class Main extends Application {
    private GameController gameController = new GameController();
    @Override
    public void start(Stage stage)  {
        gameController.initialize();
        gameController.setStage(stage);
        Scene scene = new Scene(gameController.getBorderPane(), 800, 600);
        stage.setScene(scene);
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon_minesweeper.png"), "Icon image not found!"));
        stage.getIcons().add(icon);
        stage.setTitle("Minesweeper");
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
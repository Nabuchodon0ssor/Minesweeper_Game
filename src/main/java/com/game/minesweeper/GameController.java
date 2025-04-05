package com.game.minesweeper;


import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;


public class GameController {
    private int side = 10;
    private double minePercentage = 0.1;
    private double buttonSize = 55;

    private GameObject[][] gameField;
    private Button[][] buttons;
    private int countMinesOnField;
    private int countFlags;
    private int countClosedTiles;
    private static final String MINE = "/images/bomb.png";
    private static final String BLAST = "/images/blast.png";
    private static final String FLAG = "/images/flag.png";
    private boolean isGameStopped;
    private GridPane gridPane;
    private Label minesLeftLabel;
    private Label timeLabel;
    private long startTime;
    private BorderPane borderPane;
    private Timeline timeline;
    private Stage stage;
    private GameObject mineRanInto;

    private ToggleButton soundToggleButton;
    private ImageView soundIcon;
    private boolean isSoundEnabled = true;


    public GameController() {

    }
    private SoundManager soundManager = new SoundManager();

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void initialize() {
        borderPane = createBorderPane();
        createGameBoard(borderPane);
        createGame();
    }

    private Button createRestartButton() {
        Button restartButton = new Button("Restart");
        restartButton.setOnAction(e -> restart());
        return restartButton;
    }

    private HBox createTopBar() {
        HBox leftPanel = createLeftPanel();
        HBox rightPanel = createRightPanel();

        HBox topBar = new HBox(leftPanel, rightPanel);
        topBar.setAlignment(Pos.CENTER);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        return topBar;
    }

    private HBox createLeftPanel() {
        MenuBar menuBar = createMenuBar();
        Button restartButton = createRestartButton();
        Label minesLeftLabel = createMinesLeftLabel();
        Label timeLabel = createTimeLabel();

        HBox leftPanel = new HBox(10, menuBar, restartButton, minesLeftLabel, timeLabel);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        return leftPanel;
    }
    private Label createMinesLeftLabel(){minesLeftLabel = new Label("Mines Left: ");
        return minesLeftLabel;}

    private Label createTimeLabel(){timeLabel = new Label("Time: 0");return timeLabel;}

    private HBox createRightPanel() {
        HBox soundControlPanel = createSoundControlPanel();
        soundControlPanel.setAlignment(Pos.CENTER_RIGHT);
        return soundControlPanel;
    }

    private BorderPane createBorderPane() {
        BorderPane borderPane = new BorderPane();
        HBox topBar = createTopBar();
        borderPane.setTop(topBar);
        return borderPane;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    private void createGameBoard(BorderPane borderPane) {
        gridPane = createGridPane();
        borderPane.setCenter(gridPane);
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(2);
        gridPane.setVgap(2);
        gridPane.setPadding(new Insets(5));
        return gridPane;
    }

    private MenuBar createMenuBar() {
        Menu difficultyMenu = new Menu("Difficulty");
        MenuItem easy = new MenuItem("Easy");
        easy.setOnAction(e -> setDifficulty(10, 0.1, 55));
        MenuItem medium = new MenuItem("Medium");
        medium.setOnAction(e -> setDifficulty(15, 0.15, 45));
        MenuItem hard = new MenuItem("Hard");
        hard.setOnAction(e -> setDifficulty(20, 0.2, 35));
        difficultyMenu.getItems().addAll(easy, medium, hard);
        return new MenuBar(difficultyMenu);
    }

    private void createGame(){
        gameField = new GameObject[side][side];
        buttons = new Button[side][side];
        gridPane.getChildren().clear();
        countMinesOnField = 0;
        startTime = 0;
        countClosedTiles = side * side;

        for (int y = 0; y < side; y++) {
            for (int x = 0; x < side; x++) {
                boolean isMine = Math.random() < minePercentage;
                gameField[y][x] = new GameObject(x, y, isMine);
                if (isMine) {
                    countMinesOnField++;
                }

                Button cell = createButton(x,y,buttonSize);

                gridPane.add(cell, x, y);
                buttons[y][x] = cell;
            }
        }
        countFlags = countMinesOnField;
        countMineNeighbors();
        updateLabels();

        PauseTransition delay = new PauseTransition(Duration.millis(30));
        delay.setOnFinished(event -> resizeWindow());
        delay.play();
    }

    private Button createButton(int x, int y, double buttonSize) {
        Button cell = new Button();
        cell.setPrefSize(buttonSize,buttonSize); // Размер кнопки
        cell.setStyle("-fx-background-color: gray;");
        final int currentX = x;
        final int currentY = y;
        // Обработка левого клика
        cell.setOnMouseClicked(event -> {
            if (!isGameStopped) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    openTile(currentX, currentY, cell);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    markTile(currentX, currentY, cell);
                }
            }
        });
        return cell;
    }

    public void setDifficulty(int side, double minePercentage, double buttonSize) {
        this.side = side;
        this.minePercentage = minePercentage;
        this.buttonSize = buttonSize;
        restart();
    }

    private void openTile(int x, int y, Button cell) {
        GameObject gameObject = gameField[y][x];
        if(gameObject.isOpen || gameObject.isFlag || isGameStopped) return;

        revealCell(gameObject, cell);
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            startTimer();
        }

        if (gameObject.isMine) {
            mineRanInto = gameObject;
            handleMine(cell);
        } else {
            soundManager.playClickSound();
            handleSafeCell(gameObject, cell);
        }
        checkForWin();
    }

    private void revealCell(GameObject gameObject, Button cell) {
        gameObject.isOpen = true;
        countClosedTiles--;
        cell.setStyle("-fx-background-color: DARKSEAGREEN;");
    }
    private void handleMine(Button cell) {
        Image blastImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(BLAST), "Blast image not found!"));
        ImageView blastView = new ImageView(blastImage);
        blastView.setFitWidth(buttonSize*0.5);
        blastView.setFitHeight(buttonSize*0.5);
        cell.setGraphic(blastView);
        cell.setStyle("-fx-background-color: yellow;");
        gameOver();
    }

    private void handleSafeCell(GameObject gameObject, Button cell) {
        if (gameObject.countMineNeighbors > 0) {
            cell.setText(String.valueOf(gameObject.countMineNeighbors));
        } else {
            cell.setText("");
            for (GameObject neighbor : GameUtils.getNeighbors(gameObject, gameField, side)) {
                if (!neighbor.isOpen) {
                    Button neighborCell = getButtonAt(neighbor.x, neighbor.y);
                    revealCell(neighbor, neighborCell);
                    handleSafeCell(neighbor, neighborCell);
                }
            }
        }
    }

    private void checkForWin() {
        if (countClosedTiles == countMinesOnField) {
            win();
        }
    }

    private void markTile(int x, int y, Button cell){
        GameObject gameObject = gameField[y][x];
        if(gameObject.isOpen || isGameStopped || (countFlags==0 && !gameObject.isFlag)) return;
        if (!gameObject.isFlag) {
            markCellWithFlag(gameObject, cell);
        } else {
            unMarkCellWithFlag(gameObject, cell);
        }
        updateLabels();
    }

    private void markCellWithFlag(GameObject gameObject, Button cell) {

        Image flagImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(FLAG), "Flag image not found!"));
        ImageView flagView = new ImageView(flagImage);
        flagView.setFitWidth(buttonSize*0.5);
        flagView.setFitHeight(buttonSize*0.5);
        cell.setGraphic(flagView);

        gameObject.isFlag = true;
        cell.setStyle("-fx-background-color: cornflowerblue;");
        countFlags--;
        soundManager.playFlagSound();
    }
    private void unMarkCellWithFlag(GameObject gameObject, Button cell) {
        gameObject.isFlag = false;
        cell.setGraphic(null);
        cell.setStyle("-fx-background-color: gray;");
        countFlags++;
        soundManager.playFlagSound();
    }

    private void showTilesWithMines() {
        for (int y = 0; y < side; y++) {
            for (int x = 0; x < side; x++) {
                GameObject gameObject = gameField[y][x];
                Button cell = getButtonAt(x,y);
                if(gameObject==mineRanInto) continue;

                if (gameObject.isMine) {
                    revealCell(gameObject, cell);
                    Image mineImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(MINE), "Mine image not found!"));
                    ImageView mineView = new ImageView(mineImage);
                    mineView.setFitWidth(buttonSize*0.5);
                    mineView.setFitHeight(buttonSize*0.5);
                    cell.setGraphic(mineView);
                }
                else if (gameObject.isFlag && !gameObject.isMine) {
                    cell.setStyle("-fx-background-color: yellow;");
                }
            }
        }
    }

    private void gameOver() {
        isGameStopped = true;
        showTilesWithMines();
        soundManager.playDefeatSound();
        if (timeline != null) {
            timeline.stop();
        }
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon_minesweeper.png"), "Icon image not found!"));
        ImageView iconView = new ImageView(icon);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Game Over! You hit a mine.");
        alert.setGraphic(iconView);
        alert.showAndWait();

    }

    private void win() {
        isGameStopped = true;
        soundManager.playVictorySound();
        showTilesWithMines();
        timeline.stop();
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon_minesweeper.png"), "Icon image not found!"));
        ImageView iconView = new ImageView(icon);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText(null);
        alert.setContentText("You Won! All mines were successfully avoided.");
        alert.setGraphic(iconView);
        alert.showAndWait();

    }

    public void restart() {
        isGameStopped = false;
        countClosedTiles = side*side;
        countMinesOnField = 0;
        if (timeline != null) {
            timeline.stop();
        }
        createGame();
    }

    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateLabels();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateLabels() {
        if (minesLeftLabel != null && timeLabel != null) {
            minesLeftLabel.setText("Mines Left: " + countFlags);
            if (startTime != 0) {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                Platform.runLater(() -> {
                    timeLabel.setText("Time: " + elapsedTime);
                });
            }
        }
    }

    private void countMineNeighbors(){
        for (int y = 0; y < side; y++) {
            for (int x = 0; x < side; x++) {
                GameObject currentObject = gameField[y][x];
                if(currentObject.isMine) continue;
                int mines = 0;
                for(GameObject go : GameUtils.getNeighbors(currentObject, gameField, side)){
                    if(go.isMine) mines++;
                }
                currentObject.countMineNeighbors = mines;
            }
        }
    }

    private Button getButtonAt(int x, int y) {
        return buttons[y][x];
    }

    private double getTopBarHeight() {
        if (borderPane.getTop() != null) {
            return borderPane.getTop().prefHeight(-1);
        }
        return 50;
    }

    protected void resizeWindow() {
        if (stage != null && gridPane != null) {
            //double gridWidth = side * (buttonSize + 2);
            //double gridHeight = side * (buttonSize + 2);

            double gridWidth = gridPane.prefWidth(-1);
            double gridHeight = gridPane.prefHeight(-1);
            double topBarHeight = getTopBarHeight();
            stage.setWidth(gridWidth);
            stage.setHeight(gridHeight + topBarHeight+40);

        }
    }

    private HBox createSoundControlPanel() {
        HBox soundControlPanel = new HBox(5);
        soundControlPanel.setAlignment(Pos.CENTER_RIGHT);
        soundIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/sound-icon.png"))));
        soundIcon.setFitWidth(20);
        soundIcon.setFitHeight(20);

        ToggleButton soundToggleButton = new ToggleButton("On");
        soundToggleButton.setSelected(true);
        soundToggleButton.setOnAction(event -> toggleSound(soundIcon, soundToggleButton));

        soundControlPanel.getChildren().addAll(soundIcon, soundToggleButton);

        return soundControlPanel;
    }

    private void toggleSound(ImageView soundIcon, ToggleButton soundToggleButton) {
        isSoundEnabled = !isSoundEnabled;
        soundToggleButton.setText(isSoundEnabled ? "On" : "Off");
        soundIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                isSoundEnabled ? "/images/sound-icon.png" : "/images/mute-icon.png"
        ))));
        soundManager.setSoundEnabled(isSoundEnabled);
    }
}

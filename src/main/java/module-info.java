module com.game.minesweeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens com.game.minesweeper to javafx.fxml;
    exports com.game.minesweeper;
}
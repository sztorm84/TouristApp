package edp.touristapp.components;

import javafx.scene.control.Button;

public class ColoredButton extends Button {

    public ColoredButton() {
        super();
        setupStyle();
        setupListeners();
    }

    private void setupStyle() {
        setStyle("-fx-background-color: #B0B0B0;" +
                "-fx-text-fill: black;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 16 8 16;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );
    }

    private void setupListeners() {
        setOnMouseEntered(e -> setStyle("-fx-background-color: #909090;" +
                "-fx-text-fill: black;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 16 8 16;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 7, 0, 0, 3);"
        ));
        setOnMouseExited(e -> setupStyle());

        setOnMousePressed(e -> setStyle("-fx-background-color: #707070;" +
                "-fx-text-fill: black;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 16 8 16;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 4);"
        ));
        setOnMouseReleased(e -> setStyle("-fx-background-color: #909090;" +
                "-fx-text-fill: black;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 16 8 16;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 7, 0, 0, 3);"
        ));
    }
}

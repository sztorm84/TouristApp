package edp.touristapp.components;

import javafx.scene.control.TextField;

public class ColoredTextField extends TextField {

    public ColoredTextField() {
        super();
        setupStyle();
        setupListeners();
    }

    private void setupStyle() {
        setStyle("-fx-background-color: white;" +
                "-fx-text-fill: black;" +
                "-fx-border-color: #A0A0A0;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 6 10 6 10;" +
                "-fx-font-size: 14px;");
    }

    private void setupListeners() {
        focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                setStyle("-fx-background-color: white;" +
                        "-fx-text-fill: black;" +
                        "-fx-border-color: #606060;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 10 6 10;" +
                        "-fx-font-size: 14px;");
            } else {
                setupStyle();
            }
        });
    }
}

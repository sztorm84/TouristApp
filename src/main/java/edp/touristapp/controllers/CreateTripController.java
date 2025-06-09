package edp.touristapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateView {

    @FXML
    private TextField tripNameField;
    private String tripName;
    private Stage dialogStage;
    private boolean okClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public String getTripName() {
        return tripName;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        tripName = tripNameField.getText();
        if (!tripName.isBlank()) {
            okClicked = true;
            dialogStage.close();
        }
    }
}

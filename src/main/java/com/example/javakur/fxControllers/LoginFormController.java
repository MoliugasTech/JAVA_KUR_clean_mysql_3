package com.example.javakur.fxControllers;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.gui.FoodDeliveryApplication;
import com.example.javakur.gui.GuiContext;
import com.example.javakur.model.UserRole;
import com.example.javakur.security.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginFormController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        errorLabel.setText("");
        try {
            UserSession session = GuiContext.authenticationService()
                    .login(usernameField.getText(), passwordField.getText());
            if (session.role() != UserRole.ADMIN && session.role() != UserRole.RESTAURANT_OWNER) {
                throw new ValidationException(
                        "Klientams ir vairuotojams skirta mobilioji programėlė");
            }
            FoodDeliveryApplication.getInstance().showMain(session);
        } catch (ValidationException exception) {
            errorLabel.setText(exception.getMessage());
            passwordField.clear();
        }
    }
}

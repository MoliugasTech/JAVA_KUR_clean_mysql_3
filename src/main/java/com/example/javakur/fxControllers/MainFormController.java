package com.example.javakur.fxControllers;

import com.example.javakur.gui.FoodDeliveryApplication;
import com.example.javakur.gui.GuiSession;
import com.example.javakur.model.UserRole;
import com.example.javakur.security.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainFormController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Button userManagementButton;

    @FXML
    private void initialize() {
        UserSession session = GuiSession.requireCurrent();
        usernameLabel.setText("Sveiki, " + session.username());
        roleLabel.setText("Rolė: " + roleName(session.role()));
        boolean admin = session.role() == UserRole.ADMIN;
        userManagementButton.setVisible(admin);
        userManagementButton.setManaged(admin);
    }

    @FXML
    private void handleUsers() {
        FoodDeliveryApplication.getInstance().showUserManagement();
    }

    @FXML
    private void handleLogout() {
        FoodDeliveryApplication.getInstance().showLogin();
    }

    private String roleName(UserRole role) {
        return role == UserRole.ADMIN ? "Administratorius" : "Restorano savininkas";
    }
}

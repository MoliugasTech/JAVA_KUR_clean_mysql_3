package com.example.javakur.gui;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.util.HibernateUtil;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FoodDeliveryApplication extends Application {
    private final AuthenticationService authenticationService =
            new AuthenticationService(new UserRepository());
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Maisto užsakymų sistema");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        showLogin();
        stage.show();
    }

    private void showLogin() {
        Label title = new Label("Sveiki sugrįžę");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Prisijunkite prie maisto užsakymų sistemos");
        subtitle.getStyleClass().add("subtitle");
        TextField username = new TextField();
        username.setPromptText("Vartotojo vardas");
        PasswordField password = new PasswordField();
        password.setPromptText("Slaptažodis");
        Label error = new Label();
        error.getStyleClass().add("error");
        Button login = new Button("Prisijungti");
        login.setDefaultButton(true);
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> {
            try {
                showDashboard(authenticationService.login(username.getText(), password.getText()));
            } catch (ValidationException exception) {
                error.setText(exception.getMessage());
            }
        });

        VBox card = new VBox(14, title, subtitle, username, password, error, login);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(380);
        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32));
        setScene(root);
    }

    private void showDashboard(UserSession session) {
        Label brand = new Label("FOOD DELIVERY");
        brand.getStyleClass().add("brand");
        Label welcome = new Label("Sveiki, " + session.username());
        welcome.getStyleClass().add("title");
        Label role = new Label("Rolė: " + roleName(session));
        role.getStyleClass().add("subtitle");
        Label checkpoint = new Label(
                "GUI karkasas paruoštas. Rolės funkcijos bus jungiamos kitame etape.");
        Button logout = new Button("Atsijungti");
        logout.setOnAction(event -> showLogin());

        VBox sidebar = new VBox(24, brand, logout);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(230);
        VBox content = new VBox(12, welcome, role, checkpoint);
        content.setPadding(new Insets(48));
        BorderPane root = new BorderPane(content, null, null, null, sidebar);
        setScene(root);
    }

    private String roleName(UserSession session) {
        return switch (session.role()) {
            case CLIENT -> "Klientas";
            case RESTAURANT_OWNER -> "Restorano savininkas";
            case DRIVER -> "Vairuotojas";
            case ADMIN -> "Administratorius";
        };
    }

    private void setScene(javafx.scene.Parent root) {
        Scene scene = new Scene(root, 1000, 680);
        scene.getStylesheets().add(
                FoodDeliveryApplication.class.getResource("/gui/application.css").toExternalForm());
        stage.setScene(scene);
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

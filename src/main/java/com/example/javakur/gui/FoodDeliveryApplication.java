package com.example.javakur.gui;

import com.example.javakur.security.UserSession;
import com.example.javakur.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FoodDeliveryApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoodDeliveryApplication.class);
    private static FoodDeliveryApplication instance;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        instance = this;
        primaryStage = stage;
        primaryStage.setTitle("Maisto užsakymų sistema");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        showLogin();
        primaryStage.show();
    }

    public static FoodDeliveryApplication getInstance() {
        return instance;
    }

    public void showLogin() {
        GuiSession.clear();
        setPrimaryScene("/gui/login-form.fxml", 1000, 680);
    }

    public void showMain(UserSession session) {
        GuiSession.set(session);
        setPrimaryScene("/gui/main-form.fxml", 1100, 720);
    }

    public void showUserManagement() {
        try {
            Stage dialog = new Stage();
            dialog.setTitle("Vartotojų valdymas");
            dialog.initOwner(primaryStage);
            dialog.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(load("/gui/user-form.fxml"), 1180, 760);
            addStylesheet(scene);
            dialog.setScene(scene);
            dialog.setMinWidth(980);
            dialog.setMinHeight(650);
            dialog.showAndWait();
        } catch (IOException exception) {
            LOGGER.error("Nepavyko atidaryti vartotojų formos", exception);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Klaida");
            alert.setHeaderText("Nepavyko atidaryti vartotojų valdymo");
            alert.setContentText("Išsamesnė informacija rodoma terminale.");
            alert.showAndWait();
        }
    }

    public void showOwnerOrders() {
        try {
            Stage dialog = new Stage();
            dialog.setTitle("Restorano užsakymų valdymas");
            dialog.initOwner(primaryStage);
            dialog.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(load("/gui/owner-orders-form.fxml"), 1280, 800);
            addStylesheet(scene);
            dialog.setScene(scene);
            dialog.setMinWidth(1100);
            dialog.setMinHeight(700);
            dialog.showAndWait();
        } catch (IOException exception) {
            LOGGER.error("Nepavyko atidaryti restorano užsakymų formos", exception);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Klaida");
            alert.setHeaderText("Nepavyko atidaryti užsakymų valdymo");
            alert.setContentText("Išsamesnė informacija rodoma terminale.");
            alert.showAndWait();
        }
    }

    public void showAdminSystem() {
        showDialog("Administratoriaus sistemos centras", "/gui/admin-system-form.fxml",
                1320, 820, 1120, 700);
    }

    public void showOwnerCatalog() {
        showDialog("Restoranų ir meniu valdymas", "/gui/owner-catalog-form.fxml",
                1260, 800, 1080, 680);
    }

    private void showDialog(String title, String resource, double width, double height,
                            double minWidth, double minHeight) {
        try {
            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.initOwner(primaryStage);
            dialog.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(load(resource), width, height);
            addStylesheet(scene);
            dialog.setScene(scene);
            dialog.setMinWidth(minWidth);
            dialog.setMinHeight(minHeight);
            dialog.showAndWait();
        } catch (IOException exception) {
            LOGGER.error("Nepavyko atidaryti formos {}", resource, exception);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Klaida");
            alert.setHeaderText("Nepavyko atidaryti lango");
            alert.setContentText("Išsamesnė informacija rodoma terminale.");
            alert.showAndWait();
        }
    }

    private void setPrimaryScene(String resource, double width, double height) {
        try {
            Scene scene = new Scene(load(resource), width, height);
            addStylesheet(scene);
            primaryStage.setScene(scene);
        } catch (IOException exception) {
            throw new IllegalStateException("Nepavyko užkrauti formos: " + resource, exception);
        }
    }

    private Parent load(String resource) throws IOException {
        return FXMLLoader.load(FoodDeliveryApplication.class.getResource(resource));
    }

    private void addStylesheet(Scene scene) {
        scene.getStylesheets().add(
                FoodDeliveryApplication.class.getResource("/gui/application.css").toExternalForm());
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

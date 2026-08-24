package com.example.javakur.fxControllers;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.gui.GuiContext;
import com.example.javakur.gui.GuiSession;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.User;
import com.example.javakur.model.UserRole;
import com.example.javakur.model.VehicleType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Locale;

public class UserFormController {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> idColumn;
    @FXML private TableColumn<User, UserRole> roleColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField emailField;
    @FXML private TextField deliveryAddressField;
    @FXML private ComboBox<VehicleType> vehicleTypeComboBox;
    @FXML private HBox deliveryAddressRow;
    @FXML private HBox vehicleTypeRow;
    @FXML private Label formTitleLabel;
    @FXML private Label passwordHintLabel;
    @FXML private Button activeButton;

    private final ObservableList<User> allUsers = FXCollections.observableArrayList();
    private User selectedUser;

    @FXML
    private void initialize() {
        if (GuiSession.requireCurrent().role() != UserRole.ADMIN) {
            throw new IllegalStateException("Vartotojų valdymas skirtas tik administratoriui");
        }
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        configureColumns();
        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        vehicleTypeComboBox.setItems(FXCollections.observableArrayList(VehicleType.values()));
        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateSpecificFields());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        usersTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> selectUser(newValue));
        startCreate();
        refreshUsers();
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        roleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getRole()));
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getUsername()));
        nameColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getName() + " " + cell.getValue().getSurname()));
        emailColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getEmail()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().isActive() ? "Aktyvus" : "Užblokuotas"));
    }

    @FXML
    private void handleNew() {
        usersTable.getSelectionModel().clearSelection();
        startCreate();
    }

    @FXML
    private void handleSave() {
        try {
            if (selectedUser == null) {
                createUser();
                showInformation("Vartotojas sukurtas");
            } else {
                GuiContext.userService().updateUser(
                        selectedUser.getId(), usernameField.getText(), passwordField.getText(),
                        nameField.getText(), surnameField.getText(), emailField.getText(),
                        deliveryAddressField.getText(), vehicleTypeComboBox.getValue());
                showInformation("Vartotojo duomenys atnaujinti");
            }
            refreshUsers();
            startCreate();
        } catch (RuntimeException exception) {
            showError(readableMessage(exception));
        }
    }

    @FXML
    private void handleToggleActive() {
        if (selectedUser == null) {
            showError("Pirmiausia pasirinkite vartotoją lentelėje");
            return;
        }
        try {
            boolean activate = !selectedUser.isActive();
            GuiContext.adminService().setUserActive(
                    GuiSession.requireCurrent().userId(), selectedUser.getId(), activate);
            showInformation(activate ? "Vartotojas atblokuotas" : "Vartotojas užblokuotas");
            refreshUsers();
            startCreate();
        } catch (RuntimeException exception) {
            showError(readableMessage(exception));
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) usersTable.getScene().getWindow()).close();
    }

    private void createUser() {
        UserRole role = roleComboBox.getValue();
        if (role == null) throw new ValidationException("Pasirinkite vartotojo rolę");
        String username = usernameField.getText();
        String password = passwordField.getText();
        String name = nameField.getText();
        String surname = surnameField.getText();
        String email = emailField.getText();
        switch (role) {
            case ADMIN -> GuiContext.userService().createAdmin(username, password, name, surname, email);
            case RESTAURANT_OWNER -> GuiContext.userService()
                    .createRestaurantOwner(username, password, name, surname, email);
            case CLIENT -> GuiContext.userService()
                    .createClient(username, password, name, surname, email, deliveryAddressField.getText());
            case DRIVER -> GuiContext.userService()
                    .createDriver(username, password, name, surname, email, vehicleTypeComboBox.getValue());
        }
    }

    protected void refreshUsers() {
        allUsers.setAll(GuiContext.adminService().listUsers(GuiSession.requireCurrent().userId()));
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        usersTable.setItems(allUsers.filtered(user -> query.isEmpty()
                || contains(user.getUsername(), query)
                || contains(user.getName(), query)
                || contains(user.getSurname(), query)
                || contains(user.getEmail(), query)));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private void selectUser(User user) {
        if (user == null) return;
        selectedUser = user;
        formTitleLabel.setText("Redaguoti vartotoją");
        passwordHintLabel.setText("Palikite tuščią, jei slaptažodžio nekeičiate");
        roleComboBox.setValue(user.getRole());
        roleComboBox.setDisable(true);
        usernameField.setText(user.getUsername());
        passwordField.clear();
        nameField.setText(user.getName());
        surnameField.setText(user.getSurname());
        emailField.setText(user.getEmail());
        deliveryAddressField.setText(user instanceof Client client ? client.getDeliveryAddress() : "");
        vehicleTypeComboBox.setValue(user instanceof Driver driver ? driver.getVehicleType() : null);
        activeButton.setDisable(user.getId().equals(GuiSession.requireCurrent().userId()));
        activeButton.setText(user.isActive() ? "Blokuoti" : "Atblokuoti");
        updateSpecificFields();
    }

    private void startCreate() {
        selectedUser = null;
        formTitleLabel.setText("Naujas vartotojas");
        passwordHintLabel.setText("Bent 8 simboliai");
        roleComboBox.setDisable(false);
        roleComboBox.setValue(UserRole.CLIENT);
        usernameField.clear();
        passwordField.clear();
        nameField.clear();
        surnameField.clear();
        emailField.clear();
        deliveryAddressField.clear();
        vehicleTypeComboBox.setValue(VehicleType.CAR);
        activeButton.setText("Blokuoti");
        activeButton.setDisable(true);
        updateSpecificFields();
    }

    private void updateSpecificFields() {
        boolean client = roleComboBox.getValue() == UserRole.CLIENT;
        boolean driver = roleComboBox.getValue() == UserRole.DRIVER;
        deliveryAddressRow.setVisible(client);
        deliveryAddressRow.setManaged(client);
        vehicleTypeRow.setVisible(driver);
        vehicleTypeRow.setManaged(driver);
    }

    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Veiksmas atliktas");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Klaida");
        alert.setHeaderText("Nepavyko atlikti veiksmo");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String readableMessage(RuntimeException exception) {
        if (exception.getMessage() != null && !exception.getMessage().isBlank()
                && !exception.getMessage().startsWith("Failed to")) {
            return exception.getMessage();
        }
        return "Nepavyko išsaugoti duomenų. Patikrinkite įvestas reikšmes.";
    }
}

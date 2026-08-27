package com.example.javakur.fxControllers;

import com.example.javakur.gui.GuiContext;
import com.example.javakur.gui.GuiSession;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.service.OwnerConsoleService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class OwnerCatalogFormController {
    private final OwnerConsoleService ownerService = GuiContext.ownerService();

    @FXML private TableView<Restaurant> restaurantsTable;
    @FXML private TableColumn<Restaurant, Long> restaurantIdColumn;
    @FXML private TableColumn<Restaurant, String> restaurantNameColumn;
    @FXML private TableColumn<Restaurant, RestaurantType> restaurantTypeColumn;
    @FXML private TableColumn<Restaurant, String> restaurantHoursColumn;
    @FXML private TextField restaurantNameField;
    @FXML private TextArea restaurantDescriptionField;
    @FXML private TextField restaurantAddressField;
    @FXML private ComboBox<RestaurantType> restaurantTypeCombo;
    @FXML private TextField openingTimeField;
    @FXML private TextField closingTimeField;
    @FXML private Label restaurantFormTitle;

    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, Long> menuIdColumn;
    @FXML private TableColumn<MenuItem, String> menuNameColumn;
    @FXML private TableColumn<MenuItem, BigDecimal> menuPriceColumn;
    @FXML private TableColumn<MenuItem, String> menuAvailabilityColumn;
    @FXML private TextField menuNameField;
    @FXML private TextArea menuDescriptionField;
    @FXML private TextField menuPriceField;
    @FXML private CheckBox menuAvailableCheck;
    @FXML private Label menuFormTitle;
    @FXML private Label statusLabel;

    private Restaurant selectedRestaurant;
    private MenuItem selectedMenuItem;

    @FXML
    private void initialize() {
        configureTables();
        restaurantTypeCombo.getItems().setAll(RestaurantType.values());
        restaurantsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, value) -> selectRestaurant(value));
        menuTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, value) -> selectMenuItem(value));
        startNewRestaurant();
        startNewMenuItem();
        refreshRestaurants();
    }

    private void configureTables() {
        restaurantIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        restaurantNameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));
        restaurantTypeColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getType()));
        restaurantHoursColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getOpeningTime() + "–" + data.getValue().getClosingTime()));
        menuIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        menuNameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));
        menuPriceColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
        menuAvailabilityColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isAvailable() ? "Parduodamas" : "Paslėptas"));
    }

    protected void refreshRestaurants() {
        try {
            Long selectedId = selectedRestaurant == null ? null : selectedRestaurant.getId();
            List<Restaurant> restaurants = ownerService.listRestaurants(ownerId());
            restaurantsTable.getItems().setAll(restaurants);
            if (selectedId != null) {
                restaurants.stream().filter(value -> selectedId.equals(value.getId()))
                        .findFirst().ifPresent(restaurantsTable.getSelectionModel()::select);
            }
            if (restaurantsTable.getSelectionModel().getSelectedItem() == null
                    && !restaurants.isEmpty()) {
                restaurantsTable.getSelectionModel().selectFirst();
            }
            showStatus("Restoranų: " + restaurants.size());
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void refreshMenu() {
        if (selectedRestaurant == null) {
            menuTable.getItems().clear();
            return;
        }
        try {
            Long selectedId = selectedMenuItem == null ? null : selectedMenuItem.getId();
            List<MenuItem> items = ownerService.listMenu(
                    ownerId(), selectedRestaurant.getId());
            menuTable.getItems().setAll(items);
            if (selectedId != null) {
                items.stream().filter(value -> selectedId.equals(value.getId()))
                        .findFirst().ifPresent(menuTable.getSelectionModel()::select);
            }
            showStatus("Patiekalų pasirinktame restorane: " + items.size());
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void selectRestaurant(Restaurant restaurant) {
        selectedRestaurant = restaurant;
        selectedMenuItem = null;
        if (restaurant == null) {
            startNewRestaurant();
            menuTable.getItems().clear();
            return;
        }
        restaurantFormTitle.setText("Redaguoti restoraną #" + restaurant.getId());
        restaurantNameField.setText(restaurant.getName());
        restaurantDescriptionField.setText(restaurant.getDescription());
        restaurantAddressField.setText(restaurant.getAddress());
        restaurantTypeCombo.setValue(restaurant.getType());
        openingTimeField.setText(restaurant.getOpeningTime().toString());
        closingTimeField.setText(restaurant.getClosingTime().toString());
        startNewMenuItem();
        refreshMenu();
    }

    private void selectMenuItem(MenuItem item) {
        selectedMenuItem = item;
        if (item == null) {
            startNewMenuItem();
            return;
        }
        menuFormTitle.setText("Redaguoti patiekalą #" + item.getId());
        menuNameField.setText(item.getName());
        menuDescriptionField.setText(item.getDescription());
        menuPriceField.setText(item.getPrice().toPlainString());
        menuAvailableCheck.setSelected(item.isAvailable());
    }

    @FXML
    private void handleNewRestaurant() {
        restaurantsTable.getSelectionModel().clearSelection();
        startNewRestaurant();
    }

    @FXML
    private void handleSaveRestaurant() {
        try {
            LocalTime opening = parseTime(openingTimeField.getText());
            LocalTime closing = parseTime(closingTimeField.getText());
            if (selectedRestaurant == null) {
                selectedRestaurant = ownerService.createRestaurant(ownerId(),
                        restaurantNameField.getText(), restaurantDescriptionField.getText(),
                        restaurantAddressField.getText(), restaurantTypeCombo.getValue(),
                        opening, closing);
                showStatus("Restoranas sukurtas.");
            } else {
                selectedRestaurant = ownerService.updateRestaurant(ownerId(),
                        selectedRestaurant.getId(), restaurantNameField.getText(),
                        restaurantDescriptionField.getText(), restaurantAddressField.getText(),
                        restaurantTypeCombo.getValue(), opening, closing);
                showStatus("Restorano duomenys išsaugoti.");
            }
            refreshRestaurants();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void handleNewMenuItem() {
        menuTable.getSelectionModel().clearSelection();
        startNewMenuItem();
    }

    @FXML
    private void handleSaveMenuItem() {
        if (selectedRestaurant == null) {
            showStatus("Pirmiausia pasirinkite arba sukurkite restoraną.");
            return;
        }
        try {
            BigDecimal price = new BigDecimal(menuPriceField.getText().trim());
            if (selectedMenuItem == null) {
                selectedMenuItem = ownerService.addMenuItem(ownerId(),
                        selectedRestaurant.getId(), menuNameField.getText(),
                        menuDescriptionField.getText(), price);
                showStatus("Patiekalas sukurtas.");
            } else {
                selectedMenuItem = ownerService.updateMenuItem(ownerId(),
                        selectedRestaurant.getId(), selectedMenuItem.getId(),
                        menuNameField.getText(), menuDescriptionField.getText(),
                        price, menuAvailableCheck.isSelected());
                showStatus("Patiekalo duomenys išsaugoti.");
            }
            refreshMenu();
        } catch (NumberFormatException exception) {
            showStatus("Kaina turi būti skaičius, pvz. 8.50.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void handleDeleteMenuItem() {
        if (selectedRestaurant == null || selectedMenuItem == null) {
            showStatus("Pasirinkite šalinamą patiekalą.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Pašalinti patiekalą");
        alert.setHeaderText(selectedMenuItem.getName());
        alert.setContentText("Istoriniuose užsakymuose naudojamo patiekalo pašalinti negalima; "
                + "tokį patiekalą geriau paslėpti.");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            ownerService.deleteMenuItem(ownerId(), selectedRestaurant.getId(),
                    selectedMenuItem.getId());
            selectedMenuItem = null;
            startNewMenuItem();
            refreshMenu();
            showStatus("Patiekalas pašalintas.");
        } catch (RuntimeException exception) {
            showStatus("Patiekalo pašalinti nepavyko. Jei jis yra užsakymų istorijoje, "
                    + "nuimkite žymą „Parduodamas“.");
        }
    }

    private void startNewRestaurant() {
        selectedRestaurant = null;
        restaurantFormTitle.setText("Naujas restoranas");
        restaurantNameField.clear();
        restaurantDescriptionField.clear();
        restaurantAddressField.clear();
        restaurantTypeCombo.setValue(RestaurantType.OTHER);
        openingTimeField.setText("09:00");
        closingTimeField.setText("22:00");
    }

    private void startNewMenuItem() {
        selectedMenuItem = null;
        menuFormTitle.setText("Naujas patiekalas");
        menuNameField.clear();
        menuDescriptionField.clear();
        menuPriceField.clear();
        menuAvailableCheck.setSelected(true);
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value == null ? "" : value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Laikas turi būti HH:mm formato, pvz. 09:00");
        }
    }

    private Long ownerId() {
        return GuiSession.requireCurrent().userId();
    }

    private void showError(RuntimeException exception) {
        String message = exception.getMessage();
        showStatus(message == null || message.isBlank()
                ? "Veiksmo atlikti nepavyko." : message);
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
    }
}

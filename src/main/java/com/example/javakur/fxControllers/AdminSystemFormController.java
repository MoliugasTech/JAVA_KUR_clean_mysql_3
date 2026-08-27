package com.example.javakur.fxControllers;

import com.example.javakur.gui.GuiContext;
import com.example.javakur.gui.GuiSession;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.Review;
import com.example.javakur.service.AdminConsoleService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminSystemFormController {
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final AdminConsoleService adminService = GuiContext.adminService();

    @FXML private ComboBox<Restaurant> orderRestaurantFilter;
    @FXML private ComboBox<OrderStatus> orderStatusFilter;
    @FXML private DatePicker orderFromDate;
    @FXML private DatePicker orderToDate;
    @FXML private TextField orderClientFilter;
    @FXML private TableView<FoodOrder> ordersTable;
    @FXML private TableColumn<FoodOrder, Long> orderIdColumn;
    @FXML private TableColumn<FoodOrder, OrderStatus> orderStatusColumn;
    @FXML private TableColumn<FoodOrder, String> orderRestaurantColumn;
    @FXML private TableColumn<FoodOrder, String> orderClientColumn;
    @FXML private TableColumn<FoodOrder, String> orderDriverColumn;
    @FXML private TableColumn<FoodOrder, String> orderCreatedColumn;
    @FXML private TableColumn<FoodOrder, BigDecimal> orderTotalColumn;

    @FXML private ComboBox<Restaurant> catalogRestaurantCombo;
    @FXML private TableView<MenuItem> adminMenuTable;
    @FXML private TableColumn<MenuItem, Long> menuIdColumn;
    @FXML private TableColumn<MenuItem, String> menuNameColumn;
    @FXML private TableColumn<MenuItem, BigDecimal> menuPriceColumn;
    @FXML private TableColumn<MenuItem, String> menuAvailableColumn;
    @FXML private TextField menuNameField;
    @FXML private TextArea menuDescriptionField;
    @FXML private TextField menuPriceField;
    @FXML private CheckBox menuAvailableCheck;
    @FXML private Label menuFormTitle;

    @FXML private TableView<OrderMessage> messagesTable;
    @FXML private TableColumn<OrderMessage, Long> messageIdColumn;
    @FXML private TableColumn<OrderMessage, Long> messageOrderColumn;
    @FXML private TableColumn<OrderMessage, String> messageSenderColumn;
    @FXML private TableColumn<OrderMessage, String> messageTimeColumn;
    @FXML private TableColumn<OrderMessage, String> messageContentColumn;
    @FXML private TextArea messageContentEditor;

    @FXML private TableView<Review> reviewsTable;
    @FXML private TableColumn<Review, Long> reviewIdColumn;
    @FXML private TableColumn<Review, Long> reviewOrderColumn;
    @FXML private TableColumn<Review, String> reviewAuthorColumn;
    @FXML private TableColumn<Review, String> reviewTypeColumn;
    @FXML private TableColumn<Review, Number> reviewRatingColumn;
    @FXML private TableColumn<Review, String> reviewCommentColumn;
    @FXML private Label statusLabel;

    private MenuItem selectedMenuItem;

    @FXML
    private void initialize() {
        configureTables();
        orderStatusFilter.getItems().setAll(OrderStatus.values());
        catalogRestaurantCombo.valueProperty().addListener(
                (observable, oldValue, value) -> refreshMenu());
        adminMenuTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, value) -> selectMenuItem(value));
        messagesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, value) -> messageContentEditor.setText(
                        value == null ? "" : value.getContent()));
        startNewMenuItem();
        refreshData();
    }

    private void configureTables() {
        orderIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        orderStatusColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));
        orderRestaurantColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getRestaurant().getName()));
        orderClientColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getClient().getUsername()));
        orderDriverColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getDriver() == null ? ""
                        : data.getValue().getDriver().getUsername()));
        orderCreatedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getCreatedAt() == null ? ""
                        : DATE_TIME.format(data.getValue().getCreatedAt())));
        orderTotalColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getTotalPrice()));

        menuIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        menuNameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));
        menuPriceColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
        menuAvailableColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isAvailable() ? "Taip" : "Ne"));

        messageIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        messageOrderColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getOrder().getId()));
        messageSenderColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getSender().getUsername()));
        messageTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getSentAt() == null ? ""
                        : DATE_TIME.format(data.getValue().getSentAt())));
        messageContentColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getContent()));

        reviewIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        reviewOrderColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getOrder().getId()));
        reviewAuthorColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getAuthor().getUsername()));
        reviewTypeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getType().name()));
        reviewRatingColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getRating()));
        reviewCommentColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getComment()));
    }

    @FXML
    protected void refreshData() {
        try {
            List<Restaurant> restaurants = adminService.listRestaurants(adminId());
            Restaurant catalogSelected = catalogRestaurantCombo.getValue();
            Long catalogId = catalogSelected == null ? null : catalogSelected.getId();
            orderRestaurantFilter.getItems().setAll(restaurants);
            catalogRestaurantCombo.getItems().setAll(restaurants);
            if (catalogId != null) {
                restaurants.stream().filter(value -> catalogId.equals(value.getId()))
                        .findFirst().ifPresent(catalogRestaurantCombo::setValue);
            }
            if (catalogRestaurantCombo.getValue() == null && !restaurants.isEmpty()) {
                catalogRestaurantCombo.getSelectionModel().selectFirst();
            }
            refreshOrders();
            refreshMenu();
            messagesTable.getItems().setAll(adminService.listMessages(adminId()));
            reviewsTable.getItems().setAll(adminService.listReviews(adminId()));
            showStatus("Sistemos duomenys atnaujinti.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void refreshOrders() {
        try {
            Restaurant restaurant = orderRestaurantFilter.getValue();
            ordersTable.getItems().setAll(adminService.listOrders(adminId(),
                    orderStatusFilter.getValue(),
                    restaurant == null ? null : restaurant.getId(),
                    orderFromDate.getValue(), orderToDate.getValue(),
                    orderClientFilter.getText()));
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void clearOrderFilters() {
        orderRestaurantFilter.getSelectionModel().clearSelection();
        orderStatusFilter.getSelectionModel().clearSelection();
        orderFromDate.setValue(null);
        orderToDate.setValue(null);
        orderClientFilter.clear();
        refreshOrders();
    }

    @FXML
    private void deleteSelectedOrder() {
        FoodOrder order = ordersTable.getSelectionModel().getSelectedItem();
        if (order == null) {
            showStatus("Pasirinkite užsakymą.");
            return;
        }
        if (!confirm("Pašalinti užsakymą #" + order.getId(),
                "Kartu bus pašalintos jo žinutės ir atsiliepimai.")) return;
        try {
            adminService.deleteTerminalOrder(adminId(), order.getId());
            refreshData();
            showStatus("Užsakymas pašalintas.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void refreshMenu() {
        Restaurant restaurant = catalogRestaurantCombo.getValue();
        if (restaurant == null) {
            adminMenuTable.getItems().clear();
            return;
        }
        try {
            adminMenuTable.getItems().setAll(
                    adminService.listMenu(adminId(), restaurant.getId()));
        } catch (RuntimeException exception) {
            showError(exception);
        }
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
    private void newMenuItem() {
        adminMenuTable.getSelectionModel().clearSelection();
        startNewMenuItem();
    }

    @FXML
    private void saveMenuItem() {
        Restaurant restaurant = catalogRestaurantCombo.getValue();
        if (restaurant == null) {
            showStatus("Pasirinkite restoraną.");
            return;
        }
        try {
            BigDecimal price = new BigDecimal(menuPriceField.getText().trim());
            if (selectedMenuItem == null) {
                adminService.createMenuItem(adminId(), restaurant.getId(),
                        menuNameField.getText(), menuDescriptionField.getText(), price);
                showStatus("Patiekalas sukurtas.");
            } else {
                adminService.updateMenuItem(adminId(), restaurant.getId(),
                        selectedMenuItem.getId(), menuNameField.getText(),
                        menuDescriptionField.getText(), price,
                        menuAvailableCheck.isSelected());
                showStatus("Patiekalas išsaugotas.");
            }
            selectedMenuItem = null;
            startNewMenuItem();
            refreshMenu();
        } catch (NumberFormatException exception) {
            showStatus("Kaina turi būti skaičius, pvz. 8.50.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void deleteMenuItem() {
        Restaurant restaurant = catalogRestaurantCombo.getValue();
        MenuItem item = adminMenuTable.getSelectionModel().getSelectedItem();
        if (restaurant == null || item == null) {
            showStatus("Pasirinkite patiekalą.");
            return;
        }
        if (!confirm("Pašalinti patiekalą", item.getName())) return;
        try {
            adminService.deleteMenuItem(adminId(), restaurant.getId(), item.getId());
            selectedMenuItem = null;
            startNewMenuItem();
            refreshMenu();
            showStatus("Patiekalas pašalintas.");
        } catch (RuntimeException exception) {
            showStatus("Patiekalo pašalinti nepavyko. Jei jis naudotas užsakyme, "
                    + "pažymėkite jį kaip neprieinamą.");
        }
    }

    @FXML
    private void saveMessage() {
        OrderMessage message = messagesTable.getSelectionModel().getSelectedItem();
        if (message == null) {
            showStatus("Pasirinkite žinutę.");
            return;
        }
        try {
            adminService.updateMessage(adminId(), message.getId(),
                    messageContentEditor.getText());
            refreshData();
            showStatus("Žinutė atnaujinta.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void deleteMessage() {
        OrderMessage message = messagesTable.getSelectionModel().getSelectedItem();
        if (message == null || !confirm("Pašalinti žinutę", message.getContent())) return;
        try {
            adminService.deleteMessage(adminId(), message.getId());
            refreshData();
            showStatus("Žinutė pašalinta.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void deleteReview() {
        Review review = reviewsTable.getSelectionModel().getSelectedItem();
        if (review == null || !confirm("Pašalinti atsiliepimą",
                review.getComment() == null ? "" : review.getComment())) return;
        try {
            adminService.deleteReview(adminId(), review.getId());
            refreshData();
            showStatus("Atsiliepimas pašalintas.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void startNewMenuItem() {
        selectedMenuItem = null;
        menuFormTitle.setText("Naujas patiekalas");
        menuNameField.clear();
        menuDescriptionField.clear();
        menuPriceField.clear();
        menuAvailableCheck.setSelected(true);
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Patvirtinimas");
        alert.setHeaderText(title);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private Long adminId() {
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

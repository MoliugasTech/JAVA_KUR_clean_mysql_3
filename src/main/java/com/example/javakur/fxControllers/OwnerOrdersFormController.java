package com.example.javakur.fxControllers;

import com.example.javakur.gui.GuiContext;
import com.example.javakur.gui.GuiSession;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.service.OwnerConsoleService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class OwnerOrdersFormController {
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final EnumSet<OrderStatus> RESTAURANT_EDITABLE = EnumSet.of(
            OrderStatus.CREATED, OrderStatus.CONFIRMED, OrderStatus.PREPARING);
    private static final EnumSet<OrderStatus> DRIVER_POLICY_EDITABLE = EnumSet.of(
            OrderStatus.CREATED, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP);
    private final OwnerConsoleService ownerService = GuiContext.ownerService();

    @FXML private ComboBox<Restaurant> restaurantCombo;
    @FXML private ComboBox<OrderStatus> statusFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TextField clientFilter;
    @FXML private TableView<FoodOrder> ordersTable;
    @FXML private TableColumn<FoodOrder, Long> orderIdColumn;
    @FXML private TableColumn<FoodOrder, OrderStatus> orderStatusColumn;
    @FXML private TableColumn<FoodOrder, String> clientColumn;
    @FXML private TableColumn<FoodOrder, String> createdColumn;
    @FXML private TableColumn<FoodOrder, BigDecimal> totalColumn;
    @FXML private TableColumn<FoodOrder, String> driverPolicyColumn;
    @FXML private TableView<OrderItem> itemsTable;
    @FXML private TableColumn<OrderItem, String> itemNameColumn;
    @FXML private TableColumn<OrderItem, Integer> itemQuantityColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> itemPriceColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> itemTotalColumn;
    @FXML private TableView<OrderMessage> messagesTable;
    @FXML private TableColumn<OrderMessage, String> messageSenderColumn;
    @FXML private TableColumn<OrderMessage, String> messageTimeColumn;
    @FXML private TableColumn<OrderMessage, String> messageContentColumn;
    @FXML private TextField messageField;
    @FXML private Button sendMessageButton;
    @FXML private Label orderInfoLabel;
    @FXML private Label cancellationReasonLabel;
    @FXML private Label statusLabel;
    @FXML private Button acceptButton;
    @FXML private Button prepareButton;
    @FXML private Button readyButton;
    @FXML private Button rejectButton;
    @FXML private Button removeItemButton;
    @FXML private Button toggleDriverButton;

    private FoodOrder currentOrder;

    @FXML
    private void initialize() {
        configureTables();
        statusFilter.getItems().setAll(OrderStatus.values());
        restaurantCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshOrders());
        ordersTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> loadSelectedOrder(newValue));
        itemsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> updateActions());
        refreshRestaurants();
        updateActions();
    }

    private void configureTables() {
        orderIdColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        orderStatusColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));
        clientColumn.setCellValueFactory(data -> {
            var client = data.getValue().getClient();
            String text = client == null ? "" : client.getUsername() + " — "
                    + client.getName() + " " + client.getSurname();
            return new ReadOnlyStringWrapper(text);
        });
        createdColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getCreatedAt() == null ? ""
                        : DATE_TIME.format(data.getValue().getCreatedAt())));
        totalColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getTotalPrice()));
        driverPolicyColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isDriverAssignmentAllowed() ? "Leidžiama" : "Užblokuota"));

        itemNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getMenuItem() == null ? ""
                        : data.getValue().getMenuItem().getName()));
        itemQuantityColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        itemPriceColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPriceAtPurchase()));
        itemTotalColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                data.getValue().getPriceAtPurchase().multiply(
                        BigDecimal.valueOf(data.getValue().getQuantity()))));
        messageSenderColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getSender().getUsername()));
        messageTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getSentAt() == null ? ""
                        : DATE_TIME.format(data.getValue().getSentAt())));
        messageContentColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getContent()));
    }

    protected void refreshRestaurants() {
        try {
            List<Restaurant> restaurants = ownerService.listRestaurants(ownerId());
            restaurantCombo.getItems().setAll(restaurants);
            if (!restaurants.isEmpty()) {
                restaurantCombo.getSelectionModel().selectFirst();
            } else {
                showStatus("Šis savininkas neturi restoranų.");
            }
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    protected void refreshOrders() {
        Restaurant restaurant = restaurantCombo.getValue();
        if (restaurant == null) {
            ordersTable.getItems().clear();
            clearDetail();
            return;
        }
        try {
            Long selectedId = currentOrder == null ? null : currentOrder.getId();
            List<FoodOrder> orders = ownerService.listOrders(ownerId(), restaurant.getId(),
                    statusFilter.getValue(), fromDatePicker.getValue(),
                    toDatePicker.getValue(), clientFilter.getText());
            ordersTable.getItems().setAll(orders);
            if (selectedId != null) {
                orders.stream().filter(order -> selectedId.equals(order.getId()))
                        .findFirst().ifPresent(order ->
                                ordersTable.getSelectionModel().select(order));
            }
            if (ordersTable.getSelectionModel().getSelectedItem() == null) {
                clearDetail();
            }
            showStatus("Rasta užsakymų: " + orders.size());
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    @FXML
    private void handleApplyFilters() {
        refreshOrders();
    }

    @FXML
    private void handleClearFilters() {
        statusFilter.getSelectionModel().clearSelection();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        clientFilter.clear();
        refreshOrders();
    }

    @FXML
    private void handleRefresh() {
        refreshOrders();
    }

    @FXML
    private void handleAccept() {
        changeStatus(OrderStatus.CONFIRMED, "Užsakymas priimtas.");
    }

    @FXML
    private void handlePrepare() {
        changeStatus(OrderStatus.PREPARING, "Pradėtas užsakymo ruošimas.");
    }

    @FXML
    private void handleReady() {
        changeStatus(OrderStatus.READY_FOR_PICKUP, "Užsakymas paruoštas atsiėmimui.");
    }

    private void changeStatus(OrderStatus status, String message) {
        runOrderAction(() -> ownerService.changeOrderStatus(
                ownerId(), requireCurrentOrder().getId(), status), message);
    }

    @FXML
    private void handleReject() {
        FoodOrder order = requireCurrentOrder();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Atmesti užsakymą");
        dialog.setHeaderText("Užsakymas #" + order.getId());
        dialog.setContentText("Atmetimo priežastis:");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isEmpty()) return;
        runOrderAction(() -> ownerService.cancelOrder(
                ownerId(), order.getId(), answer.get()), "Užsakymas atmestas.");
    }

    @FXML
    private void handleRemoveItem() {
        FoodOrder order = requireCurrentOrder();
        OrderItem item = itemsTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            showStatus("Pasirinkite šalinamą patiekalą.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Pašalinti patiekalą");
        confirmation.setHeaderText(item.getMenuItem().getName());
        confirmation.setContentText("Patiekalas bus pašalintas, o užsakymo suma perskaičiuota.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        runOrderAction(() -> ownerService.removeOrderItem(
                ownerId(), order.getId(), item.getId()), "Patiekalas pašalintas.");
    }

    @FXML
    private void handleSendMessage() {
        if (currentOrder == null) {
            showStatus("Pasirinkite užsakymą.");
            return;
        }
        try {
            ownerService.sendMessage(ownerId(), currentOrder.getId(), messageField.getText());
            messageField.clear();
            refreshMessages();
            showStatus("Žinutė išsiųsta.");
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void refreshMessages() {
        if (currentOrder == null) {
            messagesTable.getItems().clear();
            return;
        }
        messagesTable.getItems().setAll(
                ownerService.listMessages(ownerId(), currentOrder.getId()));
    }

    @FXML
    private void handleToggleDriver() {
        FoodOrder order = requireCurrentOrder();
        boolean allowed = !order.isDriverAssignmentAllowed();
        runOrderAction(() -> ownerService.setDriverAssignmentAllowed(
                ownerId(), order.getId(), allowed),
                allowed ? "Vairuotojams vėl leidžiama paimti užsakymą."
                        : "Vairuotojams užsakymo paėmimas užblokuotas.");
    }

    private void runOrderAction(OrderAction action, String successMessage) {
        try {
            FoodOrder changed = action.run();
            Long id = changed.getId();
            refreshOrders();
            ordersTable.getItems().stream().filter(order -> id.equals(order.getId()))
                    .findFirst().ifPresent(order -> {
                        ordersTable.getSelectionModel().select(order);
                        loadSelectedOrder(order);
                    });
            showStatus(successMessage);
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void loadSelectedOrder(FoodOrder selected) {
        if (selected == null) {
            clearDetail();
            return;
        }
        try {
            currentOrder = ownerService.getOrder(ownerId(), selected.getId());
            itemsTable.getItems().setAll(currentOrder.getItems());
            String driver = currentOrder.getDriver() == null ? "nepriskirtas"
                    : currentOrder.getDriver().getName() + " "
                    + currentOrder.getDriver().getSurname();
            orderInfoLabel.setText("Užsakymas #" + currentOrder.getId()
                    + " · " + currentOrder.getStatus()
                    + " · vairuotojas: " + driver
                    + " · suma: " + currentOrder.getTotalPrice() + " €");
            String reason = currentOrder.getCancellationReason();
            cancellationReasonLabel.setText(reason == null || reason.isBlank()
                    ? "" : "Atmetimo priežastis: " + reason);
            refreshMessages();
            updateActions();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void clearDetail() {
        currentOrder = null;
        itemsTable.getItems().clear();
        messagesTable.getItems().clear();
        messageField.clear();
        orderInfoLabel.setText("Pasirinkite užsakymą.");
        cancellationReasonLabel.setText("");
        updateActions();
    }

    private void updateActions() {
        OrderStatus status = currentOrder == null ? null : currentOrder.getStatus();
        boolean hasOrder = currentOrder != null;
        acceptButton.setDisable(status != OrderStatus.CREATED);
        prepareButton.setDisable(status != OrderStatus.CONFIRMED);
        readyButton.setDisable(status != OrderStatus.PREPARING);
        rejectButton.setDisable(!hasOrder || !RESTAURANT_EDITABLE.contains(status));
        removeItemButton.setDisable(!hasOrder || !RESTAURANT_EDITABLE.contains(status)
                || currentOrder.getItems().size() <= 1
                || itemsTable.getSelectionModel().getSelectedItem() == null);
        boolean driverPolicyAllowed = hasOrder
                && DRIVER_POLICY_EDITABLE.contains(status)
                && currentOrder.getDriver() == null;
        toggleDriverButton.setDisable(!driverPolicyAllowed);
        toggleDriverButton.setText(hasOrder && currentOrder.isDriverAssignmentAllowed()
                ? "Blokuoti vairuotojo paėmimą" : "Leisti vairuotojo paėmimą");
        boolean messagingAllowed = hasOrder
                && status != OrderStatus.COMPLETED
                && status != OrderStatus.CANCELLED;
        messageField.setDisable(!messagingAllowed);
        sendMessageButton.setDisable(!messagingAllowed);
        messageField.setPromptText(messagingAllowed
                ? "Žinutė klientui arba vairuotojui"
                : "Baigto užsakymo pokalbis tik skaitomas");
    }

    private FoodOrder requireCurrentOrder() {
        if (currentOrder == null) {
            throw new IllegalStateException("Pasirinkite užsakymą");
        }
        return currentOrder;
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

    @FunctionalInterface
    private interface OrderAction {
        FoodOrder run();
    }
}

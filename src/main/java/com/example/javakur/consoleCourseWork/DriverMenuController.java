package com.example.javakur.consoleCourseWork;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.DriverConsoleService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

class DriverMenuController extends AbstractRoleMenuController {
    static final List<String> OPTIONS = List.of(
            "Pristatymui paruošti užsakymai", "Paimti užsakymą",
            "Mano aktyvūs pristatymai", "Keisti pristatymo būseną",
            "Užsakymų žinutės ir atsiliepimai");
    private final DriverConsoleService service;
    private final OrderInteractionMenu interactions;

    DriverMenuController(DriverConsoleService service, BufferedReader input, PrintStream output) {
        this(service, null, input, output);
    }

    DriverMenuController(DriverConsoleService service, OrderInteractionMenu interactions,
                         BufferedReader input, PrintStream output) {
        super(input, output);
        this.service = service;
        this.interactions = interactions;
    }
    public List<String> options() { return OPTIONS; }

    public void handle(UserSession session, int choice) {
        try {
            switch (choice) {
                case 1 -> showReady();
                case 2 -> pickUp(session.userId());
                case 3 -> showActive(session.userId());
                case 4 -> changeStatus(session.userId());
                case 5 -> interactions.open(session);
                default -> output.println("Neteisingas pasirinkimas.");
            }
        } catch (RuntimeException e) {
            output.println("Veiksmas nepavyko: " + e.getMessage());
        }
    }

    private void showReady() {
        List<FoodOrder> orders = service.listReadyOrders();
        if (orders.isEmpty()) output.println("Pristatymui paruoštų užsakymų nėra.");
        else printOrders(orders);
    }

    private void pickUp(Long driverId) {
        List<FoodOrder> orders = service.listReadyOrders();
        if (orders.isEmpty()) { output.println("Pristatymui paruoštų užsakymų nėra."); return; }
        printOrders(orders);
        FoodOrder order = select(orders, "Užsakymo numeris (0 - grįžti): ");
        if (order != null) output.println("Užsakymas paimtas. Būsena="
                + service.pickUpOrder(driverId, order.getId()).getStatus());
    }

    private void showActive(Long driverId) {
        List<FoodOrder> orders = service.listActiveDeliveries(driverId);
        if (orders.isEmpty()) output.println("Aktyvių pristatymų nėra.");
        else printOrders(orders);
    }

    private void changeStatus(Long driverId) {
        List<FoodOrder> orders = service.listActiveDeliveries(driverId);
        if (orders.isEmpty()) { output.println("Aktyvių pristatymų nėra."); return; }
        printOrders(orders);
        FoodOrder order = select(orders, "Užsakymo numeris (0 - grįžti): ");
        if (order == null) return;
        List<OrderStatus> statuses = List.of(OrderStatus.DELIVERING, OrderStatus.COMPLETED);
        for (int i = 0; i < statuses.size(); i++) output.println((i + 1) + " - " + statuses.get(i));
        OrderStatus status = statuses.get(Utils.readChoiceInRange(
                input, output, "Naujos būsenos numeris: ", 1, statuses.size()) - 1);
        output.println("Pristatymo būsena pakeista į "
                + service.changeDeliveryStatus(driverId, order.getId(), status).getStatus());
    }
}

package com.example.javakur.consoleCourseWork;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.ClientConsoleService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

class ClientMenuController extends AbstractRoleMenuController {
    static final List<String> OPTIONS = List.of(
            "Peržiūrėti restoranus ir meniu", "Sukurti užsakymą",
            "Mano užsakymai", "Užsakymo žinutės ir atsiliepimai");
    private final ClientConsoleService service;

    ClientMenuController(ClientConsoleService service, BufferedReader input, PrintStream output) {
        super(input, output);
        this.service = service;
    }

    public List<String> options() { return OPTIONS; }

    public void handle(UserSession session, int choice) {
        try {
            switch (choice) {
                case 1 -> browseRestaurants();
                case 2 -> createOrder(session.userId());
                case 3 -> showOrders(session.userId());
                case 4 -> output.println("Žinutės ir atsiliepimai bus prijungti M13 etape.");
                default -> output.println("Neteisingas pasirinkimas.");
            }
        } catch (ValidationException e) {
            output.println("Veiksmas nepavyko: " + e.getMessage());
        }
    }

    private void browseRestaurants() {
        List<Restaurant> restaurants = service.listRestaurants();
        if (restaurants.isEmpty()) { output.println("Restoranų nėra."); return; }
        printRestaurants(restaurants);
        Restaurant restaurant = select(restaurants, "Restorano numeris meniu peržiūrai (0 - grįžti): ");
        if (restaurant == null) return;
        List<MenuItem> items = service.listAvailableMenu(restaurant.getId());
        if (items.isEmpty()) output.println("Šis restoranas neturi prieinamų patiekalų.");
        else printMenu(items);
    }

    private void createOrder(Long clientId) {
        List<Restaurant> restaurants = service.listRestaurants();
        if (restaurants.isEmpty()) { output.println("Restoranų nėra."); return; }
        output.println("--- PASIRINKITE RESTORANĄ ---");
        printRestaurants(restaurants);
        Restaurant restaurant = select(restaurants, "Restorano numeris (0 - grįžti): ");
        if (restaurant == null) return;
        List<MenuItem> items = service.listAvailableMenu(restaurant.getId());
        if (items.isEmpty()) { output.println("Šis restoranas neturi prieinamų patiekalų."); return; }
        output.println("--- PASIRINKITE PATIEKALĄ ---");
        printMenu(items);
        MenuItem item = select(items, "Patiekalo numeris (0 - grįžti): ");
        if (item == null) return;
        int quantity = Utils.readPositiveInt(input, output, "Kiekis: ");
        output.println("Užsakymas: " + item.getName() + " x " + quantity);
        output.println("Numatoma suma: " + item.getPrice().multiply(BigDecimal.valueOf(quantity)) + " EUR");
        if (!Utils.readConfirmation(input, output, "Patvirtinti užsakymą? (taip/ne): ")) {
            output.println("Užsakymo kūrimas atšauktas."); return;
        }
        FoodOrder order = service.createSingleItemOrder(
                clientId, restaurant.getId(), item.getId(), quantity);
        output.println("Užsakymas sukurtas. ID=" + order.getId()
                + ", suma=" + order.getTotalPrice() + " EUR");
    }

    private void showOrders(Long clientId) {
        List<FoodOrder> orders = service.listClientOrders(clientId);
        if (orders.isEmpty()) { output.println("Užsakymų nėra."); return; }
        for (FoodOrder order : orders) {
            output.println(order.getId() + " | " + order.getRestaurant().getName()
                    + " | " + order.getStatus() + " | " + order.getTotalPrice() + " EUR");
        }
    }
}

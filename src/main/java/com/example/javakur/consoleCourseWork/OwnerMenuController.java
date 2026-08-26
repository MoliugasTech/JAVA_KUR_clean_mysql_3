package com.example.javakur.consoleCourseWork;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.OwnerConsoleService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

class OwnerMenuController extends AbstractRoleMenuController {
    static final List<String> OPTIONS = List.of(
            "Sukurti restoraną", "Mano restoranai", "Valdyti meniu",
            "Restorano užsakymai", "Keisti užsakymo būseną",
            "Užsakymų žinutės ir atsiliepimai");
    private final OwnerConsoleService service;
    private final OrderInteractionMenu interactions;

    OwnerMenuController(OwnerConsoleService service, BufferedReader input, PrintStream output) {
        this(service, null, input, output);
    }

    OwnerMenuController(OwnerConsoleService service, OrderInteractionMenu interactions,
                        BufferedReader input, PrintStream output) {
        super(input, output);
        this.service = service;
        this.interactions = interactions;
    }

    public List<String> options() { return OPTIONS; }

    public void handle(UserSession session, int choice) {
        try {
            switch (choice) {
                case 1 -> createRestaurant(session.userId());
                case 2 -> showRestaurants(session.userId());
                case 3 -> manageMenu(session.userId());
                case 4 -> showOrders(session.userId());
                case 5 -> changeStatus(session.userId());
                case 6 -> interactions.open(session);
                default -> output.println("Neteisingas pasirinkimas.");
            }
        } catch (RuntimeException e) {
            output.println("Veiksmas nepavyko: " + e.getMessage());
        }
    }

    private void createRestaurant(Long ownerId) {
        String name = Utils.readRequired(input, output, "Restorano pavadinimas: ");
        String description = Utils.readLine(input, output, "Aprašymas: ");
        String address = Utils.readRequired(input, output, "Adresas: ");
        List<RestaurantType> types = Arrays.asList(RestaurantType.values());
        for (int i = 0; i < types.size(); i++) output.println((i + 1) + " - " + types.get(i));
        RestaurantType type = types.get(Utils.readChoiceInRange(
                input, output, "Tipo numeris: ", 1, types.size()) - 1);
        Restaurant restaurant = service.createRestaurant(ownerId, name, description, address, type,
                Utils.readTime(input, output, "Atidarymo laikas (pvz., 9 arba 09:00): "),
                Utils.readTime(input, output, "Uždarymo laikas (pvz., 22 arba 22:00): "));
        output.println("Restoranas sukurtas: " + restaurant.getName());
    }

    private void showRestaurants(Long ownerId) {
        List<Restaurant> restaurants = service.listRestaurants(ownerId);
        if (restaurants.isEmpty()) output.println("Jūs dar neturite restoranų.");
        else printRestaurants(restaurants);
    }

    private void manageMenu(Long ownerId) {
        Restaurant restaurant = selectRestaurant(ownerId);
        if (restaurant == null) return;
        output.println("1 - Peržiūrėti meniu\n2 - Pridėti patiekalą\n3 - Keisti kainą arba prieinamumą\n0 - Grįžti");
        switch (Utils.readChoiceInRange(input, output, "Pasirinkimas: ", 0, 3)) {
            case 1 -> printMenu(service.listMenu(ownerId, restaurant.getId()));
            case 2 -> addItem(ownerId, restaurant);
            case 3 -> updateItem(ownerId, restaurant);
            default -> { }
        }
    }

    private void addItem(Long ownerId, Restaurant restaurant) {
        MenuItem item = service.addMenuItem(ownerId, restaurant.getId(),
                Utils.readRequired(input, output, "Patiekalo pavadinimas: "),
                Utils.readLine(input, output, "Aprašymas: "),
                Utils.readPositiveDecimal(input, output, "Kaina: "));
        output.println("Patiekalas pridėtas: " + item.getName());
    }

    private void updateItem(Long ownerId, Restaurant restaurant) {
        List<MenuItem> items = service.listMenu(ownerId, restaurant.getId());
        if (items.isEmpty()) { output.println("Meniu tuščias."); return; }
        printMenu(items);
        MenuItem item = select(items, "Patiekalo numeris (0 - grįžti): ");
        if (item == null) return;
        BigDecimal price = Utils.readOptionalPositiveDecimal(
                input, output, "Nauja kaina (Enter - nekeisti): ");
        Boolean available = Utils.readOptionalBoolean(
                input, output, "Prieinamas? (taip/ne, Enter - nekeisti): ");
        service.updateMenuItem(ownerId, restaurant.getId(), item.getId(), price, available);
        output.println("Patiekalas atnaujintas.");
    }

    private void showOrders(Long ownerId) {
        Restaurant restaurant = selectRestaurant(ownerId);
        if (restaurant == null) return;
        List<FoodOrder> orders = service.listOrders(ownerId, restaurant.getId());
        if (orders.isEmpty()) output.println("Šis restoranas užsakymų neturi.");
        else printOrders(orders);
    }

    private void changeStatus(Long ownerId) {
        Restaurant restaurant = selectRestaurant(ownerId);
        if (restaurant == null) return;
        List<FoodOrder> orders = service.listOrders(ownerId, restaurant.getId());
        if (orders.isEmpty()) { output.println("Šis restoranas užsakymų neturi."); return; }
        printOrders(orders);
        FoodOrder order = select(orders, "Užsakymo numeris (0 - grįžti): ");
        if (order == null) return;
        List<OrderStatus> statuses = List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED);
        for (int i = 0; i < statuses.size(); i++) output.println((i + 1) + " - " + statuses.get(i));
        OrderStatus status = statuses.get(Utils.readChoiceInRange(
                input, output, "Naujos būsenos numeris: ", 1, statuses.size()) - 1);
        FoodOrder updated;
        if (status == OrderStatus.CANCELLED) {
            String reason = Utils.readRequired(input, output, "Atmetimo priežastis: ");
            updated = service.cancelOrder(ownerId, order.getId(), reason);
        } else {
            updated = service.changeOrderStatus(ownerId, order.getId(), status);
        }
        output.println("Užsakymo būsena pakeista į " + updated.getStatus());
    }

    private Restaurant selectRestaurant(Long ownerId) {
        List<Restaurant> restaurants = service.listRestaurants(ownerId);
        if (restaurants.isEmpty()) { output.println("Jūs dar neturite restoranų."); return null; }
        printRestaurants(restaurants);
        return select(restaurants, "Restorano numeris (0 - grįžti): ");
    }
}

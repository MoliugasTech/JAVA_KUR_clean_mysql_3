package com.example.javakur.consoleCourseWork;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.ClientConsoleService;
import com.example.javakur.service.OrderCart;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

class ClientMenuController extends AbstractRoleMenuController {
    static final List<String> OPTIONS = List.of(
            "Peržiūrėti restoranus ir meniu", "Sukurti užsakymą",
            "Mano užsakymai", "Užsakymo žinutės ir atsiliepimai");
    private final ClientConsoleService service;
    private final OrderInteractionMenu interactions;

    ClientMenuController(ClientConsoleService service, BufferedReader input, PrintStream output) {
        this(service, null, input, output);
    }

    ClientMenuController(ClientConsoleService service, OrderInteractionMenu interactions,
                         BufferedReader input, PrintStream output) {
        super(input, output);
        this.service = service;
        this.interactions = interactions;
    }

    public List<String> options() { return OPTIONS; }

    public void handle(UserSession session, int choice) {
        try {
            switch (choice) {
                case 1 -> browseRestaurants();
                case 2 -> createOrder(session.userId());
                case 3 -> showOrders(session.userId());
                case 4 -> interactions.open(session);
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
        OrderCart cart = new OrderCart(restaurant);
        boolean editing = true;
        while (editing) {
            output.println("\n--- KREPŠELIS: " + restaurant.getName() + " ---");
            output.println("1 - Pridėti arba pakeisti patiekalo kiekį");
            output.println("2 - Pašalinti patiekalą");
            output.println("3 - Peržiūrėti krepšelį");
            output.println("4 - Patvirtinti užsakymą");
            output.println("0 - Atšaukti užsakymą");
            switch (Utils.readChoiceInRange(input, output, "Pasirinkimas: ", 0, 4)) {
                case 1 -> addOrUpdate(cart, items);
                case 2 -> removeItem(cart);
                case 3 -> printCart(cart);
                case 4 -> {
                    FoodOrder order = checkout(clientId, cart);
                    if (order != null) return;
                }
                default -> {
                    output.println("Užsakymo kūrimas atšauktas.");
                    editing = false;
                }
            }
        }
    }

    private void addOrUpdate(OrderCart cart, List<MenuItem> menu) {
        output.println("--- MENIU ---");
        printMenu(menu);
        MenuItem item = select(menu, "Patiekalo numeris (0 - grįžti): ");
        if (item == null) return;
        int quantity = Utils.readPositiveInt(input, output, "Kiekis: ");
        cart.setQuantity(item, quantity);
        output.println("Krepšelis atnaujintas: " + item.getName() + " x " + quantity);
    }

    private void removeItem(OrderCart cart) {
        if (cart.isEmpty()) {
            output.println("Krepšelis tuščias.");
            return;
        }
        List<MenuItem> cartItems = List.copyOf(cart.getItems().keySet());
        for (int i = 0; i < cartItems.size(); i++) {
            MenuItem item = cartItems.get(i);
            output.println((i + 1) + " - " + item.getName()
                    + " x " + cart.getItems().get(item));
        }
        MenuItem item = select(cartItems, "Šalinamo patiekalo numeris (0 - grįžti): ");
        if (item != null) {
            cart.remove(item);
            output.println("Patiekalas pašalintas: " + item.getName());
        }
    }

    private void printCart(OrderCart cart) {
        if (cart.isEmpty()) {
            output.println("Krepšelis tuščias.");
            return;
        }
        output.println("--- KREPŠELIO TURINYS ---");
        cart.getItems().forEach((item, quantity) -> output.println(
                item.getName() + " x " + quantity + " = "
                        + item.getPrice().multiply(BigDecimal.valueOf(quantity)) + " EUR"));
        output.println("Iš viso: " + cart.subtotal() + " EUR");
    }

    private FoodOrder checkout(Long clientId, OrderCart cart) {
        if (cart.isEmpty()) {
            output.println("Negalima patvirtinti tuščio krepšelio.");
            return null;
        }
        printCart(cart);
        if (!Utils.readConfirmation(input, output, "Patvirtinti užsakymą? (taip/ne): ")) {
            output.println("Galite toliau redaguoti krepšelį.");
            return null;
        }
        FoodOrder order = service.checkout(clientId, cart);
        output.println("Užsakymas sukurtas. ID=" + order.getId()
                + ", suma=" + order.getTotalPrice() + " EUR");
        return order;
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

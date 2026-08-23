package com.example.javakur.consoleCourseWork;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.UserRole;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.ClientConsoleService;
import com.example.javakur.service.OwnerConsoleService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

public class MenuControl {
    private final AuthenticationService authenticationService;
    private final ClientConsoleService clientConsoleService;
    private final OwnerConsoleService ownerConsoleService;
    private final BufferedReader input;
    private final PrintStream output;

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientConsoleService,
                       OwnerConsoleService ownerConsoleService,
                       BufferedReader input, PrintStream output) {
        this.authenticationService = authenticationService;
        this.clientConsoleService = clientConsoleService;
        this.ownerConsoleService = ownerConsoleService;
        this.input = input;
        this.output = output;
    }

    public MenuControl(AuthenticationService authenticationService,
                       BufferedReader input, PrintStream output) {
        this(authenticationService, null, null, input, output);
    }

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientConsoleService,
                       BufferedReader input, PrintStream output) {
        this(authenticationService, clientConsoleService, null, input, output);
    }

    public void run() {
        output.println("=== MAISTO UŽSAKYMŲ SISTEMA ===");
        boolean running = true;
        while (running) {
            output.println();
            output.println("1 - Prisijungti");
            output.println("2 - Registruotis kaip klientui");
            output.println("3 - Registruotis kaip restorano savininkui");
            output.println("0 - Baigti darbą");
            int choice = Utils.readChoice(input, output, "Pasirinkimas: ");
            switch (choice) {
                case 1 -> login();
                case 2 -> registerClient();
                case 3 -> registerOwner();
                case 0 -> running = false;
                default -> output.println("Neteisingas pasirinkimas.");
            }
        }
        output.println("Programa baigta.");
    }

    private void registerOwner() {
        output.println("--- RESTORANO SAVININKO REGISTRACIJA ---");
        String username = Utils.readLine(input, output, "Vartotojo vardas: ");
        String password = Utils.readLine(input, output, "Slaptažodis (bent 8 simboliai): ");
        String name = Utils.readLine(input, output, "Vardas: ");
        String surname = Utils.readLine(input, output, "Pavardė: ");
        String email = Utils.readLine(input, output, "El. paštas: ");
        try {
            ownerConsoleService.register(username, password, name, surname, email);
            output.println("Savininko registracija sėkminga. Dabar galite prisijungti.");
        } catch (ValidationException e) {
            output.println("Registracija nepavyko: " + e.getMessage());
        }
    }

    private void registerClient() {
        output.println("--- KLIENTO REGISTRACIJA ---");
        String username = Utils.readLine(input, output, "Vartotojo vardas: ");
        String password = Utils.readLine(input, output, "Slaptažodis (bent 8 simboliai): ");
        String name = Utils.readLine(input, output, "Vardas: ");
        String surname = Utils.readLine(input, output, "Pavardė: ");
        String email = Utils.readLine(input, output, "El. paštas: ");
        String address = Utils.readLine(input, output, "Pristatymo adresas: ");
        try {
            clientConsoleService.register(username, password, name, surname, email, address);
            output.println("Registracija sėkminga. Dabar galite prisijungti.");
        } catch (ValidationException e) {
            output.println("Registracija nepavyko: " + e.getMessage());
        }
    }

    private void login() {
        String username = Utils.readLine(input, output, "Vartotojo vardas: ");
        String password = Utils.readLine(input, output, "Slaptažodis: ");
        try {
            UserSession session = authenticationService.login(username, password);
            output.println("Prisijungta: " + session.username() + " [" + session.role() + "]");
            showRoleMenu(session);
        } catch (ValidationException e) {
            output.println("Prisijungti nepavyko: " + e.getMessage());
        }
    }

    private void showRoleMenu(UserSession session) {
        boolean loggedIn = true;
        while (loggedIn) {
            output.println();
            output.println("--- " + roleTitle(session.role()) + " ---");
            List<String> options = menuOptionsFor(session.role());
            for (int index = 0; index < options.size(); index++) {
                output.println((index + 1) + " - " + options.get(index));
            }
            output.println("0 - Atsijungti");

            int choice = Utils.readChoice(input, output, "Pasirinkimas: ");
            if (choice == 0) {
                loggedIn = false;
                output.println("Atsijungta.");
            } else if (choice > 0 && choice <= options.size()) {
                executeRoleChoice(session, choice, options.get(choice - 1));
            } else {
                output.println("Neteisingas pasirinkimas.");
            }
        }
    }

    private void executeRoleChoice(UserSession session, int choice, String label) {
        output.println("Pasirinkta: " + label);
        if (session.role() == UserRole.RESTAURANT_OWNER) {
            executeOwnerChoice(session, choice);
            return;
        }
        if (session.role() != UserRole.CLIENT) {
            output.println("Ši funkcija bus prijungta kitame etape.");
            return;
        }
        try {
            switch (choice) {
                case 1 -> browseRestaurants();
                case 2 -> createOrder(session.userId());
                case 3 -> showClientOrders(session.userId());
                case 4 -> output.println("Žinutės ir atsiliepimai bus prijungti prie meniu vėliau.");
                default -> output.println("Neteisingas pasirinkimas.");
            }
        } catch (ValidationException e) {
            output.println("Veiksmas nepavyko: " + e.getMessage());
        }
    }

    private void executeOwnerChoice(UserSession session, int choice) {
        try {
            switch (choice) {
                case 1 -> createRestaurant(session.userId());
                case 2 -> showOwnerRestaurants(session.userId());
                case 3 -> manageMenu(session.userId());
                case 4 -> showRestaurantOrders(session.userId());
                case 5 -> changeRestaurantOrderStatus(session.userId());
                default -> output.println("Neteisingas pasirinkimas.");
            }
        } catch (RuntimeException e) {
            output.println("Veiksmas nepavyko: " + e.getMessage());
        }
    }

    private void createRestaurant(Long ownerId) {
        String name = Utils.readLine(input, output, "Restorano pavadinimas: ");
        String description = Utils.readLine(input, output, "Aprašymas: ");
        String address = Utils.readLine(input, output, "Adresas: ");
        output.println("Tipai: " + java.util.Arrays.toString(RestaurantType.values()));
        RestaurantType type = RestaurantType.valueOf(
                Utils.readLine(input, output, "Tipas: ").toUpperCase());
        java.time.LocalTime opening = java.time.LocalTime.parse(
                Utils.readLine(input, output, "Atidarymo laikas (HH:mm): "));
        java.time.LocalTime closing = java.time.LocalTime.parse(
                Utils.readLine(input, output, "Uždarymo laikas (HH:mm): "));
        Restaurant restaurant = ownerConsoleService.createRestaurant(
                ownerId, name, description, address, type, opening, closing);
        output.println("Restoranas sukurtas: " + restaurant.getName());
    }

    private void showOwnerRestaurants(Long ownerId) {
        List<Restaurant> restaurants = ownerConsoleService.listRestaurants(ownerId);
        if (restaurants.isEmpty()) output.println("Jūs dar neturite restoranų.");
        else printRestaurants(restaurants);
    }

    private void manageMenu(Long ownerId) {
        Restaurant restaurant = selectOwnerRestaurant(ownerId);
        if (restaurant == null) return;
        output.println("1 - Peržiūrėti meniu");
        output.println("2 - Pridėti patiekalą");
        output.println("3 - Keisti kainą arba prieinamumą");
        output.println("0 - Grįžti");
        int action = Utils.readChoice(input, output, "Pasirinkimas: ");
        switch (action) {
            case 1 -> printMenu(ownerConsoleService.listMenu(ownerId, restaurant.getId()));
            case 2 -> addMenuItem(ownerId, restaurant);
            case 3 -> updateMenuItem(ownerId, restaurant);
            case 0 -> { }
            default -> output.println("Neteisingas pasirinkimas.");
        }
    }

    private void addMenuItem(Long ownerId, Restaurant restaurant) {
        String name = Utils.readLine(input, output, "Patiekalo pavadinimas: ");
        String description = Utils.readLine(input, output, "Aprašymas: ");
        java.math.BigDecimal price = new java.math.BigDecimal(
                Utils.readLine(input, output, "Kaina: "));
        MenuItem item = ownerConsoleService.addMenuItem(
                ownerId, restaurant.getId(), name, description, price);
        output.println("Patiekalas pridėtas: " + item.getName());
    }

    private void updateMenuItem(Long ownerId, Restaurant restaurant) {
        List<MenuItem> items = ownerConsoleService.listMenu(ownerId, restaurant.getId());
        if (items.isEmpty()) {
            output.println("Meniu tuščias.");
            return;
        }
        printMenu(items);
        MenuItem item = selectedItem(items,
                Utils.readChoice(input, output, "Patiekalo numeris (0 - grįžti): "));
        if (item == null) return;
        String priceText = Utils.readLine(input, output, "Nauja kaina (Enter - nekeisti): ");
        String availableText = Utils.readLine(
                input, output, "Prieinamas? (t/n, Enter - nekeisti): ");
        java.math.BigDecimal price = priceText == null || priceText.isBlank()
                ? null : new java.math.BigDecimal(priceText);
        Boolean available = availableText == null || availableText.isBlank()
                ? null : "t".equalsIgnoreCase(availableText);
        ownerConsoleService.updateMenuItem(
                ownerId, restaurant.getId(), item.getId(), price, available);
        output.println("Patiekalas atnaujintas.");
    }

    private void showRestaurantOrders(Long ownerId) {
        Restaurant restaurant = selectOwnerRestaurant(ownerId);
        if (restaurant == null) return;
        List<FoodOrder> orders = ownerConsoleService.listOrders(ownerId, restaurant.getId());
        if (orders.isEmpty()) output.println("Šis restoranas užsakymų neturi.");
        else printOrders(orders);
    }

    private void changeRestaurantOrderStatus(Long ownerId) {
        Restaurant restaurant = selectOwnerRestaurant(ownerId);
        if (restaurant == null) return;
        List<FoodOrder> orders = ownerConsoleService.listOrders(ownerId, restaurant.getId());
        if (orders.isEmpty()) {
            output.println("Šis restoranas užsakymų neturi.");
            return;
        }
        printOrders(orders);
        FoodOrder order = selectedItem(orders,
                Utils.readChoice(input, output, "Užsakymo numeris (0 - grįžti): "));
        if (order == null) return;
        output.println("Leidžiamos būsenos: CONFIRMED, PREPARING, READY_FOR_PICKUP, CANCELLED");
        OrderStatus status = OrderStatus.valueOf(
                Utils.readLine(input, output, "Nauja būsena: ").toUpperCase());
        FoodOrder updated = ownerConsoleService.changeOrderStatus(ownerId, order.getId(), status);
        output.println("Užsakymo būsena pakeista į " + updated.getStatus());
    }

    private Restaurant selectOwnerRestaurant(Long ownerId) {
        List<Restaurant> restaurants = ownerConsoleService.listRestaurants(ownerId);
        if (restaurants.isEmpty()) {
            output.println("Jūs dar neturite restoranų.");
            return null;
        }
        printRestaurants(restaurants);
        return selectedItem(restaurants,
                Utils.readChoice(input, output, "Restorano numeris (0 - grįžti): "));
    }

    private void printOrders(List<FoodOrder> orders) {
        for (int index = 0; index < orders.size(); index++) {
            FoodOrder order = orders.get(index);
            output.println((index + 1) + " - Užsakymas " + order.getId()
                    + " | " + order.getStatus() + " | " + order.getTotalPrice() + " EUR");
        }
    }

    private void browseRestaurants() {
        List<Restaurant> restaurants = clientConsoleService.listRestaurants();
        if (restaurants.isEmpty()) {
            output.println("Restoranų nėra.");
            return;
        }
        output.println("--- RESTORANAI ---");
        printRestaurants(restaurants);
        int restaurantChoice = Utils.readChoice(
                input, output, "Restorano numeris meniu peržiūrai (0 - grįžti): ");
        Restaurant restaurant = selectedItem(restaurants, restaurantChoice);
        if (restaurant == null) return;
        List<MenuItem> items = clientConsoleService.listAvailableMenu(restaurant.getId());
        if (items.isEmpty()) {
            output.println("Šis restoranas neturi prieinamų patiekalų.");
            return;
        }
        output.println("--- MENIU ---");
        printMenu(items);
    }

    private void createOrder(Long clientId) {
        List<Restaurant> restaurants = clientConsoleService.listRestaurants();
        if (restaurants.isEmpty()) {
            output.println("Restoranų nėra.");
            return;
        }
        output.println("--- PASIRINKITE RESTORANĄ ---");
        printRestaurants(restaurants);
        Restaurant restaurant = selectedItem(restaurants,
                Utils.readChoice(input, output, "Restorano numeris (0 - grįžti): "));
        if (restaurant == null) return;

        List<MenuItem> items = clientConsoleService.listAvailableMenu(restaurant.getId());
        if (items.isEmpty()) {
            output.println("Šis restoranas neturi prieinamų patiekalų.");
            return;
        }
        output.println("--- PASIRINKITE PATIEKALĄ ---");
        printMenu(items);
        MenuItem menuItem = selectedItem(items,
                Utils.readChoice(input, output, "Patiekalo numeris (0 - grįžti): "));
        if (menuItem == null) return;

        int quantity = Utils.readChoice(input, output, "Kiekis: ");
        if (quantity <= 0) {
            output.println("Kiekis turi būti didesnis už nulį.");
            return;
        }
        output.println("Užsakymas: " + menuItem.getName() + " x " + quantity);
        output.println("Numatoma suma: "
                + menuItem.getPrice().multiply(java.math.BigDecimal.valueOf(quantity)) + " EUR");
        String confirmation = Utils.readLine(input, output, "Patvirtinti užsakymą? (t/n): ");
        if (!"t".equalsIgnoreCase(confirmation)) {
            output.println("Užsakymo kūrimas atšauktas.");
            return;
        }
        FoodOrder order = clientConsoleService.createSingleItemOrder(
                clientId, restaurant.getId(), menuItem.getId(), quantity);
        output.println("Užsakymas sukurtas. ID=" + order.getId()
                + ", suma=" + order.getTotalPrice() + " EUR");
    }

    private void printRestaurants(List<Restaurant> restaurants) {
        for (int index = 0; index < restaurants.size(); index++) {
            Restaurant restaurant = restaurants.get(index);
            output.println((index + 1) + " - " + restaurant.getName()
                    + " | " + restaurant.getType());
        }
    }

    private void printMenu(List<MenuItem> items) {
        for (int index = 0; index < items.size(); index++) {
            MenuItem item = items.get(index);
            output.println((index + 1) + " - " + item.getName()
                    + " | " + item.getPrice() + " EUR");
        }
    }

    private <T> T selectedItem(List<T> items, int choice) {
        if (choice == 0) return null;
        if (choice < 1 || choice > items.size()) {
            output.println("Neteisingas pasirinkimas.");
            return null;
        }
        return items.get(choice - 1);
    }

    private void showClientOrders(Long clientId) {
        List<FoodOrder> orders = clientConsoleService.listClientOrders(clientId);
        if (orders.isEmpty()) {
            output.println("Užsakymų nėra.");
            return;
        }
        output.println("--- MANO UŽSAKYMAI ---");
        orders.forEach(order -> output.println(
                order.getId() + " | " + order.getRestaurant().getName()
                        + " | " + order.getStatus() + " | " + order.getTotalPrice() + " EUR"));
    }

    public static List<String> menuOptionsFor(UserRole role) {
        if (role == null) throw new IllegalArgumentException("Role is required");
        return switch (role) {
            case CLIENT -> List.of(
                    "Peržiūrėti restoranus ir meniu",
                    "Sukurti užsakymą",
                    "Mano užsakymai",
                    "Užsakymo žinutės ir atsiliepimai");
            case RESTAURANT_OWNER -> List.of(
                    "Sukurti restoraną",
                    "Mano restoranai",
                    "Valdyti meniu",
                    "Restorano užsakymai",
                    "Keisti užsakymo būseną");
            case DRIVER -> List.of(
                    "Pristatymui paruošti užsakymai",
                    "Mano pristatymas",
                    "Keisti pristatymo būseną ir žinutės",
                    "Palikti atsiliepimą klientui");
            case ADMIN -> List.of(
                    "Valdyti vartotojus",
                    "Peržiūrėti restoranus",
                    "Peržiūrėti visus užsakymus",
                    "Peržiūrėti atsiliepimus");
        };
    }

    private String roleTitle(UserRole role) {
        return switch (role) {
            case CLIENT -> "KLIENTO MENIU";
            case RESTAURANT_OWNER -> "RESTORANO SAVININKO MENIU";
            case DRIVER -> "VAIRUOTOJO MENIU";
            case ADMIN -> "ADMINISTRATORIAUS MENIU";
        };
    }
}

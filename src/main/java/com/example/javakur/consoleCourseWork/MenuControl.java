package com.example.javakur.consoleCourseWork;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.UserRole;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.VehicleType;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.ClientConsoleService;
import com.example.javakur.service.OwnerConsoleService;
import com.example.javakur.service.DriverConsoleService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

public class MenuControl {
    private final AuthenticationService authenticationService;
    private final ClientConsoleService clientConsoleService;
    private final OwnerConsoleService ownerConsoleService;
    private final DriverConsoleService driverConsoleService;
    private final BufferedReader input;
    private final PrintStream output;

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientConsoleService,
                       OwnerConsoleService ownerConsoleService,
                       DriverConsoleService driverConsoleService,
                       BufferedReader input, PrintStream output) {
        this.authenticationService = authenticationService;
        this.clientConsoleService = clientConsoleService;
        this.ownerConsoleService = ownerConsoleService;
        this.driverConsoleService = driverConsoleService;
        this.input = input;
        this.output = output;
    }

    public MenuControl(AuthenticationService authenticationService,
                       BufferedReader input, PrintStream output) {
        this(authenticationService, null, null, null, input, output);
    }

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientConsoleService,
                       BufferedReader input, PrintStream output) {
        this(authenticationService, clientConsoleService, null, null, input, output);
    }

    public void run() {
        output.println("=== MAISTO UŽSAKYMŲ SISTEMA ===");
        boolean running = true;
        while (running) {
            output.println();
            output.println("1 - Prisijungti");
            output.println("2 - Registruotis kaip klientui");
            output.println("3 - Registruotis kaip restorano savininkui");
            output.println("4 - Registruotis kaip vairuotojui");
            output.println("0 - Baigti darbą");
            int choice = Utils.readChoiceInRange(input, output, "Pasirinkimas: ", 0, 4);
            switch (choice) {
                case 1 -> login();
                case 2 -> registerClient();
                case 3 -> registerOwner();
                case 4 -> registerDriver();
                case 0 -> running = false;
                default -> output.println("Neteisingas pasirinkimas.");
            }
        }
        output.println("Programa baigta.");
    }

    private void registerDriver() {
        output.println("--- VAIRUOTOJO REGISTRACIJA ---");
        String username = Utils.readRequired(input, output, "Vartotojo vardas: ");
        String password = Utils.readMinLength(input, output, "Slaptažodis (bent 8 simboliai): ", 8);
        String name = Utils.readRequired(input, output, "Vardas: ");
        String surname = Utils.readRequired(input, output, "Pavardė: ");
        String email = Utils.readEmail(input, output, "El. paštas: ");
        List<VehicleType> vehicleTypes = java.util.Arrays.asList(VehicleType.values());
        for (int index = 0; index < vehicleTypes.size(); index++) {
            output.println((index + 1) + " - " + vehicleTypes.get(index));
        }
        try {
            int choice = Utils.readChoiceInRange(
                    input, output, "Transporto tipo numeris: ", 1, vehicleTypes.size());
            VehicleType vehicleType = vehicleTypes.get(choice - 1);
            driverConsoleService.register(username, password, name, surname, email, vehicleType);
            output.println("Vairuotojo registracija sėkminga. Dabar galite prisijungti.");
        } catch (RuntimeException e) {
            output.println("Registracija nepavyko: " + e.getMessage());
        }
    }

    private void registerOwner() {
        output.println("--- RESTORANO SAVININKO REGISTRACIJA ---");
        String username = Utils.readRequired(input, output, "Vartotojo vardas: ");
        String password = Utils.readMinLength(input, output, "Slaptažodis (bent 8 simboliai): ", 8);
        String name = Utils.readRequired(input, output, "Vardas: ");
        String surname = Utils.readRequired(input, output, "Pavardė: ");
        String email = Utils.readEmail(input, output, "El. paštas: ");
        try {
            ownerConsoleService.register(username, password, name, surname, email);
            output.println("Savininko registracija sėkminga. Dabar galite prisijungti.");
        } catch (ValidationException e) {
            output.println("Registracija nepavyko: " + e.getMessage());
        }
    }

    private void registerClient() {
        output.println("--- KLIENTO REGISTRACIJA ---");
        String username = Utils.readRequired(input, output, "Vartotojo vardas: ");
        String password = Utils.readMinLength(input, output, "Slaptažodis (bent 8 simboliai): ", 8);
        String name = Utils.readRequired(input, output, "Vardas: ");
        String surname = Utils.readRequired(input, output, "Pavardė: ");
        String email = Utils.readEmail(input, output, "El. paštas: ");
        String address = Utils.readRequired(input, output, "Pristatymo adresas: ");
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

            int choice = Utils.readChoiceInRange(
                    input, output, "Pasirinkimas: ", 0, options.size());
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
        if (session.role() == UserRole.DRIVER) {
            executeDriverChoice(session, choice);
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

    private void executeDriverChoice(UserSession session, int choice) {
        try {
            switch (choice) {
                case 1 -> showReadyOrders();
                case 2 -> pickUpOrder(session.userId());
                case 3 -> showActiveDeliveries(session.userId());
                case 4 -> changeDeliveryStatus(session.userId());
                case 5 -> output.println("Atsiliepimai bus prijungti M13 etape.");
                default -> output.println("Neteisingas pasirinkimas.");
            }
        } catch (RuntimeException e) {
            output.println("Veiksmas nepavyko: " + e.getMessage());
        }
    }

    private void showReadyOrders() {
        List<FoodOrder> orders = driverConsoleService.listReadyOrders();
        if (orders.isEmpty()) output.println("Pristatymui paruoštų užsakymų nėra.");
        else printOrders(orders);
    }

    private void pickUpOrder(Long driverId) {
        List<FoodOrder> orders = driverConsoleService.listReadyOrders();
        if (orders.isEmpty()) {
            output.println("Pristatymui paruoštų užsakymų nėra.");
            return;
        }
        printOrders(orders);
        FoodOrder order = selectedItem(orders,
                Utils.readChoiceInRange(input, output,
                        "Užsakymo numeris (0 - grįžti): ", 0, orders.size()));
        if (order == null) return;
        FoodOrder pickedUp = driverConsoleService.pickUpOrder(driverId, order.getId());
        output.println("Užsakymas paimtas. Būsena=" + pickedUp.getStatus());
    }

    private void showActiveDeliveries(Long driverId) {
        List<FoodOrder> orders = driverConsoleService.listActiveDeliveries(driverId);
        if (orders.isEmpty()) output.println("Aktyvių pristatymų nėra.");
        else printOrders(orders);
    }

    private void changeDeliveryStatus(Long driverId) {
        List<FoodOrder> orders = driverConsoleService.listActiveDeliveries(driverId);
        if (orders.isEmpty()) {
            output.println("Aktyvių pristatymų nėra.");
            return;
        }
        printOrders(orders);
        FoodOrder order = selectedItem(orders,
                Utils.readChoiceInRange(input, output,
                        "Užsakymo numeris (0 - grįžti): ", 0, orders.size()));
        if (order == null) return;
        List<OrderStatus> statuses = List.of(OrderStatus.DELIVERING, OrderStatus.COMPLETED);
        for (int index = 0; index < statuses.size(); index++) {
            output.println((index + 1) + " - " + statuses.get(index));
        }
        int statusChoice = Utils.readChoiceInRange(
                input, output, "Naujos būsenos numeris: ", 1, statuses.size());
        OrderStatus status = statuses.get(statusChoice - 1);
        FoodOrder updated = driverConsoleService.changeDeliveryStatus(driverId, order.getId(), status);
        output.println("Pristatymo būsena pakeista į " + updated.getStatus());
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
        String name = Utils.readRequired(input, output, "Restorano pavadinimas: ");
        String description = Utils.readLine(input, output, "Aprašymas: ");
        String address = Utils.readRequired(input, output, "Adresas: ");
        List<RestaurantType> types = java.util.Arrays.asList(RestaurantType.values());
        output.println("--- RESTORANO TIPAS ---");
        for (int index = 0; index < types.size(); index++) {
            output.println((index + 1) + " - " + types.get(index));
        }
        int typeChoice = Utils.readChoiceInRange(
                input, output, "Tipo numeris: ", 1, types.size());
        RestaurantType type = types.get(typeChoice - 1);
        java.time.LocalTime opening = Utils.readTime(
                input, output, "Atidarymo laikas (pvz., 9 arba 09:00): ");
        java.time.LocalTime closing = Utils.readTime(
                input, output, "Uždarymo laikas (pvz., 22 arba 22:00): ");
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
        int action = Utils.readChoiceInRange(input, output, "Pasirinkimas: ", 0, 3);
        switch (action) {
            case 1 -> printMenu(ownerConsoleService.listMenu(ownerId, restaurant.getId()));
            case 2 -> addMenuItem(ownerId, restaurant);
            case 3 -> updateMenuItem(ownerId, restaurant);
            case 0 -> { }
            default -> output.println("Neteisingas pasirinkimas.");
        }
    }

    private void addMenuItem(Long ownerId, Restaurant restaurant) {
        String name = Utils.readRequired(input, output, "Patiekalo pavadinimas: ");
        String description = Utils.readLine(input, output, "Aprašymas: ");
        java.math.BigDecimal price = Utils.readPositiveDecimal(input, output, "Kaina: ");
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
                Utils.readChoiceInRange(input, output,
                        "Patiekalo numeris (0 - grįžti): ", 0, items.size()));
        if (item == null) return;
        java.math.BigDecimal price = Utils.readOptionalPositiveDecimal(
                input, output, "Nauja kaina (Enter - nekeisti): ");
        Boolean available = Utils.readOptionalBoolean(
                input, output, "Prieinamas? (taip/ne, Enter - nekeisti): ");
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
                Utils.readChoiceInRange(input, output,
                        "Užsakymo numeris (0 - grįžti): ", 0, orders.size()));
        if (order == null) return;
        List<OrderStatus> statuses = List.of(
                OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED);
        for (int index = 0; index < statuses.size(); index++) {
            output.println((index + 1) + " - " + statuses.get(index));
        }
        int statusChoice = Utils.readChoiceInRange(
                input, output, "Naujos būsenos numeris: ", 1, statuses.size());
        OrderStatus status = statuses.get(statusChoice - 1);
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
                Utils.readChoiceInRange(input, output,
                        "Restorano numeris (0 - grįžti): ", 0, restaurants.size()));
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
        int restaurantChoice = Utils.readChoiceInRange(
                input, output, "Restorano numeris meniu peržiūrai (0 - grįžti): ",
                0, restaurants.size());
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
                Utils.readChoiceInRange(input, output,
                        "Restorano numeris (0 - grįžti): ", 0, restaurants.size()));
        if (restaurant == null) return;

        List<MenuItem> items = clientConsoleService.listAvailableMenu(restaurant.getId());
        if (items.isEmpty()) {
            output.println("Šis restoranas neturi prieinamų patiekalų.");
            return;
        }
        output.println("--- PASIRINKITE PATIEKALĄ ---");
        printMenu(items);
        MenuItem menuItem = selectedItem(items,
                Utils.readChoiceInRange(input, output,
                        "Patiekalo numeris (0 - grįžti): ", 0, items.size()));
        if (menuItem == null) return;

        int quantity = Utils.readPositiveInt(input, output, "Kiekis: ");
        output.println("Užsakymas: " + menuItem.getName() + " x " + quantity);
        output.println("Numatoma suma: "
                + menuItem.getPrice().multiply(java.math.BigDecimal.valueOf(quantity)) + " EUR");
        if (!Utils.readConfirmation(input, output, "Patvirtinti užsakymą? (taip/ne): ")) {
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
                    "Paimti užsakymą",
                    "Mano aktyvūs pristatymai",
                    "Keisti pristatymo būseną",
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

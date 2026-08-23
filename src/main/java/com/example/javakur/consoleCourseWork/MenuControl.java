package com.example.javakur.consoleCourseWork;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.UserRole;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.ClientConsoleService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

public class MenuControl {
    private final AuthenticationService authenticationService;
    private final ClientConsoleService clientConsoleService;
    private final BufferedReader input;
    private final PrintStream output;

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientConsoleService,
                       BufferedReader input, PrintStream output) {
        this.authenticationService = authenticationService;
        this.clientConsoleService = clientConsoleService;
        this.input = input;
        this.output = output;
    }

    public MenuControl(AuthenticationService authenticationService,
                       BufferedReader input, PrintStream output) {
        this(authenticationService, null, input, output);
    }

    public void run() {
        output.println("=== MAISTO UŽSAKYMŲ SISTEMA ===");
        boolean running = true;
        while (running) {
            output.println();
            output.println("1 - Prisijungti");
            output.println("2 - Registruotis kaip klientui");
            output.println("0 - Baigti darbą");
            int choice = Utils.readChoice(input, output, "Pasirinkimas: ");
            switch (choice) {
                case 1 -> login();
                case 2 -> registerClient();
                case 0 -> running = false;
                default -> output.println("Neteisingas pasirinkimas.");
            }
        }
        output.println("Programa baigta.");
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

    private void browseRestaurants() {
        List<Restaurant> restaurants = clientConsoleService.listRestaurants();
        if (restaurants.isEmpty()) {
            output.println("Restoranų nėra.");
            return;
        }
        output.println("--- RESTORANAI ---");
        restaurants.forEach(restaurant -> output.println(
                restaurant.getId() + " - " + restaurant.getName() + " | " + restaurant.getType()));
        int restaurantId = Utils.readChoice(input, output, "Restorano ID meniu peržiūrai (0 - grįžti): ");
        if (restaurantId == 0) return;
        List<MenuItem> items = clientConsoleService.listAvailableMenu((long) restaurantId);
        if (items.isEmpty()) {
            output.println("Šis restoranas neturi prieinamų patiekalų.");
            return;
        }
        output.println("--- MENIU ---");
        items.forEach(item -> output.println(
                item.getId() + " - " + item.getName() + " | " + item.getPrice() + " EUR"));
    }

    private void createOrder(Long clientId) {
        int restaurantId = Utils.readChoice(input, output, "Restorano ID: ");
        int menuItemId = Utils.readChoice(input, output, "Patiekalo ID: ");
        int quantity = Utils.readChoice(input, output, "Kiekis: ");
        FoodOrder order = clientConsoleService.createSingleItemOrder(
                clientId, (long) restaurantId, (long) menuItemId, quantity);
        output.println("Užsakymas sukurtas. ID=" + order.getId()
                + ", suma=" + order.getTotalPrice() + " EUR");
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
                    "Mano restoranai",
                    "Valdyti meniu",
                    "Restorano užsakymai",
                    "Keisti užsakymo būseną ir žinutės");
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

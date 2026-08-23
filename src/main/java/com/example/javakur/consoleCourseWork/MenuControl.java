package com.example.javakur.consoleCourseWork;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.UserRole;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

public class MenuControl {
    private final AuthenticationService authenticationService;
    private final BufferedReader input;
    private final PrintStream output;

    public MenuControl(AuthenticationService authenticationService,
                       BufferedReader input, PrintStream output) {
        this.authenticationService = authenticationService;
        this.input = input;
        this.output = output;
    }

    public void run() {
        output.println("=== MAISTO UŽSAKYMŲ SISTEMA ===");
        boolean running = true;
        while (running) {
            output.println();
            output.println("1 - Prisijungti");
            output.println("0 - Baigti darbą");
            int choice = Utils.readChoice(input, output, "Pasirinkimas: ");
            switch (choice) {
                case 1 -> login();
                case 0 -> running = false;
                default -> output.println("Neteisingas pasirinkimas.");
            }
        }
        output.println("Programa baigta.");
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
                output.println("Pasirinkta: " + options.get(choice - 1));
                output.println("Ši funkcija bus prijungta kitame etape.");
            } else {
                output.println("Neteisingas pasirinkimas.");
            }
        }
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

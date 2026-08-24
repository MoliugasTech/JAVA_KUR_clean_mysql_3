package com.example.javakur.consoleCourseWork;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.UserRole;
import com.example.javakur.model.VehicleType;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.ClientConsoleService;
import com.example.javakur.service.DriverConsoleService;
import com.example.javakur.service.OwnerConsoleService;
import com.example.javakur.service.OrderInteractionService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MenuControl {
    private final AuthenticationService authenticationService;
    private final ClientConsoleService clientService;
    private final OwnerConsoleService ownerService;
    private final DriverConsoleService driverService;
    private final OrderInteractionService interactionService;
    private final BufferedReader input;
    private final PrintStream output;
    private final Map<UserRole, RoleMenuController> roleControllers;

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientService, OwnerConsoleService ownerService,
                       DriverConsoleService driverService, BufferedReader input, PrintStream output) {
        this(authenticationService, clientService, ownerService, driverService, null, input, output);
    }

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientService, OwnerConsoleService ownerService,
                       DriverConsoleService driverService, OrderInteractionService interactionService,
                       BufferedReader input, PrintStream output) {
        this.authenticationService = authenticationService;
        this.clientService = clientService;
        this.ownerService = ownerService;
        this.driverService = driverService;
        this.interactionService = interactionService;
        this.input = input;
        this.output = output;
        this.roleControllers = createRoleControllers();
    }

    public MenuControl(AuthenticationService authenticationService,
                       BufferedReader input, PrintStream output) {
        this(authenticationService, null, null, null, input, output);
    }

    public MenuControl(AuthenticationService authenticationService,
                       ClientConsoleService clientService, BufferedReader input, PrintStream output) {
        this(authenticationService, clientService, null, null, input, output);
    }

    public void run() {
        output.println("=== MAISTO UŽSAKYMŲ SISTEMA ===");
        boolean running = true;
        while (running) {
            output.println("\n1 - Prisijungti\n2 - Registruotis kaip klientui");
            output.println("3 - Registruotis kaip restorano savininkui");
            output.println("4 - Registruotis kaip vairuotojui\n0 - Baigti darbą");
            switch (Utils.readChoiceInRange(input, output, "Pasirinkimas: ", 0, 4)) {
                case 1 -> login();
                case 2 -> registerClient();
                case 3 -> registerOwner();
                case 4 -> registerDriver();
                default -> running = false;
            }
        }
        output.println("Programa baigta.");
    }

    private void login() {
        try {
            UserSession session = authenticationService.login(
                    Utils.readLine(input, output, "Vartotojo vardas: "),
                    Utils.readLine(input, output, "Slaptažodis: "));
            output.println("Prisijungta: " + session.username() + " [" + session.role() + "]");
            showRoleMenu(session);
        } catch (ValidationException e) {
            output.println("Prisijungti nepavyko: " + e.getMessage());
        }
    }

    private void showRoleMenu(UserSession session) {
        RoleMenuController controller = roleControllers.get(session.role());
        boolean loggedIn = true;
        while (loggedIn) {
            output.println("\n--- " + roleTitle(session.role()) + " ---");
            List<String> options = controller.options();
            for (int i = 0; i < options.size(); i++) output.println((i + 1) + " - " + options.get(i));
            output.println("0 - Atsijungti");
            int choice = Utils.readChoiceInRange(input, output, "Pasirinkimas: ", 0, options.size());
            if (choice == 0) {
                loggedIn = false;
                output.println("Atsijungta.");
            } else {
                output.println("Pasirinkta: " + options.get(choice - 1));
                controller.handle(session, choice);
            }
        }
    }

    private void registerClient() {
        output.println("--- KLIENTO REGISTRACIJA ---");
        try {
            clientService.register(readUsername(), readPassword(), readName(), readSurname(),
                    Utils.readEmail(input, output, "El. paštas: "),
                    Utils.readRequired(input, output, "Pristatymo adresas: "));
            output.println("Registracija sėkminga. Dabar galite prisijungti.");
        } catch (ValidationException e) {
            output.println("Registracija nepavyko: " + e.getMessage());
        }
    }

    private void registerOwner() {
        output.println("--- RESTORANO SAVININKO REGISTRACIJA ---");
        try {
            ownerService.register(readUsername(), readPassword(), readName(), readSurname(),
                    Utils.readEmail(input, output, "El. paštas: "));
            output.println("Savininko registracija sėkminga. Dabar galite prisijungti.");
        } catch (ValidationException e) {
            output.println("Registracija nepavyko: " + e.getMessage());
        }
    }

    private void registerDriver() {
        output.println("--- VAIRUOTOJO REGISTRACIJA ---");
        String username = readUsername();
        String password = readPassword();
        String name = readName();
        String surname = readSurname();
        String email = Utils.readEmail(input, output, "El. paštas: ");
        List<VehicleType> types = Arrays.asList(VehicleType.values());
        for (int i = 0; i < types.size(); i++) output.println((i + 1) + " - " + types.get(i));
        VehicleType type = types.get(Utils.readChoiceInRange(
                input, output, "Transporto tipo numeris: ", 1, types.size()) - 1);
        try {
            driverService.register(username, password, name, surname, email, type);
            output.println("Vairuotojo registracija sėkminga. Dabar galite prisijungti.");
        } catch (ValidationException e) {
            output.println("Registracija nepavyko: " + e.getMessage());
        }
    }

    private String readUsername() { return Utils.readRequired(input, output, "Vartotojo vardas: "); }
    private String readPassword() { return Utils.readMinLength(input, output, "Slaptažodis (bent 8 simboliai): ", 8); }
    private String readName() { return Utils.readRequired(input, output, "Vardas: "); }
    private String readSurname() { return Utils.readRequired(input, output, "Pavardė: "); }

    private Map<UserRole, RoleMenuController> createRoleControllers() {
        Map<UserRole, RoleMenuController> controllers = new EnumMap<>(UserRole.class);
        OrderInteractionMenu interactions = new OrderInteractionMenu(interactionService, input, output);
        controllers.put(UserRole.CLIENT,
                new ClientMenuController(clientService, interactions, input, output));
        controllers.put(UserRole.RESTAURANT_OWNER,
                new OwnerMenuController(ownerService, interactions, input, output));
        controllers.put(UserRole.DRIVER,
                new DriverMenuController(driverService, interactions, input, output));
        controllers.put(UserRole.ADMIN, new AdminMenuController(input, output));
        return controllers;
    }

    public static List<String> menuOptionsFor(UserRole role) {
        return switch (role) {
            case CLIENT -> ClientMenuController.OPTIONS;
            case RESTAURANT_OWNER -> OwnerMenuController.OPTIONS;
            case DRIVER -> DriverMenuController.OPTIONS;
            case ADMIN -> AdminMenuController.OPTIONS;
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

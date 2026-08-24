package com.example.javakur.consoleCourseWork;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.Review;
import com.example.javakur.model.User;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AdminConsoleService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

class AdminMenuController extends AbstractRoleMenuController {
    static final List<String> OPTIONS = List.of(
            "Valdyti vartotojus", "Peržiūrėti restoranus",
            "Peržiūrėti visus užsakymus", "Peržiūrėti atsiliepimus");

    private final AdminConsoleService service;

    AdminMenuController(AdminConsoleService service, BufferedReader input, PrintStream output) {
        super(input, output);
        this.service = service;
    }

    AdminMenuController(BufferedReader input, PrintStream output) { this(null, input, output); }
    public List<String> options() { return OPTIONS; }
    public void handle(UserSession session, int choice) {
        if (service == null) { output.println("Administratoriaus modulis nepasiekiamas."); return; }
        try {
            switch (choice) {
                case 1 -> manageUsers(session.userId());
                case 2 -> showRestaurants(session.userId());
                case 3 -> showOrders(session.userId());
                case 4 -> showReviews(session.userId());
                default -> output.println("Neteisingas pasirinkimas.");
            }
        } catch (RuntimeException e) {
            output.println("Veiksmas nepavyko: " + e.getMessage());
        }
    }

    private void manageUsers(Long adminId) {
        List<User> users = service.listUsers(adminId);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            output.println((i + 1) + " - " + user.getUsername() + " | " + user.getRole()
                    + " | " + (user.isActive() ? "AKTYVI" : "UŽBLOKUOTA"));
        }
        User user = select(users, "Naudotojo numeris būsenai keisti (0 - grįžti): ");
        if (user == null) return;
        boolean activate = !user.isActive();
        if (!Utils.readConfirmation(input, output,
                (activate ? "Atblokuoti" : "Užblokuoti") + " naudotoją? (taip/ne): ")) return;
        User updated = service.setUserActive(adminId, user.getId(), activate);
        output.println("Paskyros būsena: " + (updated.isActive() ? "AKTYVI" : "UŽBLOKUOTA"));
    }

    private void showRestaurants(Long adminId) {
        List<Restaurant> values = service.listRestaurants(adminId);
        if (values.isEmpty()) { output.println("Restoranų nėra."); return; }
        values.forEach(value -> output.println(value.getId() + " | " + value.getName()
                + " | savininkas=" + value.getOwner().getUsername()));
    }

    private void showOrders(Long adminId) {
        List<FoodOrder> values = service.listOrders(adminId);
        if (values.isEmpty()) { output.println("Užsakymų nėra."); return; }
        values.forEach(value -> output.println(value.getId() + " | " + value.getStatus()
                + " | klientas=" + value.getClient().getUsername()
                + " | restoranas=" + value.getRestaurant().getName()
                + " | vairuotojas=" + (value.getDriver() == null
                ? "nepriskirtas" : value.getDriver().getUsername())));
    }

    private void showReviews(Long adminId) {
        List<Review> values = service.listReviews(adminId);
        if (values.isEmpty()) { output.println("Atsiliepimų nėra."); return; }
        values.forEach(value -> output.println(value.getId() + " | " + value.getType()
                + " | " + value.getRating() + "/5 | autorius="
                + value.getAuthor().getUsername() + " | "
                + (value.getComment() == null ? "be komentaro" : value.getComment())));
    }
}

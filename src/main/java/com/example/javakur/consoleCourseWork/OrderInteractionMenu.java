package com.example.javakur.consoleCourseWork;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.Review;
import com.example.javakur.model.UserRole;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.OrderInteractionService;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

class OrderInteractionMenu {
    private final OrderInteractionService service;
    private final BufferedReader input;
    private final PrintStream output;

    OrderInteractionMenu(OrderInteractionService service, BufferedReader input, PrintStream output) {
        this.service = service;
        this.input = input;
        this.output = output;
    }

    void open(UserSession session) {
        if (service == null) {
            output.println("Užsakymų bendravimo modulis nepasiekiamas.");
            return;
        }
        List<FoodOrder> orders = service.listParticipantOrders(session.userId());
        if (orders.isEmpty()) {
            output.println("Su jumis susietų užsakymų nėra.");
            return;
        }
        printOrders(orders);
        FoodOrder order = select(orders, "Užsakymo numeris (0 - grįžti): ");
        if (order == null) return;
        boolean active = true;
        while (active) {
            output.println("1 - Peržiūrėti pokalbį");
            output.println("2 - Siųsti žinutę");
            output.println("3 - Peržiūrėti atsiliepimus");
            if (session.role() == UserRole.CLIENT) {
                output.println("4 - Įvertinti restoraną");
                output.println("5 - Įvertinti vairuotoją");
            } else if (session.role() == UserRole.DRIVER) {
                output.println("4 - Įvertinti klientą");
            }
            output.println("0 - Grįžti");
            int max = session.role() == UserRole.CLIENT ? 5
                    : session.role() == UserRole.DRIVER ? 4 : 3;
            int choice = Utils.readChoiceInRange(input, output, "Pasirinkimas: ", 0, max);
            switch (choice) {
                case 1 -> showConversation(session.userId(), order.getId());
                case 2 -> sendMessage(session.userId(), order.getId());
                case 3 -> showReviews(session.userId(), order.getId());
                case 4 -> leavePrimaryReview(session, order.getId());
                case 5 -> leaveReview(session.userId(), order.getId(), false);
                default -> active = false;
            }
        }
    }

    private void showConversation(Long userId, Long orderId) {
        List<OrderMessage> messages = service.getConversation(userId, orderId);
        if (messages.isEmpty()) {
            output.println("Žinučių nėra.");
            return;
        }
        messages.forEach(message -> output.println(message.getSentAt() + " | "
                + message.getSender().getUsername() + ": " + message.getContent()));
    }

    private void sendMessage(Long userId, Long orderId) {
        OrderMessage message = service.sendMessage(userId, orderId,
                Utils.readRequired(input, output, "Žinutė: "));
        output.println("Žinutė išsiųsta. ID=" + message.getId());
    }

    private void showReviews(Long userId, Long orderId) {
        List<Review> reviews = service.getReviews(userId, orderId);
        if (reviews.isEmpty()) {
            output.println("Atsiliepimų nėra.");
            return;
        }
        reviews.forEach(review -> output.println(review.getType() + " | "
                + review.getRating() + "/5 | "
                + (review.getComment() == null ? "be komentaro" : review.getComment())));
    }

    private void leavePrimaryReview(UserSession session, Long orderId) {
        if (session.role() == UserRole.CLIENT) leaveReview(session.userId(), orderId, true);
        else if (session.role() == UserRole.DRIVER) {
            byte rating = readRating();
            Review review = service.reviewClient(session.userId(), orderId, rating, readComment());
            output.println("Atsiliepimas klientui išsaugotas. ID=" + review.getId());
        }
    }

    private void leaveReview(Long clientId, Long orderId, boolean restaurant) {
        byte rating = readRating();
        String comment = readComment();
        Review review = restaurant
                ? service.reviewRestaurant(clientId, orderId, rating, comment)
                : service.reviewDriver(clientId, orderId, rating, comment);
        output.println("Atsiliepimas išsaugotas. ID=" + review.getId());
    }

    private byte readRating() {
        return (byte) Utils.readChoiceInRange(input, output, "Įvertinimas (1-5): ", 1, 5);
    }

    private String readComment() {
        return Utils.readLine(input, output, "Komentaras (Enter - be komentaro): ");
    }

    private void printOrders(List<FoodOrder> orders) {
        for (int i = 0; i < orders.size(); i++) {
            FoodOrder order = orders.get(i);
            output.println((i + 1) + " - Užsakymas " + order.getId()
                    + " | " + order.getStatus() + " | " + order.getTotalPrice() + " EUR");
        }
    }

    private <T> T select(List<T> items, String prompt) {
        int choice = Utils.readChoiceInRange(input, output, prompt, 0, items.size());
        return choice == 0 ? null : items.get(choice - 1);
    }
}

package com.example.javakur.consoleCourseWork;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

abstract class AbstractRoleMenuController implements RoleMenuController {
    protected final BufferedReader input;
    protected final PrintStream output;

    protected AbstractRoleMenuController(BufferedReader input, PrintStream output) {
        this.input = input;
        this.output = output;
    }

    protected void printRestaurants(List<Restaurant> restaurants) {
        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurant = restaurants.get(i);
            output.println((i + 1) + " - " + restaurant.getName() + " | " + restaurant.getType());
        }
    }

    protected void printMenu(List<MenuItem> items) {
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            output.println((i + 1) + " - " + item.getName() + " | " + item.getPrice() + " EUR");
        }
    }

    protected void printOrders(List<FoodOrder> orders) {
        for (int i = 0; i < orders.size(); i++) {
            FoodOrder order = orders.get(i);
            output.println((i + 1) + " - Užsakymas " + order.getId()
                    + " | " + order.getStatus() + " | " + order.getTotalPrice() + " EUR");
        }
    }

    protected <T> T select(List<T> items, String prompt) {
        int choice = Utils.readChoiceInRange(input, output, prompt, 0, items.size());
        return choice == 0 ? null : items.get(choice - 1);
    }
}

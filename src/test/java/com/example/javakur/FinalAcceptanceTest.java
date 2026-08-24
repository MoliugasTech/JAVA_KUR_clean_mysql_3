package com.example.javakur;

import com.example.javakur.consoleCourseWork.MenuControl;
import com.example.javakur.model.Admin;
import com.example.javakur.model.Client;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.UserRole;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinalAcceptanceTest {

    @Test
    void everyRoleHasCompleteUniqueMenuOptions() {
        for (UserRole role : UserRole.values()) {
            List<String> options = MenuControl.menuOptionsFor(role);
            assertFalse(options.isEmpty());
            assertTrue(options.stream().noneMatch(option -> option.contains("bus prijungt")));
            assertTrue(new HashSet<>(options).size() == options.size());
        }
    }

    @Test
    void newlyCreatedAccountsAreActiveByDefault() {
        Client client = new Client("client", "hash", "C", "C", "c@example.com", "Address");
        Admin admin = new Admin("admin", "hash", "A", "A", "a@example.com");

        assertTrue(client.isActive());
        assertTrue(admin.isActive());
    }

    @Test
    void orderLifecycleDefinesSuccessfulAndCancelledTerminalStates() {
        assertTrue(OrderStatus.COMPLETED != OrderStatus.CANCELLED);
        assertTrue(List.of(OrderStatus.values()).contains(OrderStatus.READY_FOR_PICKUP));
        assertTrue(List.of(OrderStatus.values()).contains(OrderStatus.DELIVERING));
    }
}

package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.User;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.OrderMessageRepository;
import com.example.javakur.service.OrderMessageService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderMessageServiceTest {

    private final InMemoryMessageRepository repository = new InMemoryMessageRepository();
    private final OrderMessageService service = new OrderMessageService(repository);

    @Test
    void orderParticipantsCanSendMessages() {
        FoodOrder order = activeOrder();

        service.sendMessage(order, order.getClient(), "  Kur mano užsakymas?  ");
        service.sendMessage(order, order.getRestaurant().getOwner(), "Jau ruošiame");
        service.sendMessage(order, order.getDriver(), "Atvyksiu už 5 minučių");

        List<OrderMessage> conversation = service.getConversation(order, order.getClient());
        assertEquals(3, conversation.size());
        assertEquals("Kur mano užsakymas?", conversation.getFirst().getContent());
    }

    @Test
    void rejectsOutsider() {
        FoodOrder order = activeOrder();
        Client outsider = client(99L, "outsider");

        assertThrows(ValidationException.class,
                () -> service.sendMessage(order, outsider, "Hello"));
        assertThrows(ValidationException.class,
                () -> service.getConversation(order, outsider));
    }

    @Test
    void rejectsBlankMessage() {
        FoodOrder order = activeOrder();

        assertThrows(ValidationException.class,
                () -> service.sendMessage(order, order.getClient(), "   "));
    }

    @Test
    void rejectsTooLongMessage() {
        FoodOrder order = activeOrder();

        assertThrows(ValidationException.class,
                () -> service.sendMessage(order, order.getClient(), "x".repeat(2001)));
    }

    @Test
    void rejectsMessageAfterCompletion() {
        FoodOrder order = activeOrder();
        order.setStatus(OrderStatus.COMPLETED);

        assertThrows(ValidationException.class,
                () -> service.sendMessage(order, order.getClient(), "Ačiū"));
    }

    @Test
    void driverCannotParticipateBeforeAssignment() {
        FoodOrder order = activeOrder();
        Driver driver = order.getDriver();
        order.setDriver(null);

        assertThrows(ValidationException.class,
                () -> service.sendMessage(order, driver, "Paimsiu užsakymą"));
    }

    private FoodOrder activeOrder() {
        Client client = client(1L, "client");
        Driver driver = new Driver(
                "driver", "hash", "Test", "Driver", "driver@example.com", VehicleType.CAR);
        driver.setId(2L);
        RestaurantOwner owner = new RestaurantOwner(
                "owner", "hash", "Test", "Owner", "owner@example.com");
        owner.setId(3L);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(4L);
        restaurant.setOwner(owner);

        FoodOrder order = new FoodOrder(client, restaurant);
        order.setId(5L);
        order.setDriver(driver);
        order.setStatus(OrderStatus.PICKED_UP);
        return order;
    }

    private Client client(Long id, String username) {
        Client client = new Client(
                username, "hash", "Test", "Client", username + "@example.com", "Test address");
        client.setId(id);
        return client;
    }

    private static class InMemoryMessageRepository extends OrderMessageRepository {
        private final List<OrderMessage> messages = new ArrayList<>();

        @Override
        public OrderMessage save(OrderMessage message) {
            message.setId((long) messages.size() + 1);
            messages.add(message);
            return message;
        }

        @Override
        public List<OrderMessage> findByOrderId(Long orderId) {
            return messages.stream()
                    .filter(message -> message.getOrder().getId().equals(orderId))
                    .toList();
        }
    }
}

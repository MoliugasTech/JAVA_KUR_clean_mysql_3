package com.example.fooddeliverymobile;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfileInfoTest {
    private final Gson gson = new Gson();

    @Test
    public void parsesClientSpecificProfileFields() {
        ProfileInfo profile = gson.fromJson(
                "{\"id\":7,\"username\":\"client\",\"deliveryAddress\":\"Vilnius\",\"bonusPoints\":12}",
                ProfileInfo.class);

        assertEquals("client", profile.getUsername());
        assertEquals("Vilnius", profile.getDeliveryAddress());
        assertEquals(12, profile.getBonusPoints());
    }

    @Test
    public void parsesDriverVehicleType() {
        ProfileInfo profile = gson.fromJson(
                "{\"id\":9,\"username\":\"driver\",\"vehicleType\":\"CAR\"}",
                ProfileInfo.class);

        assertEquals("driver", profile.getUsername());
        assertEquals("CAR", profile.getVehicleType());
    }
}

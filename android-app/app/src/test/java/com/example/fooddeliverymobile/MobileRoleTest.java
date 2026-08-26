package com.example.fooddeliverymobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MobileRoleTest {
    @Test
    public void apiRoleIsParsedIgnoringCase() {
        assertEquals(MobileRole.CLIENT, MobileRole.fromApi("client"));
        assertEquals(MobileRole.DRIVER, MobileRole.fromApi("DRIVER"));
    }

    @Test
    public void desktopRoleIsRejected() {
        assertNull(MobileRole.fromApi("ADMIN"));
        assertNull(MobileRole.fromApi("RESTAURANT_OWNER"));
        assertNull(MobileRole.fromApi(null));
    }
}

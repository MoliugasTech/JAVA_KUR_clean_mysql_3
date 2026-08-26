package com.example.fooddeliverymobile;

public class ProfileInfo {
    private long id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private String deliveryAddress;
    private int bonusPoints;
    private String vehicleType;
    private boolean active;

    public long getId() { return id; }
    public String getUsername() { return safe(username); }
    public String getName() { return safe(name); }
    public String getSurname() { return safe(surname); }
    public String getEmail() { return safe(email); }
    public String getPhoneNumber() { return safe(phoneNumber); }
    public String getDeliveryAddress() { return safe(deliveryAddress); }
    public int getBonusPoints() { return bonusPoints; }
    public String getVehicleType() { return safe(vehicleType); }
    public boolean isActive() { return active; }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

package com.example.fooddeliverymobile;

public class RestaurantInfo {
    private long id;
    private String name;
    private String description;
    private String address;
    private String type;
    private String openingTime;
    private String closingTime;

    public long getId() { return id; }
    public String getName() { return value(name); }
    public String getDescription() { return value(description); }
    public String getAddress() { return value(address); }
    public String getType() { return value(type); }
    public String getOpeningTime() { return value(openingTime); }
    public String getClosingTime() { return value(closingTime); }
    private String value(String text) { return text == null ? "" : text; }

    @Override
    public String toString() {
        return getName() + "\n" + getAddress() + " · " + getOpeningTime() + "–" + getClosingTime();
    }
}

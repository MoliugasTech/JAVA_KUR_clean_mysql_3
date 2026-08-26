package com.example.fooddeliverymobile;

public final class StatusLabels {
    private StatusLabels() {}

    public static String toLithuanian(String status) {
        if (status == null) return "";
        return switch (status) {
            case "CREATED" -> "Sukurtas";
            case "CONFIRMED" -> "Patvirtintas";
            case "PREPARING" -> "Ruošiamas";
            case "READY_FOR_PICKUP" -> "Paruoštas atsiėmimui";
            case "PICKED_UP" -> "Paimtas vairuotojo";
            case "DELIVERING" -> "Pristatomas";
            case "COMPLETED" -> "Užbaigtas";
            case "CANCELLED" -> "Atšauktas";
            default -> status;
        };
    }
}

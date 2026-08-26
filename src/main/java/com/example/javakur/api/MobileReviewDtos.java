package com.example.javakur.api;

import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class MobileReviewDtos {
    private MobileReviewDtos() {
    }

    public record ReviewRequest(
            @Min(value = 1, message = "Įvertinimas turi būti nuo 1 iki 5")
            @Max(value = 5, message = "Įvertinimas turi būti nuo 1 iki 5")
            int rating,
            @Size(max = 2000, message = "Atsiliepimo komentaras negali viršyti 2000 simbolių")
            String comment) {
    }

    public record ReviewResponse(Long id, ReviewType type, int rating, String comment,
                                 LocalDateTime createdAt) {
        public static ReviewResponse from(Review review) {
            return new ReviewResponse(review.getId(), review.getType(), review.getRating(),
                    review.getComment(), review.getCreatedAt());
        }
    }
}

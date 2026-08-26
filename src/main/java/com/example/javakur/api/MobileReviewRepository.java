package com.example.javakur.api;

import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MobileReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderIdAndAuthorIdAndType(Long orderId, Long authorId, ReviewType type);

    List<Review> findByOrderIdAndAuthorIdOrderByCreatedAtAsc(Long orderId, Long authorId);
}

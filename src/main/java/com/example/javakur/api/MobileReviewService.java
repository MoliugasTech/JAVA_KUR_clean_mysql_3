package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.javakur.api.MobileReviewDtos.ReviewRequest;
import static com.example.javakur.api.MobileReviewDtos.ReviewResponse;

@Service
@Transactional(readOnly = true)
public class MobileReviewService {
    private final MobileUserRepository userRepository;
    private final ClientOrderRepository clientOrderRepository;
    private final DriverOrderRepository driverOrderRepository;
    private final MobileReviewRepository reviewRepository;

    public MobileReviewService(MobileUserRepository userRepository,
                               ClientOrderRepository clientOrderRepository,
                               DriverOrderRepository driverOrderRepository,
                               MobileReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.clientOrderRepository = clientOrderRepository;
        this.driverOrderRepository = driverOrderRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewResponse> clientReviews(Long clientId, Long orderId) {
        requireClient(clientId);
        requireClientOrder(clientId, orderId);
        return ownReviews(orderId, clientId);
    }

    public List<ReviewResponse> driverReviews(Long driverId, Long orderId) {
        requireDriver(driverId);
        requireDriverOrder(driverId, orderId);
        return ownReviews(orderId, driverId);
    }

    @Transactional
    public ReviewResponse reviewRestaurant(Long clientId, Long orderId, ReviewRequest request) {
        Client client = requireClient(clientId);
        FoodOrder order = requireClientOrder(clientId, orderId);
        return create(order, client, ReviewType.RESTAURANT, request);
    }

    @Transactional
    public ReviewResponse reviewDriver(Long clientId, Long orderId, ReviewRequest request) {
        Client client = requireClient(clientId);
        FoodOrder order = requireClientOrder(clientId, orderId);
        if (order.getDriver() == null) {
            throw new ValidationException("Užsakymas neturi priskirto vairuotojo");
        }
        return create(order, client, ReviewType.DRIVER, request);
    }

    @Transactional
    public ReviewResponse reviewClient(Long driverId, Long orderId, ReviewRequest request) {
        Driver driver = requireDriver(driverId);
        FoodOrder order = requireDriverOrder(driverId, orderId);
        return create(order, driver, ReviewType.CLIENT, request);
    }

    private ReviewResponse create(FoodOrder order, com.example.javakur.model.User author,
                                  ReviewType type, ReviewRequest request) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ValidationException("Vertinti galima tik užbaigtus užsakymus");
        }
        if (reviewRepository.existsByOrderIdAndAuthorIdAndType(
                order.getId(), author.getId(), type)) {
            throw new ValidationException("Šis užsakymas jau įvertintas pasirinktam tipui");
        }
        String comment = request.comment() == null || request.comment().isBlank()
                ? null : request.comment().trim();
        Review review = switch (type) {
            case RESTAURANT -> new Review(order, author, type, order.getRestaurant(), null,
                    (byte) request.rating(), comment);
            case DRIVER -> new Review(order, author, type, null, order.getDriver(),
                    (byte) request.rating(), comment);
            case CLIENT -> new Review(order, author, type, null, order.getClient(),
                    (byte) request.rating(), comment);
        };
        return ReviewResponse.from(reviewRepository.save(review));
    }

    private List<ReviewResponse> ownReviews(Long orderId, Long authorId) {
        return reviewRepository.findByOrderIdAndAuthorIdOrderByCreatedAtAsc(orderId, authorId)
                .stream().map(ReviewResponse::from).toList();
    }

    private Client requireClient(Long clientId) {
        return userRepository.findActiveClientById(clientId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Klientas nerastas"));
    }

    private Driver requireDriver(Long driverId) {
        return userRepository.findActiveDriverById(driverId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Vairuotojas nerastas"));
    }

    private FoodOrder requireClientOrder(Long clientId, Long orderId) {
        return clientOrderRepository.findByIdAndClientId(orderId, clientId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Užsakymas nerastas"));
    }

    private FoodOrder requireDriverOrder(Long driverId, Long orderId) {
        return driverOrderRepository.findByIdAndDriverId(orderId, driverId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Užsakymas nerastas"));
    }
}

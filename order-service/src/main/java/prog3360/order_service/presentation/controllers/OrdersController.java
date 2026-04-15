package prog3360.order_service.presentation.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog3360.order_service.domain.dto.ProductDTO;
import prog3360.order_service.domain.entity.Order;
import prog3360.order_service.domain.repository.IOrderRepository;
import prog3360.order_service.infrastructure.client.IProductClient;
import prog3360.order_service.service.FeatureFlagService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    private final IProductClient IProductClient;
    private final IOrderRepository orderRepository;
    private final FeatureFlagService featureFlagService;

    public OrdersController(IProductClient IProductClient, IOrderRepository orderRepository, FeatureFlagService featureFlagService) {
        this.IProductClient = IProductClient;
        this.orderRepository = orderRepository;
        this.featureFlagService = featureFlagService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Assignment 4 - Part 1: Logging
    // Instruction: "Log output from both services containing at least one INFO, one WARN, and one ERROR line each"
    // Extended createOrder() to ResponseEntity<?>, added Feign failure handling and business event logs
    // - INFO (Event 1): order creation request received, includes productId, quantity
    // - INFO (Event 2): calling product-service, includes productId
    // - ERROR (Event 3): Feign call failed, includes productId, error message
    // - WARN (Event 4): product not found, includes productId
    // - WARN: insufficient stock, includes productId, requested, available
    // - INFO (Event 5): order created successfully, includes orderId, productId, name, quantity, totalPrice

    // Feature Flag: bulk-order-discount - 15% discount when quantity > 5
    // Feature Flag: order-notifications - logs order confirmation (simulates email/SMS)
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        // Event 1: Receiving an order creation request
        logger.info("Order creation request received: productId={}, quantity={}",
                order.getProductId(), order.getQuantity());

        // Event 2: order-service calling product-service, including the target URL (ref. a4_Instruction.md:94-95)
        logger.info("Calling product-service to validate product: productId={}",
                order.getProductId());

        ProductDTO product;
        try {
            product = IProductClient.getProductById(order.getProductId());
        } catch (Exception e) {
            // Event 3: Unexpected exception caught in a controller
            logger.error("Failed to call product-service for productId={}: {}",
                    order.getProductId(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to reach product-service: " + e.getMessage());
        }

        if (product == null) {
            // Event 4: Product lookup returning no results
            logger.warn("Product not found for ID: {}", order.getProductId());
            return ResponseEntity.badRequest()
                    .body("Product with ID " + order.getProductId() + " does not exist.");
        }
        if (product.getQuantity() < order.getQuantity()) {
            logger.warn("Insufficient stock for productId={}: requested={}, available={}",
                    order.getProductId(), order.getQuantity(), product.getQuantity());
            return ResponseEntity.badRequest()
                    .body("Requested quantity exceeds available stock. Available: "
                            + product.getQuantity());
        }

        double totalPrice = product.getPrice() * order.getQuantity();

        // Feature Flag: bulk-order-discount
        if (featureFlagService.isBulkOrderDiscountEnabled() && order.getQuantity() > 5) {
            totalPrice = totalPrice * 0.85; // 15% discount
        }

        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // Feature Flag: order-notifications
        // Event 5: Order created successfully
        if (featureFlagService.isOrderNotificationsEnabled()) {
            logger.info("ORDER NOTIFICATION - Order ID: {}, Product ID: {}, Product Name: {}, Quantity: {}, Total Price: {}",
                    savedOrder.getId(), savedOrder.getProductId(), product.getName(),
                    savedOrder.getQuantity(), savedOrder.getTotalPrice());
        }

        return ResponseEntity.ok(savedOrder);
    }
}

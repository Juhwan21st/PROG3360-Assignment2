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

    // Feature Flag: bulk-order-discount — 15% discount when quantity > 5
    // Feature Flag: order-notifications — logs order confirmation (simulates email/SMS)
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        ProductDTO product = IProductClient.getProductById(order.getProductId());

        if (product == null) {
            return ResponseEntity.badRequest()
                    .body("Product with ID " + order.getProductId() + " does not exist.");
        }
        if (product.getQuantity() < order.getQuantity()) {
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
        if (featureFlagService.isOrderNotificationsEnabled()) {
            logger.info("ORDER NOTIFICATION - Order ID: {}, Product ID: {}, Product Name: {}, Quantity: {}, Total Price: {}",
                    savedOrder.getId(), savedOrder.getProductId(), product.getName(), savedOrder.getQuantity(), savedOrder.getTotalPrice());
        }

        return ResponseEntity.ok(savedOrder);
    }
}

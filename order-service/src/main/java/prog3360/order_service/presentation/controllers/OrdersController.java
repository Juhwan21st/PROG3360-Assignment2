package prog3360.order_service.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog3360.order_service.domain.dto.ProductDTO;
import prog3360.order_service.domain.entity.Order;
import prog3360.order_service.domain.repository.IOrderRepository;
import prog3360.order_service.infrastructure.client.IProductClient;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final IProductClient IProductClient;
    private final IOrderRepository orderRepository;

    public OrdersController(IProductClient IProductClient, IOrderRepository orderRepository) {
        this.IProductClient = IProductClient;
        this.orderRepository = orderRepository;
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
        order.setTotalPrice(product.getPrice() * order.getQuantity());
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(savedOrder);
    }
}

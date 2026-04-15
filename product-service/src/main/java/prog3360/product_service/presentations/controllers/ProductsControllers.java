package prog3360.product_service.presentations.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog3360.product_service.domain.entity.Product;
import prog3360.product_service.domain.repository.IProductRepository;
import prog3360.product_service.service.FeatureFlagService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductsControllers {
    // SLF4J Logger pattern (ref. week5 'event-message-demo' lab
    private static final Logger logger = LoggerFactory.getLogger(ProductsControllers.class);

    private final IProductRepository repository;
    private final FeatureFlagService featureFlagService;

    public ProductsControllers(IProductRepository repository, FeatureFlagService featureFlagService) {
        this.repository = repository;
        this.featureFlagService = featureFlagService;
    }

    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }

    // Assignment 4 - Part 1: Logging
    // Instruction: "Log output from both services containing at least one INFO, one WARN, and one ERROR line each"
    // Extended getById() to ResponseEntity<?>, added business event logs on success and not-found paths
    // - INFO (Event 5): product retrieved successfully, includes id, name, price
    // - WARN (Event 6): product not found for given ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Product product = repository.findById(id).orElseThrow();
            // Event 5: Successfully retrieving a product
            logger.info("Product retrieved: id={}, name={}, price={}",
                    product.getId(), product.getName(), product.getPrice());
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            // Event 6: Product lookup returning no results for a given ID
            logger.warn("Product not found for ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // Assignment 4 - Part 1: Logging
    // Extended create() to ResponseEntity<?>, added business event logs on success and failure paths
    // - INFO (Event 7): product created successfully, includes id, name, price
    // - ERROR (Event 8): unexpected exception while saving product, includes name, error message
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        try {
            Product saved = repository.save(product);
            // Event 7: Product created successfully
            logger.info("Product created: id={}, name={}, price={}",
                    saved.getId(), saved.getName(), saved.getPrice());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            // Event 8: Unexpected exception caught while saving product
            logger.error("Failed to create product: name={}, error={}",
                    product.getName(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to create product: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // Feature Flag: premium-pricing
    // When enabled, applies a 10% discount to all product prices
    // Returns both originalPrice and price so the caller can verify discount was applied
    @GetMapping("/premium")
    public List<Map<String, Object>> getPremiumProducts() {
        List<Product> products = repository.findAll();
        return products.stream().map(product -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", product.getId());
            dto.put("name", product.getName());
            dto.put("originalPrice", product.getPrice());
            double price = featureFlagService.isPremiumPricingEnabled()
                    ? product.getPrice() * 0.9 // 10% discount when flag is ON
                    : product.getPrice();
            dto.put("price", price);
            dto.put("quantity", product.getQuantity());
            dto.put("premiumPricingEnabled", featureFlagService.isPremiumPricingEnabled());
            return dto;
        }).collect(Collectors.toList());
    }
}

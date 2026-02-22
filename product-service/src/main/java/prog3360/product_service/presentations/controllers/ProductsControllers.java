package prog3360.product_service.presentations.controllers;

import org.springframework.web.bind.annotation.*;
import prog3360.product_service.domain.entity.Product;
import prog3360.product_service.domain.repository.IProductRepository;
import prog3360.product_service.service.FeatureFlagService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductsControllers {
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

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        return repository.save(product);
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

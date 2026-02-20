package prog3360.product_service.presentations.controllers;

import org.springframework.web.bind.annotation.*;
import prog3360.product_service.domain.entity.Product;
import prog3360.product_service.domain.repository.IProductRepository;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsControllers {
    private final IProductRepository repository;

    public ProductsControllers(IProductRepository repository) {
        this.repository = repository;
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
}

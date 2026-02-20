package prog3360.product_service.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prog3360.product_service.domain.entity.Product;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
}

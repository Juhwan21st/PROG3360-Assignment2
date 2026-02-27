package prog3360.order_service.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import prog3360.order_service.domain.dto.ProductDTO;

import java.util.List;

@FeignClient(name = "product-service", url = "${server.product.url}")
public interface IProductClient {

    @GetMapping("/api/products")
    List<ProductDTO> getAllProducts();

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable Long id);
}

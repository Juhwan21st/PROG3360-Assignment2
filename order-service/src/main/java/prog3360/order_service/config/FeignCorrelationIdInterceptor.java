package prog3360.order_service.config;

// Assignment 4 - Part 1 
// Instruction - "forward the correlation ID as an X-Correlation-Id request header"
// Uses feign.RequestInterceptor because our project calls product-service via Feign client. (provided by spring-cloud-starter-openfeign)

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class FeignCorrelationIdInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            template.header("X-Correlation-Id", correlationId);
        }
    }
}

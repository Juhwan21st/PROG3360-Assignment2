package prog3360.product_service.config;

// Assignment 4 - Part 1 
// Instruction - Correlation ID requirements

// Ref: labs/week12/logging-lecture/src/main/java/com/example/logging_lecture/filter/LoggingFilter.java
//      - Full source directly from week12 lab (only package name changed)
// Ref: lectures/week12_Observability > Correlation IDs - concept explanation

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter implements Filter {

    // The HTTP header name we read from incoming requests
    private static final String HEADER = "X-Correlation-Id";

    // The MDC key that %X{correlationId} in the log pattern reads from
    private static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Step 1: Read the header or generate a new UUID
        String correlationId = httpRequest.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Step 2: Store in MDC
        // Every log line on this thread will automatically include [correlationId]
        // because of the %X{correlationId} token in application.properties
        MDC.put(MDC_KEY, correlationId);

        try {
            // Step 3: Continue to the controller
            chain.doFilter(request, response);
        } finally {
            // Step 4: ALWAYS clean up MDC
            // Web servers reuse threads - without this, correlationId leaks across requests
            MDC.remove(MDC_KEY);
        }
    }
}

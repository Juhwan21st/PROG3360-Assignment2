package prog3360.order_service.service;

import io.getunleash.Unleash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Feature Flag Service for Order Service
 * Encapsulates Unleash interactions and provides feature flag status checks
 */
@Service
public class FeatureFlagService {
    
    // SLF4J Logger pattern from Week 5 lab (event-message-demo)
    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagService.class);
    private final Unleash unleash;
    
    /**
     * Constructor injection of Unleash client
     */
    public FeatureFlagService(Unleash unleash) {
        this.unleash = unleash;
    }
    
    /**
     * Check if order notifications feature is enabled
     * Logs notifications when orders are created
     */
    public boolean isOrderNotificationsEnabled() {
        try {
            return unleash.isEnabled("order-notifications", false);
        } catch (Exception e) {
            logger.error("Unleash connection failed while checking order-notifications flag: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if bulk order discount feature is enabled
     * Provides 15% discount for orders with quantity > 5
     */
    public boolean isBulkOrderDiscountEnabled() {
        try {
            return unleash.isEnabled("bulk-order-discount", false);
        } catch (Exception e) {
            logger.error("Unleash connection failed while checking bulk-order-discount flag: {}", e.getMessage());
            return false;
        }
    }
    
}

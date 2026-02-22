package prog3360.product_service.service;

import io.getunleash.Unleash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Feature Flag Service for Product Service
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
     * Check if premium pricing feature is enabled
     * Provides 10% discount for premium users
     * 
     * @return true if premium-pricing flag is enabled, false otherwise
     */
    public boolean isPremiumPricingEnabled() {
        try {
            return unleash.isEnabled("premium-pricing", false);
        } catch (Exception e) {
            logger.error("Unleash connection failed while checking premium-pricing flag: {}", e.getMessage());
            return false;
        }
    }
    
}

package prog3360.order_service.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Unleash Configuration for Order Service
 * Configures Unleash client to connect to Unleash server
 */
@Configuration
public class UnleashConfigBean {
    
    /**
     * Configures and returns default Unleash client instance
     * for Order Service
     * 
     * @param apiUrl Unleash server API URL
     * @param appName Application name for identification
     * @param instanceId Unique instance identifier
     * @param apiToken Authentication token for API access
     * @return Configured Unleash client
     */
    @Bean
    public Unleash unleash(
            @Value("${unleash.api-url}") String apiUrl,
            @Value("${unleash.app-name}") String appName,
            @Value("${unleash.instance-id}") String instanceId,
            @Value("${unleash.api-token}") String apiToken
    ) {
        UnleashConfig config = UnleashConfig.builder()
                .unleashAPI(apiUrl)
                .appName(appName)
                .instanceId(instanceId)
                .apiKey(apiToken)
                .fetchTogglesInterval(5)
                .build();

        return new DefaultUnleash(config);
    }
}

package prog3360.order_service.presentation.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// '/version' endpoint for Blue-Green deployment demonstration (A3 Instruction Part 3).
//
//   - Blue pods receive APP_VERSION="v1-blue" from ConfigMap (envFrom).
//   - Green pods override APP_VERSION="v2-green" via Deployment env (env > envFrom).
//   - When the Service selector switches to green, /version returns "v2-green".
//   - On rollback (selector back to blue), /version returns "v1-blue" instantly.
@RestController
public class VersionController {

    // Injects APP_VERSION env var at startup. Falls back to "unknown" if not set.
    @Value("${APP_VERSION:unknown}")
    private String appVersion;

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        return Map.of(
                "service", "order-service",
                "version", appVersion
        );
    }
}

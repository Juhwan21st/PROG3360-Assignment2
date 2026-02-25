#!/bin/bash
# init-flags.sh - Initialize feature flags in Unleash via Admin API.

# Run this script after the Unleash server is healthy.
# in terminal: ./init-flags.sh

UNLEASH_URL="http://localhost:4242"

# Admin API token - matches INIT_ADMIN_API_TOKENS in docker-compose.yml.
ADMIN_TOKEN="*:*.unleash-insecure-admin-api-token"

# Reusable function to avoid repeating the same curl structure for each flag.
create_flag() {
    local FLAG_NAME=$1
    local DESCRIPTION=$2

    echo "--- Creating flag: $FLAG_NAME ---"
    curl -X POST "$UNLEASH_URL/api/admin/projects/default/features" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$FLAG_NAME\",
            \"description\": \"$DESCRIPTION\",
            \"type\": \"release\"
        }"
    echo ""
}

# Three feature flags
create_flag "premium-pricing" \
    "Product Service - GET /api/products/premium. When ON: Returns products with 10% discounted prices. When OFF: Returns products at regular prices."

create_flag "bulk-order-discount" \
    "Order Service - POST /api/orders. When ON and quantity > 5: Apply 15% discount to totalPrice. When OFF: Calculate totalPrice normally regardless of quantity."

create_flag "order-notifications" \
    "Order Service - POST /api/orders. When ON: Log notification message including order ID, product details, and total price. When OFF: Create order silently without notification logging."

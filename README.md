# PROG3360 Assignment 2: Feature Flag Development with Unleash

## Team Members

- Nathan Dinh
- Juhwan Seo

## Project Setup

1. Start all services:

```
docker compose up -d --build --wait
```

2. Initialize feature flags in Unleash:

```
bash scripts/init-flags.sh
```

3. Verify services are running:

```
curl http://localhost:8081/api/products
curl http://localhost:8082/api/orders
curl http://localhost:4242/health
```

## Links

- GitHub Repo: https://github.com/Juhwan21st/PROG3360-Assignment2

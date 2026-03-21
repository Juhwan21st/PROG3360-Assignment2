# PROG3360 Assignment 3:

# Prerequisite

## Mac OS Enviorment Setup

This is for macOS — points your terminal's Docker CLI at Minikube's internal Docker daemon so built images are available inside Minikube.

```zsh
eval $(minikube docker-env)
```

This is to unset minkube from docker

```zsh
eval $(minikube docker-env --unset)
```

## Image setup

```zsh
docker build -t product-service:1.0.0 ./product-service
docker build -t order-service:1.0.0 ./order-service
```

# Assignment 3

## Part 2

1. Deploy and verify all pods are running

**kubectl apply -f k8s**
Reads all YAML files in the k8s/ folder and creates/updates the resources in Kubernetes (Namespace, ConfigMaps, Deployments, Services).

**kubectl get pods -n prog3360-assignment3**
Lists all pods in the prog3360-assignment3 namespace so you can verify they are running.

```zsh
kubectl apply -f k8s/
kubectl get pods -n prog3360-assignment3
```

2. Show ReplicaSets created by each Deployment

```zsh
kubectl get replicasets -n prog3360-assignment3
```

3. Scale one Deployment from 2 to 4 replicas

```zsh
kubectl scale deployment product-service-blue --replicas=4 -n prog3360-assignment3
kubectl get pods -n prog3360-assignment3
```

4. Delete one running Pod manually

```zsh
# Get pod name first
kubectl get pods -n prog3360-assignment3

# Then delete one (replace with actual pod name)
kubectl delete pod product-service-xxxxx-xxxxx -n prog3360-assignment3
```

5. Show Kubernetes automatically creates a replacement

```zsh
# Watch pods in real time — you'll see a new one spin up immediately
kubectl get pods -n prog3360-assignment3 --watch
```

## Part 3

### Blue-Green Deployment

Blue-green deployment runs two identical environments simultaneously — blue (current live version) and green (new version). Traffic is controlled by the Service selector. Switching versions is instant with zero downtime.

Both `product-deployment.yaml` and `order-deployment.yaml` contain two Deployments each:

- **blue** — runs `1.0.0`, currently receiving live traffic
- **green** — runs `2.0.0`, deployed but idle

The Service selector determines which version gets traffic:

```yaml
selector:
  app: product-service
  version: blue # change to green to switch traffic
```

**Switch traffic to green:**

```zsh
kubectl patch service product-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"product-service","version":"green"}}}'

kubectl patch service order-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"order-service","version":"green"}}}'
```

**Rollback to blue:**

```zsh
kubectl patch service product-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"product-service","version":"blue"}}}'

kubectl patch service order-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"order-service","version":"blue"}}}'
```

### Rollout Commands

Use these to monitor and manage rolling updates within a single Deployment (e.g. if you update the image of blue).

**Check rollout status** — watch the update progress in real time:

```zsh
kubectl rollout status deployment/product-service-blue -n prog3360-assignment3
kubectl rollout status deployment/order-service-blue -n prog3360-assignment3
```

**View rollout history** — see all previous versions of a Deployment:

```zsh
kubectl rollout history deployment/product-service-blue -n prog3360-assignment3
kubectl rollout history deployment/order-service-blue -n prog3360-assignment3
```

**Rollback to previous version** — undoes the last image/config change to that Deployment:

```zsh
kubectl rollout undo deployment/product-service-blue -n prog3360-assignment3
kubectl rollout undo deployment/order-service-blue -n prog3360-assignment3
```

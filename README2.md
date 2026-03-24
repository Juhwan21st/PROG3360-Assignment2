# Prerequisite

## Environment Setup

Points your terminal's Docker CLI at Minikube's internal Docker daemon so built images are available inside Minikube without pushing to a registry.

**macOS / Linux**

```zsh
eval $(minikube docker-env)
```

**Windows (PowerShell)**

```powershell
& minikube -p minikube docker-env --shell powershell | Invoke-Expression
```

To unset and point back at your local Docker daemon:

**macOS / Linux**

```zsh
eval $(minikube docker-env --unset)
```

**Windows (PowerShell)**

```powershell
& minikube -p minikube docker-env --unset --shell powershell | Invoke-Expression
```

> Note: this only affects the current terminal session. Run it again in each new terminal.

## Image setup

**Blue and Green Deployment Images**

```zsh
docker build -t product-service:1.0.0 ./product-service
docker build -t order-service:1.0.0 ./order-service
docker build -t product-service:2.0.0 ./product-service
docker build -t order-service:2.0.0 ./order-service
```

---

# PROG3360 Assignment 3: Software Delivery and Release Management

## Part 1

### Overview

All resources are deployed into the `prog3360-assignment3` namespace. The following Kubernetes manifests are in the `k8s/` directory:

| File                      | Kind                | Purpose                                             |
| ------------------------- | ------------------- | --------------------------------------------------- |
| `namespace.yaml`          | Namespace           | Isolates all resources under `prog3360-assignment3` |
| `configmap.yaml`          | ConfigMap (×2)      | Stores application config for each service          |
| `product-deployment.yaml` | Deployment (×2)     | Blue/green deployments for product-service          |
| `product-service.yaml`    | Service (ClusterIP) | Internal access to product-service on port 8081     |
| `order-deployment.yaml`   | Deployment (×2)     | Blue/green deployments for order-service            |
| `order-service.yaml`      | Service (NodePort)  | External access to order-service on port 30082      |

### Namespace

Creates a dedicated namespace so all assignment resources are isolated from default:

```yaml
# k8s/namespace.yaml
name: prog3360-assignment3
```

### ConfigMap

Two ConfigMaps store per-service configuration — no hardcoded values in the Deployment specs:

- `application-properties-product-service` — app name, DB URL, server port `8081`
- `application-properties-order-service` — app name, DB URL, server port `8082`, downstream product-service URL `http://product-service:8081`

### Product Service Deployment

- 2 replicas (`product-service-blue` runs `1.0.0`, `product-service-green` runs `2.0.0`)
- Container port: `8081`
- Labels: `app: product-service`, `version: blue|green`
- Pulls config from `application-properties-product-service` ConfigMap via `envFrom`

### Order Service Deployment

- 2 replicas (`order-service-blue` runs `1.0.0`, `order-service-green` runs `2.0.0`)
- Container port: `8082`
- Labels: `app: order-service`, `version: blue|green`
- Pulls config from `application-properties-order-service` ConfigMap via `envFrom`

### Services

**product-service** — ClusterIP (internal only):

```yaml
type: ClusterIP
port: 8081 → targetPort: 8081
selector: app: product-service, version: blue
```

**order-service** — NodePort (reachable from outside the cluster via Minikube):

```yaml
type: NodePort
port: 8082 → targetPort: 8082 → nodePort: 30782
selector: app: order-service, version: blue
```

### Accessing order-service from a browser

NodePort exposes the service on the node's IP at port `30082`. On Linux this works directly, but on macOS/Windows the minikube VM's IP is not routable from the host.

> **macOS note:** On macOS, the minikube node IP (e.g. `192.168.49.2`) is not directly reachable from your browser because minikube runs inside a VM. The command above gives you a `http://127.0.0.1:<port>` URL that works around this.

> **Windows note:** On Windows, minikube runs inside a VM (Hyper-V or VirtualBox) and the node IP is similarly not directly reachable. Use one of the options below instead of navigating to the node IP directly.

**Option 1 — minikube service (recommended)**

macOS / Linux:

```zsh
minikube service order-service -n prog3360-assignment3 --url
```

Windows (PowerShell):

```powershell
minikube service order-service -n prog3360-assignment3 --url
```

Returns a `http://127.0.0.1:<port>` URL you can open directly in a browser. On Windows, minikube may automatically open the URL in your default browser.

**Option 2 — kubectl port-forward**

macOS / Linux:

```zsh
kubectl port-forward svc/order-service 8082:8082 -n prog3360-assignment3
```

Windows (PowerShell):

```powershell
kubectl port-forward svc/order-service 8082:8082 -n prog3360-assignment3
```

Then open `http://localhost:8082` in your browser.

> **Note:** This is the only option that works for `product-service` since it is type `ClusterIP` (no NodePort). Options 1 and 3 require a NodePort or LoadBalancer service type. Use `kubectl port-forward svc/product-service 8081:8081 -n prog3360-assignment3` and open `http://localhost:8081`.

**Option 3 — minikube tunnel**

macOS / Linux — run in a separate terminal (requires `sudo`):

```zsh
minikube tunnel
```

Windows (PowerShell) — run in a separate terminal as Administrator:

```powershell
minikube tunnel
```

This adds a network route so that the minikube node IP becomes reachable. You can then open `http://192.168.49.2:30082` directly. The route is removed when you stop the tunnel.

---

## Part 2

1. Deploy and verify all pods are running

```zsh
minikube start
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/
kubectl get pods -n prog3360-assignment3
```

> You have to run namespace.yaml first

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

---

### Step 1 — Show original version (blue/1.0.0) is live

```zsh
kubectl get pods -n prog3360-assignment3 --show-labels
kubectl get service product-service -n prog3360-assignment3
```

Confirms blue pods are running and the Service selector points to `version: blue`.

---

### Step 2 — Verify green (2.0.0) is deployed and ready

```zsh
kubectl rollout status deployment/product-service-green -n prog3360-assignment3
kubectl rollout status deployment/order-service-green -n prog3360-assignment3
```

Both green Deployments must report `successfully rolled out` before switching traffic.

View rollout history for the green Deployment:

```zsh
kubectl rollout history deployment/product-service-green -n prog3360-assignment3
kubectl rollout history deployment/order-service-green -n prog3360-assignment3
```

---

### Step 3 — Switch traffic to green (2.0.0)

```zsh
kubectl patch service product-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"product-service","version":"green"}}}'

kubectl patch service order-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"order-service","version":"green"}}}'
```

---

### Step 4 — Verify new version is serving

Confirm the Service selector now targets green:

```zsh
kubectl get service product-service -n prog3360-assignment3 \
  -o jsonpath='{.spec.selector}'
kubectl get service order-service -n prog3360-assignment3 \
  -o jsonpath='{.spec.selector}'
```

---

### Bonus — Rollback to blue (1.0.0)

```zsh
kubectl patch service product-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"product-service","version":"blue"}}}'

kubectl patch service order-service -n prog3360-assignment3 \
  -p '{"spec":{"selector":{"app":"order-service","version":"blue"}}}'
```

Verify rollback:

```zsh
kubectl get service product-service -n prog3360-assignment3 -o jsonpath='{.spec.selector}'
```

# Notes

Cluster
└── Namespace (prog3360-assignment3)
├── Node (VM/machine)
│ └── Pod (managed by a Deployment)
│ └── Container (your app)
│
└── Service (routes traffic to Pods)
├── port (receives traffic from other pods)
└── targetPort (forwards traffic to Container)

- apiVersion: v1 — which version of the Kubernetes API to use for this resource
- kind: ConfigMap — the type of Kubernetes resource you're creating
- metadata is identifying information about the resource — basically a label/name tag.

- A Cluster contains one or more nodes.
  - Each node is a VM or physical machine that runs Pods.
    - These pods contain containers, which are the actual services you are trying to deploy.
      - Pods are managed by a Deployment, which ensures the desired number of replicas are always running.
- Pods are used for grouping containers.
- Redundancy is achieved by the Deployment running multiple pod replicas.

## ConfigMap

- data is where you store the actual configuration values — key value pairs your containers can read as environment variables.

## NameSpace

- Namespace is a way to logically group and isolate within a cluster

## Deployment

- Replicas allows us defined how many copies of a pod to create for redundancy

- Selector matchLabels tells the Deployment which pods it manages
  - app and version (blue) are the labels used to identify/find these pods

- template the blueprint for each pod the Deployment creates
  - metadata.labels includes tags which applied to each pod

- containers is a list of containers to run inside the pod
  - name: order-service — name of the container
  - image: order-service:1.0.0 — the Docker image to use
  - containerPort: 8082 — the port your app listens on inside the container (this becomes the targetPort)
  - envFrom — inject environment variables into the container
    - configMapRef: application-properties-order-service — pull those env vars from this specific ConfigMap

## Service

Requester → Service (port) → Container (targetPort)

- NodePort opens a port for outside communication to the cluster

- Port is on the service. This is the port number other pods use to reach the Service.

- TargetPort is on the container and is where the service uses this to forward traffic to your app.
  - This is important when we have multiple containers running within a pod.

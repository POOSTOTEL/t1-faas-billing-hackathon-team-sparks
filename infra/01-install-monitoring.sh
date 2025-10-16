#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VALUES_FILE="$SCRIPT_DIR/manifests/promstack-values.yaml"

if [ ! -f "$VALUES_FILE" ]; then
  echo "❌ promstack-values.yaml not found at $VALUES_FILE"
  exit 1
fi

echo "Adding Prometheus Community Helm repo..."
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
timeout 100s sleep 200
helm repo update
timeout 100s sleep 200
echo "Installing kube-prometheus-stack with Team Sparks values..."
helm upgrade --install knative prometheus-community/kube-prometheus-stack \
  --namespace observability --create-namespace \
  -f "$VALUES_FILE"
timeout 100s sleep 200
echo "Applying Knative Serving monitors and dashboards..."
kubectl apply -f https://raw.githubusercontent.com/knative-extensions/monitoring/main/config/serving-monitors.yaml

echo "Waiting for Prometheus and Grafana to become ready..."
kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=prometheus -n observability --timeout=180s
kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=grafana -n observability --timeout=180s

echo "✅ Monitoring stack is ready and accessible via LoadBalancer."

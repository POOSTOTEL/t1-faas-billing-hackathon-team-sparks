#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CM_FILE="$SCRIPT_DIR/manifests/config-observability.yaml"

echo "Applying config-observability to enable Prometheus metrics..."
kubectl apply -f "$CM_FILE"

echo "Restarting Knative Serving components to apply config..."
kubectl rollout restart deployment -n knative-serving activator autoscaler controller

sleep 10
kubectl rollout status deployment activator -n knative-serving --timeout=60s
kubectl rollout status deployment autoscaler -n knative-serving --timeout=60s
#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Deploying loadgen service..."
kubectl apply -f "$SCRIPT_DIR/manifests/loadgen-ksvc.yaml"

echo "Deploying billing frontend..."
kubectl apply -f "$SCRIPT_DIR/manifests/billing-frontend-ksvc.yaml"

echo "✅ Team Sparks services deployed."
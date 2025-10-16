#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🔧 Step 1: Official Knative Setup (from hackathon organizers)"
"$SCRIPT_DIR/00-run-official-knative.sh"

echo "📊 Step 2: Install Prometheus + Grafana"
"$SCRIPT_DIR/01-install-monitoring.sh"

echo "🔭 Step 3: Enable Knative observability"
"$SCRIPT_DIR/02-configure-observability.sh"

echo "🚀 Step 4: Deploy custom services"
"$SCRIPT_DIR/03-deploy-services.sh"

echo
echo "🎉 ALL DONE! — Team Sparks FaaS Billing Platform"
echo "👉 Grafana:    http://localhost:3000 (admin / prom-operator)"
echo "👉 Prometheus: http://localhost:9090"
echo "👉 LoadGen:    $(kubectl get ksvc loadgen -o jsonpath='{.status.url}' 2>/dev/null || echo 'deploying...')"
echo "👉 Frontend:   $(kubectl get ksvc billing-frontend -o jsonpath='{.status.url}' 2>/dev/null || echo 'deploying...')"
#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/.."

# Убедимся, что используется контекст Docker Desktop
kubectl config use-context docker-desktop 2>/dev/null || true

# Запускаем ОФИЦИАЛЬНЫЙ скрипт от организаторов хакатона
echo "Running official Knative setup script (install_knative_1_17_kourier.sh)..."
"$ROOT_DIR/install_knative_1_17_kourier.sh"

echo "✅ Official Knative + Kourier + echo service deployed."
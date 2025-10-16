#!/bin/bash
set -e

echo "📦 Building loadgen-service..."
cd apps/loadgen-service
./mvnw clean package -DskipTests
docker build -t alexpoostotel/loadgen-service:0.1 .
docker push alexpoostotel/loadgen-service:0.1

echo "📦 Building billing-frontend..."
cd ../faas-billing-frontend
npm install
npm run build
docker build -t alexpoostotel/billing-frontend:0.1 .
docker push alexpoostotel/billing-frontend:0.1

echo "✅ All images built and pushed!"
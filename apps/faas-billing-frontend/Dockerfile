# apps/faas-billing-frontend/Dockerfile
FROM node:18 AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80

COPY --from=builder /app/index.html /usr/share/nginx/html/

COPY nginx.conf /etc/nginx/conf.d/default.conf
# ==========================================================
# RESTAURANT MANAGEMENT - ANGULAR FRONTEND
# ==========================================================

# ----------------------------------------------------------
# BUILD STAGE
# ----------------------------------------------------------

FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first for better Docker layer caching
COPY package*.json ./

# Install exact dependencies
RUN npm ci

# Copy Angular source
COPY . .

# Production build
RUN npm run build -- --configuration production


# ----------------------------------------------------------
# NGINX STAGE
# ----------------------------------------------------------

FROM nginx:1.27-alpine

# Remove default Nginx configuration
RUN rm -f /etc/nginx/conf.d/default.conf

# Copy Restaurant Management Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Angular production build
COPY --from=builder /app/dist/restaurant-ui /usr/share/nginx/html

# HTTP
EXPOSE 80

# HTTPS
EXPOSE 443

# Container health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost/health || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
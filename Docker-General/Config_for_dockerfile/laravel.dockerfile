# Stage 1: Composer
FROM composer:lts as composer
WORKDIR /app
COPY . .
RUN composer install # --no-dev --optimize-autoloader

# Stage 2: Node
FROM node:lts as node
WORKDIR /app
#COPY . .
COPY --from=composer /app/ /app/
RUN npm install
RUN npm run build

# Stage 3: PHP with Extensions
FROM php:8.2-apache-bullseye as php
WORKDIR /var/www
COPY --from=node /app/ /var/www/
COPY sites-available/ /etc/apache2/sites-available/
RUN php artisan key:generate --force
RUN php artisan config:clear && php artisan route:clear && php artisan view:clear && php artisan optimize:clear
RUN chmod -R 777 /var/www/storage
RUN chmod -R 777 /var/www/bootstrap
RUN a2enmod rewrite
USER root
EXPOSE 80
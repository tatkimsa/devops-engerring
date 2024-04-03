FROM ${NGINX}
RUN rm -rf /usr/share/nginx/html/*
# Copy the output from Stage 1 to replace the default nginx contents.
COPY ./dist/* /usr/share/nginx/html/
# Expose port 80
EXPOSE 80
# Start Nginx and keep it running in the foreground
CMD ["nginx", "-g", "daemon off;"]
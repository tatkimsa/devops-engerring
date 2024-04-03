FROM ${NODEJS18}
# set app working directory /app
WORKDIR /app
# copy the app from builder stage
COPY ./.output ./.output
COPY ./public ./public
COPY ./node_modules ./node_modules
COPY ./package.json ./package.json
EXPOSE 3000
# start the application
CMD ["node", ".output/server/index.mjs"]
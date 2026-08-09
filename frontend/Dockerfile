# Stage 1: build the static site with Node — discarded after this stage, none of
# node_modules or the build tooling ends up in the final image.
FROM node:20 AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Stage 2: just nginx + the built static files from stage 1.
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

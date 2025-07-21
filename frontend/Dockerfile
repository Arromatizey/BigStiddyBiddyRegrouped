FROM node:18

# Installer Angular CLI globalement
RUN npm install -g @angular/cli

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# Expose le port 4200 par défaut utilisé par ng serve
EXPOSE 4200

# Lance la commande ng serve avec host 0.0.0.0 pour être accessible hors container
CMD ["ng", "serve", "--host", "0.0.0.0"]


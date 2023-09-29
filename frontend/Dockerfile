FROM node:18

WORKDIR /app

COPY package*.json /app

RUN npm i -f --silence

COPY . /app

EXPOSE 3000

CMD ["npm", "run"]

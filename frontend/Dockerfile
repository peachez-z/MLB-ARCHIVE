FROM node:18-alpine

WORKDIR /app

COPY package*.json /app

# .env 파일을 컨테이너로 복사
COPY .env /app/.env

RUN npm install -f

COPY ./ /app

EXPOSE 3000

CMD ["npm","run","dev"]

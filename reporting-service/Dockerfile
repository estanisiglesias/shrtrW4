FROM node:16

WORKDIR app/
COPY package-lock.json package-lock.json
COPY package.json package.json

COPY ./src src

RUN npm install
CMD ['npm', 'run', 'start']
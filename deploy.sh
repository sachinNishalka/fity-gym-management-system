#!/bin/bash
docker stop backend || true
docker rm backend || true
docker build -t backend:latest .
docker run -d \
  --name backend \
  --network app-network \
  -p 8080:8080 \
  backend:latest
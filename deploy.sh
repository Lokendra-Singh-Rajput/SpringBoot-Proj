#!/usr/bin/env bash
set -e

if [ ! -f .env ]; then
  echo "Error: .env file is missing. Please create .env before running deploy.sh"
  exit 1
fi

docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml up -d --remove-orphans
docker image prune -f

echo "Deployment successful! Current service status:"
docker compose -f docker-compose.prod.yml ps

#!/bin/bash

# Read values from YAML using awk
MINIO_USERNAME=$(awk '/minio:/, /password:/ {if ($1 == "username:") print $2}' src/main/resources/config.yaml | tr -d '[:space:]')
MINIO_PASSWORD=$(awk '/minio:/, /password:/ {if ($1 == "password:") print $2}' src/main/resources/config.yaml | tr -d '[:space:]')

# Print username and password
echo "MINIO_USERNAME: $MINIO_USERNAME"
echo "MINIO_PASSWORD: $MINIO_PASSWORD"

# Run MinIO container with dynamic credentials
docker run -d --name minio \
  -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=$MINIO_USERNAME \
  -e MINIO_ROOT_PASSWORD=$MINIO_PASSWORD \
  -v minio-data:/data \
  minio/minio server /data --console-address ":9001"

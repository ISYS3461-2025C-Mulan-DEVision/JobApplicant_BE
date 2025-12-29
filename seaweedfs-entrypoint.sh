#!/bin/sh
# SeaweedFS Entrypoint Script
# Runs both weed server and weed admin services

# Fix data directory ownership for seaweed user
chown -R 1000:1000 /data

# Start weed server in background
# -dir: where volume data is stored.
weed server -dir=/data -master.volumeSizeLimitMB=1024 -s3 -s3.config=/etc/seaweedfs/config.json &
SERVER_PID=$!

# Give server time to start
sleep 2

# Start weed admin service with authentication
# Default credentials: admin / admin (change in production!)
weed admin -port=16625 -masters=localhost:9333 -adminUser=admin -adminPassword=admin

# Keep the container running and handle signals
wait $SERVER_PID

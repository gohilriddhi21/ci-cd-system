#!/bin/bash

# Pull the mongo container
docker run -d -p 27017:27017 --name my-mongodb mongo:latest
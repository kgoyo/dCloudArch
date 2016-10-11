#!/bin/bash

docker-compose -f docker-compose-mq.yml up -d

# Scale number of skycave services (Starts 3 skycave daemons)
docker-compose -f docker-compose-mq.yml scale skycave=3
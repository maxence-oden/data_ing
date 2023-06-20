#!/bin/bash

cd kafka_2.13-3.4.0

echo "Launch brooker"
bin/kafka-server-start.sh config/server.properties

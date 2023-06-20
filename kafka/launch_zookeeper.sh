#!/bin/bash

cd kafka_2.13-3.4.0

echo -e "Launching zookeeper"
bin/zookeeper-server-start.sh config/zookeeper.properties

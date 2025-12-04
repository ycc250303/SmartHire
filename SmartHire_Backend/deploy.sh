#!/bin/bash

cd /opt/SmartHire/SmartHire_Backend || exit

echo "拉取最新镜像..."
docker-compose pull

echo "启动/重启容器..."
docker-compose up -d --remove-orphans

echo "清理旧镜像..."
docker image prune -f

echo "部署完成！"

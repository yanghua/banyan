#! /bin/sh

sudo service messagebus-server stop
sudo rabbitmqctl stop_app
sudo rabbitmqctl reset
sudo rabbitmqctl start_app
sudo service messagebus-server start
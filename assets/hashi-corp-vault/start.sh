#!/bin/bash

echo -n "Choose option:
    1.Start
    2.Clean Data
    3.Stop all
: "
read OPTION

if [ $OPTION -eq 1 ]
then
  sudo systemctl start docker
  sudo docker stop $(sudo docker ps -a -q)
  sudo docker-compose up
fi

if [ $OPTION -eq 2 ]
then
  sudo docker-compose down --volumes
fi

if [ $OPTION -eq 3 ]
then
  sudo docker stop $(sudo docker ps -a -q)
fi

#!/usr/bin/env bash  

# PARAMS
USER=root
PID_PATH=/var/run/
PROJECT_ROOT=/opt/JSS
JAR_FILE=/opt/JSS/dist/jss.jar
MODE=""
EXCEPTION="Usage: jss {start|stop|restart} {M1|M2|M3}"
 
M1=HTTP_REQUEST_TIMER
M2=HTTP_REDIRECT_LISTENER
M3=MEMCACHED_LISTENER_TIMER
 
RETVAL=0
 
# PREPARE PARAMS
case "$2" in
    M1)
      MODE=$M1
  ;;
    M2)
      MODE=$M2
  ;;
    M3)
      MODE=$M3
  ;;
    *)
      echo $EXCEPTION
      exit 1
  ;;
esac
PID_FILE=$PID_PATH$MODE.pid
 
# PREPARE JAR FILE
if [ ! -e $JAR_FILE ]
then
    cd $PROJECT_ROOT
    ant
fi
 
# DEFINE STARTUP & ENDUP
start() {
      echo -n "Starting JSS, MODE: $MODE"
      start-stop-daemon --start --quiet -b -m -p $PID_FILE --chuid $USER --exec /usr/bin/java -- -jar $JAR_FILE $MODE
      RETVAL=$?
      echo "DONE."
}
stop() {
      echo -n "Stopping JSS, MODE: $MODE"
      start-stop-daemon --stop --quiet -p $PID_FILE
      rm -f $PID_FILE
      RETVAL=$?
      echo "DONE."
}
 
# COMMANDS
case "$1" in
    start)
      start
  ;;
    stop)
      stop
  ;;
    restart)
      stop
        sleep 2
      start
  ;;
    *)
      echo $EXCEPTION
      exit 1
  ;;
esac
 
exit $RETVAL
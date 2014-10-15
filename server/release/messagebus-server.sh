#!/usr/bin/env bash  

# PARAMS
NAME=messagebus-server
USER=root
PID_PATH=/var/run
MESSAGEBUS_SERVER_HOME=/usr/local/messagebus-server
JAR_FILE=$MESSAGEBUS_SERVER_HOME/start.jar
LOG_PROPERTY_PATH=$MESSAGEBUS_SERVER_HOME/conf/log4j.properties
EXCEPTION="Usage: messagebus-server {start|stop|restart|check|status}"
RETVAL=0
MESSAGEBUS_SERVER_PID=$PID_PATH/$NAME.pid
MESSAGEBUS_SERVER_STOP_PID=$PID_PATH/$NAME_stop.pid

usage()
{
    echo $EXCEPTION
    exit 1
}

[ $# -gt 0 ] || usage

running()
{
  if [ -f "$1" ]
  then
    local PID=$(cat "$1" 2>/dev/null) || return 1
    kill -0 "$PID" 2>/dev/null
    return
  fi
  rm -f "$1"
  return 1
}
 
# DEFINE STARTUP & ENDUP
start() {
      echo -n "Starting messagebus server ..."

      if running $MESSAGEBUS_SERVER_PID
      then
        echo "Already running $(cat $MESSAGEBUS_SERVER_PID)"
        exit 1
      fi

      start-stop-daemon -S -p$MESSAGEBUS_SERVER_PID -c$USER -b -m -a /usr/bin/java -- -jar $JAR_FILE cmd=start serverLog4jPropertyPath=$LOG_PROPERTY_PATH
      RETVAL=$?
      echo "started."
}
stop() {
      echo -n "Stopping messagebus server ..."
      #use a new pid to specify stop action, then kill it right now!
      start-stop-daemon -S -p$MESSAGEBUS_SERVER_STOP_PID -c$USER -b -m -a /usr/bin/java -- -jar $JAR_FILE cmd=stop serverLog4jPropertyPath=$LOG_PROPERTY_PATH
      sleep 3

      if running "$MESSAGEBUS_SERVER_PID"
      then
        start-stop-daemon -K -p$MESSAGEBUS_SERVER_PID -a /usr/bin/java -s KILL
        rm -f $MESSAGEBUS_SERVER_PID
      fi

      if running "$MESSAGEBUS_SERVER_STOP_PID"
      then
        start-stop-daemon -K -p$MESSAGEBUS_SERVER_STOP_PID -a /usr/bin/java -s KILL
        rm -f $MESSAGEBUS_SERVER_STOP_PID
      fi

      RETVAL=$?
      echo "stopped."
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
        sleep 10
      start
  ;;
    check|status)
      echo "Checking arguments to messagebus-server: "
      echo "MESSAGEBUS_SERVER_HOME          =  $MESSAGEBUS_SERVER_HOME"
      echo "JAR_PATH                        =  $JAR_FILE"
      echo "MESSAGEBUS_SERVER_PID           =  $MESSAGEBUS_SERVER_PID"
      echo "MESSAGEBUS_SERVER_LOG_CONFIG    =  $LOG_PROPERTY_PATH"

      if running "$MESSAGEBUS_SERVER_PID"
      then
        echo "message bus server running pid  =  $(< "$MESSAGEBUS_SERVER_PID")"
        exit 0
      fi
      exit 1
    ;;
    *)
      echo $EXCEPTION
      exit 1
  ;;
esac
 
exit $RETVAL
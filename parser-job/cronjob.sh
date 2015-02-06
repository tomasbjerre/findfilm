#!/bin/sh
#
# Example cronjob:
#    */5 2,3 * * * /home/findfilm/workspace/findfilm/parser-job/cronjob.sh
#

logfile=/home/findfilm/workspace/findfilm/parser-job/cronjob.log
filmparserjob=/home/findfilm/workspace/findfilm/parser-job/target/findfilm-parser-job-0.0.1-SNAPSHOT-jar-with-dependencies.jar
storagetype=api

echo Checking if running
logtime=`date -d @$(stat -c%Y $logfile) +"%s"`
currtime=`date +"%s"`
difftime=`echo "($currtime - $logtime)" | bc -l`
if [ "$difftime" -lt 300 ]; then
 echo Logfile changed $difftime seconds ago, looks ok
 exit
fi

echo Cleaning
killall java
for KILLPID in `ps ax | grep 'iceweasel' | awk ' { print $1;}'`; do
 kill -9 $KILLPID;
done

echo Starting
date > $logfile
export DISPLAY=:0
java -jar $filmparserjob -storageType $storagetype >> $logfile 2>&1
date >> $logfile

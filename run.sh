#! /bin/bash -e

export JAVA_HOME=/opt/jdk8/jdk1.8.0_71/

mvn clean package -P alpha

cd target && nohup java -jar patient-monitor-1.0-SNAPSHOT.jar > sysout.log &

cd ..

echo 'patient monitor started'

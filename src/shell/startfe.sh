#!/bin/sh

app=`dirname $0`
glib=$app/libs

mypath=.:$prg/lib/tools.jar:$app/libs/fep-common.jar:$app/fep-fe.jar

opt=${FE_OPTS:-"-Xms256m -Xmx2048m"}
main=com.hzjbbis.fk.fe.Application

$JAVA_HOME/bin/java -version

export LANG=zh_CN.GBK
export _JAVA_SR_SIGNUM=12
$JAVA_HOME/bin/java $opt -classpath $mypath $main -DFE >/dev/null 2>&1 

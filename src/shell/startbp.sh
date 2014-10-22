#!/bin/sh

app=`dirname $0`
glib=$app/libs

mypath=.:$prg/lib/tools.jar:$app/libs/fep-common.jar:$app/fep-bp.jar

opt=${BP_OPTS:-"-Xms256m -Xmx1024m"}
main=com.hzjbbis.fk.bp.Application

$JAVA_HOME/bin/java -version

export LANG=zh_CN.GBK
export _JAVA_SR_SIGNUM=12
$JAVA_HOME/bin/java $opt -classpath $mypath $main -DBP >/dev/null 2>&1 

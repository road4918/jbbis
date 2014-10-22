#!/bin/sh

app=`dirname $0`
glib=$app/libs

mypath=.:$prg/lib/tools.jar:$app/libs/fep-common.jar:$app/fep-gate.jar

opt="-Xms256m -Xmx960m"
main=com.hzjbbis.fk.gate.Application

$JAVA_HOME/bin/java -version

#export LANG=zh_CN.GBK
$JAVA_HOME/bin/java $opt -classpath $mypath $main -DGATE >/dev/null 2>&1 

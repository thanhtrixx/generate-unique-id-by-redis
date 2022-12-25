#!/bin/sh

while getopts t:l:s:p: flag
do
  case "${flag}" in
    t) number_thread=${OPTARG};;
    l) number_loop=${OPTARG};;
    s) sleep_ms=${OPTARG};;
    p) number_process=${OPTARG};;
  esac
done
echo "Number thread: $number_thread";
echo "Number loop: $number_loop";
echo "Waiting next process in: $sleep_ms";
echo "Number process: $number_process";

./gradlew clean build

rm -f ids result-*

for i in `seq 1 $number_process`
do
echo Run $i;
time java -jar build/libs/redis-0.0.1-SNAPSHOT.jar $number_thread $number_loop --reserveCount=100000 --logging.level.root=INFO > "result-$i" &
sleep $sleep_ms
done

for job in `jobs -p`
do
echo Waiting $job
    wait $job || let "FAIL+=1"
done

wc -l ./ids

sort ids | uniq -c | grep -v '^ *1 '

echo Done

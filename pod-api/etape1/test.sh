#!/bin/bash
java Test 0 &
sleep 1
for i in {1..90}
do 
    java Test $i &
done
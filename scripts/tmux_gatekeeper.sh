#!/bin/bash
for (( c=1; c<=$1; c++ ))
do
   echo "starting client $c"
   tmux new-window -d -n "n$c" scrips/gnode_uuid.sh
done

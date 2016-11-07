#!/bin/bash
for (( c=1; c<=$1; c++ ))
do
   echo "submitting client $c"
   qsub -cwd scrips/gnode_uuid.sh
done
 

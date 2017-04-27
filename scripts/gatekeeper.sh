#!/bin/sh

# Define varibales
JOBS_TXT=jobIDs$(date +"%H%M%S").txt
JOBS_TXT_DIR=./jobs/
GRIDLAB=""
JDL_FILE=".tmp.jdl";
JDL="Executable = \"start.sh\";\n\
Arguments = \"--user {{USER}} --password {{PASSWORD}}\";\n\
StdOutput = \"std.out\";\n\
StdError = \"std.err\";\n\
InputSandbox = {\"start.sh\", \"gnode.jar\"};\n\
OutputSandbox = {\"std.out\", \"std.err\"};";

#####
# Print out usage
usage() {
    echo "Usage: runMutlble [wn-count] [--gridlab | --cancel]";
    echo "Submits mutible jobs in the grid";
    echo "Parameters:";
    echo "  wn-count    Number of Worker-Nodes [0-9]+";
    echo "  --cancel    Cancel all sumited jobs"
    echo "  --gridlab   Submit jobs only to gridLab";
    echo "  -h --help   Print out usage";
}

#####
# Get the right user number
get_name() {
    if [ $1 -lt 10 ]; then
       echo "00$1";
       return $1;
    fi;

    if [ $1 -lt 100 ]; then
        echo "0$1";
        return $1;
    fi;
    echo $1;
}

#####
# Cancel submited jobs
cancel_jobs() {
    for JOB_FILE in `ls $JOBS_TXT_DIR`; do
        glite-wms-job-cancel -i $JOBS_TXT_DIR/$JOB_FILE && \
        rm  -i $JOBS_TXT_DIR/$JOB_FILE;
    done;
    exit 0;
}

#####
# Create temporary .jdl file
create_jdl() {
    NUMBER=`get_name $1`;
    # "grid-xmpp-user-001" --password "node-002"
    echo -e $JDL | sed "s/{{USER}}/grid-xmpp-user-$NUMBER/g" > $JDL_FILE;
    cat $JDL_FILE | sed "s/{{PASSWORD}}/node-$NUMBER/g" > $JDL_FILE.tmp \
        && mv $JDL_FILE{.tmp,};
}

#####
# Submit jobs to grid
submit_jobs() {
    for((i=1; i<=$1; i++));  do
        create_jdl $i
        glite-wms-job-submit $GRIDLAB \
            -o $JOBS_TXT_DIR$JOBS_TXT -a $JDL_FILE > /dev/null;

        rm $JDL_FILE;

        # Print status
        echo $i $1  | awk '{printf "\r%i of %i jobs submitted (%i%%)", \
            $1, $2, ($1*100/$2)}';
    done;
    echo -e "\nDone!";
}

# Creader directory for job id files
if [ ! -d "$JOBS_TXT_DIR" ]; then
    mkdir $JOBS_TXT_DIR;
fi;

#####
# Analyse command arguments
for ARG in $*; do
    case $ARG in
        -h|--help) usage; exit 0;;
        '--cancel') cancel_jobs;;
        '--gridlab') GRIDLAB='-r grid-cr9.desy.de:8443/cream-pbs-desy';;
        [0-9]*) submit_jobs $ARG;;
        *) echo "Unknown argumant $ARG"; exit 1;;
    esac;
done;

#! /bin/sh

EJABBERD_COMMAND=/usr/sbin/ejabberdctl;
EJABBERD_NODE="grischa.htw-berlin.de";
EJABBERD_PASSWORD="node";

if [ $# -le 1 ]; then 
    echo "You have to set the number of users";
    echo "Usage: "
    echo " $0 start_number end_number"
    exit 1;
fi;

create_user() {
    # $EJABBERD_COMMAND register \
    echo "grid-xmpp-user-$1" "$EJABBERD_NODE" "$EJABBERD_PASSWORD-$1";
}

for((i=$1; i<=$2; i++)); do
    
    if [ $i -lt 10 ]; then
        create_user "00$i";
        continue;
    fi;
    
    if [ $i -lt 100 ]; then
        create_user "0$i";
        continue;
    fi;
    
    create_user $i
done;


- resources
  * http://www.eu-emi.eu/products/-/asset_publisher/1gkD/content/emi-ui-2
      - https://www.gridpp.ac.uk/wiki/EMITarball
  * http://operations-portal.egi.eu/vo/search

# htw grid login host

* grid-login.f4.htw-berlin.de


# running a job


## creating grid proxy

```bash
$ voms-proxy-init --debug --voms dech
Looking for VOMS AA certificates in /etc/grid-security/vomsdir...
Looking for LSC information in /etc/grid-security/vomsdir/dech...
Loaded LSC information from file /etc/grid-security/vomsdir/dech/glite-io.scai.fraunhofer.de.lsc: LSCFile [filename=/etc/grid-security/vomsdir/dech/glite-io.scai.fraunhofer.de.lsc, vo=dech, hostname=glite-io.scai.fraunhofer.de, certChainDescription=[/C=DE/O=GermanGrid/OU=Fraunhofer SCAI/CN=host/glite-io.scai.fraunhofer.de, /C=DE/O=GermanGrid/CN=GridKa-CA]]
Looking for VOMS AA certificates in /etc/grid-security/vomsdir/dech...
Looking for VOMSES information in /etc/vomses...
Loaded vomses information 'VOMSServerInfo [alias=dech, voName=dech, URL=voms://glite-io.scai.fraunhofer.de:15000, vomsServerDN=/C=DE/O=GermanGrid/OU=Fraunhofer SCAI/CN=host/glite-io.scai.fraunhofer.de]' from /etc/vomses.
Looking for user credentials in [/home/grid/.globus/userkey.pem, /home/grid/.globus/usercert.pem]...
Enter GRID pass phrase for this identity:
Credentials loaded successfully [/home/grid/.globus/userkey.pem, /home/grid/.globus/usercert.pem]
Loading CA Certificate /etc/grid-security/certificates/dd4b34ea.0.
Loading EACL namespace (signing_policy) /etc/grid-security/certificates/dd4b34ea.signing_policy.
Contacting glite-io.scai.fraunhofer.de:15000 [/C=DE/O=GermanGrid/OU=Fraunhofer SCAI/CN=host/glite-io.scai.fraunhofer.de] "dech"...
Sent HTTP request for https://glite-io.scai.fraunhofer.de:15000/generate-ac?fqans=/dech&lifetime=43200
Loading CA Certificate /etc/grid-security/certificates/dd4b34ea.0.
Loading EACL namespace (signing_policy) /etc/grid-security/certificates/dd4b34ea.signing_policy.
Received VOMS response:

(...)

Remote VOMS server contacted succesfully.

Loading CA Certificate /etc/grid-security/certificates/dd4b34ea.0.
Loading EACL namespace (signing_policy) /etc/grid-security/certificates/dd4b34ea.signing_policy.
VOMS AC validation for VO dech succeded.
=== VO dech extension information ===
VO        : dech
subject   : /C=DE/O=GermanGrid/OU=dech-school/CN=gs062
issuer    : /C=DE/O=GermanGrid/OU=Fraunhofer SCAI/CN=host/glite-io.scai.fraunhofer.de
attribute : /dech/Role=NULL/Capability=NULL
timeleft  : 12:00:00
uri       : glite-io.scai.fraunhofer.de:15000

Created proxy in /tmp/x509up_u500.

Your proxy is valid until Mon Jun 13 23:24:37 CEST 2016
```

## preparing job payload

### job definition - my_sleep.jdl

```bash
[
  VirtualOrganisation      = "dech";
  Executable               = "my_sleep.sh";
  StdOutput                = "stdout";
  StdError                 = "stderr";
  InputSandbox             = {"my_sleep.sh"};
  OutputSandbox            = {"stdout","stderr"};
  outputsandboxbasedesturi = "gsiftp://localhost";
]
```

### job script - my_sleep.sh

```bash
#! /bin/sh

#########################################################################
echo hostname=`/bin/hostname -f`
echo --------------------------
echo ----  my_sleep.sh  -------
echo --------------------------
sleep 30
#
echo date=`/bin/date`
/usr/bin/id
echo pwd=`/bin/pwd`
/bin/ls -al
/bin/df .
#
# /usr/bin/env
# $GLOBUS_LOCATION/bin/grid-proxy-info -all
#########################################################################
```

## submitting job directly to computing element

```bash
$ glite-ce-job-submit -d -r grid-cr0.desy.de:8443/cream-pbs-desy -a my_sleep.jdl
2016-06-13 11:30:50,774 DEBUG - Using certificate proxy file [/tmp/x509up_u500]
2016-06-13 11:30:50,793 DEBUG - VO from certificate=[dech]
2016-06-13 11:30:50,793 WARN - No configuration file suitable for loading. Using built-in configuration
2016-06-13 11:30:50,793 DEBUG - Logfile is [/tmp/glite_cream_cli_logs/glite-ce-job-submit_CREAM_grid_20160613-113050.log]
2016-06-13 11:30:50,798 WARN - VirtualOrganisation specified in the JDL but overriden with [dech]
2016-06-13 11:30:50,798 DEBUG - Processing file [my_sleep.sh]...
2016-06-13 11:30:50,798 DEBUG - Adding absolute path [/home/grid/gridJob/my_sleep.sh]...
2016-06-13 11:30:50,798 DEBUG - Inserting mangled InputSandbox in JDL: [{"/home/grid/gridJob/my_sleep.sh"}]...
2016-06-13 11:30:50,839 INFO - certUtil::generateUniqueID() - Generated DelegationID: [a49c1d60ab96680a5e9db4b6bb83a216fe47ae86]
2016-06-13 11:30:52,454 DEBUG - Registering to [https://grid-cr0.desy.de:8443/ce-cream/services/CREAM2] JDL=[ StdOutput = "stdout"; BatchSystem = "pbs"; QueueName = "desy"; Executable = "my_sleep.sh"; VirtualOrganisation = "dech"; outputsandboxbasedesturi = "gsiftp://localhost"; OutputSandbox = { "stdout","stderr" }; InputSandbox = { "/home/grid/gridJob/my_sleep.sh" }; StdError = "stderr" ] - JDL File=[my_sleep.jdl]
2016-06-13 11:30:52,732 DEBUG - JobID=[https://grid-cr0.desy.de:8443/CREAM347851832]
2016-06-13 11:30:52,732 DEBUG - UploadURL=[gsiftp://grid-cr0.desy.de/var/cream_sandbox/dechusr/CN_gs062_OU_dech_school_O_GermanGrid_C_DE_dech_Role_NULL_Capability_NULL_dechusr000/34/CREAM347851832/ISB]
2016-06-13 11:30:52,738 INFO - Sending file [gsiftp://grid-cr0.desy.de/var/cream_sandbox/dechusr/CN_gs062_OU_dech_school_O_GermanGrid_C_DE_dech_Role_NULL_Capability_NULL_dechusr000/34/CREAM347851832/ISB/my_sleep.sh]
2016-06-13 11:30:53,179 DEBUG - Will invoke JobStart for JobID [CREAM347851832]
https://grid-cr0.desy.de:8443/CREAM347851832
```

## checking job status

```bash
$ glite-ce-job-status https://grid-cr0.desy.de:8443/CREAM347851832

******  JobID=[https://grid-cr0.desy.de:8443/CREAM347851832]
	Status        = [REALLY-RUNNING]


$ glite-ce-job-status https://grid-cr0.desy.de:8443/CREAM347851832

******  JobID=[https://grid-cr0.desy.de:8443/CREAM347851832]
	Status        = [DONE-OK]
	ExitCode      = [0]
```

## downloading job output

```bash
$ glite-ce-job-output https://grid-cr0.desy.de:8443/CREAM347851832

2016-06-13 11:32:10,682 INFO - For JobID [https://grid-cr0.desy.de:8443/CREAM347851832] output will be stored in the dir ./grid-cr0.desy.de_8443_CREAM347851832
```

```bash
$ cat grid-cr0.desy.de_8443_CREAM347851832/stdout
hostname=grid-wn0240.desy.de
--------------------------
---- my_sleep.sh -------
--------------------------
date=Mon Jun 13 11:31:53 CEST 2016
uid=42200(dechusr000) gid=4220(dechusr) groups=4220(dechusr)
pwd=/home/dechusr000/home_cream_347851832/CREAM347851832
total 16
drwxr-xr-x 2 dechusr000 dechusr 4096 Jun 13 11:31 .
drwxr-xr-x 3 dechusr000 dechusr 4096 Jun 13 11:31 ..
-rw-r--r-- 1 dechusr000 dechusr    0 Jun 13 11:31 .tmp_file
-rwxr-xr-x 1 dechusr000 dechusr  439 Jun 13 11:31 my_sleep.sh
-rw-r--r-- 1 dechusr000 dechusr    0 Jun 13 11:31 stderr
-rw-r--r-- 1 dechusr000 dechusr  261 Jun 13 11:31 stdout
Filesystem     1K-blocks      Used Available Use% Mounted on
/dev/sda6      779319656 161305364 578420464  22% /home
```

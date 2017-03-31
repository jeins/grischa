# GriScha short setup!
GriScha is a realtime grid chess engine. In this version it uses Redis for communication
 between the chess client and the worker nodes to evaluate moves.
 There are several other protocols used for communication:
 * SIMON
 * P2P
 * XMPP
 * ZMQ

For those repositories have a look at grischa_legacy:
```
git clone git@grischa.f4.htw−berlin.de:/grischa_legacy.git
```
These repos are not under development anymore, or one could say orphaned after research,
thesis ending.

### Compiling
First you have to download all required libraries:
- http://www.igniterealtime.org/projects/smack/
- http://commons.apache.org/proper/commons-cli/
- http://logging.apache.org/log4j/1.2/download.html (maybe use the version in the lib folder form the master branch. The newest version from the website causes some exceptions)

Or simnply clone them from our git server by:
```
git clone git@grischa.f4.htw−berlin.de:/grischa_dependencies.git
```

Put them in a directory calls "libs". The next step is to initialize the git submodule for JSON support. Run the following command: 
```
git submodule update --init
```
In this version you can compile GriScha using ant:
```
ant gnode gclient   # or
ant grischa         #  to build all at once
```

### Running GriScha

You get two jar-archives: gnode.jar which is the software running on the worker-nodes and gclient.jar. gclient.jar is the client software providing a interface to xboard. Assuming the project setup is correct, a XMPP-Server is running and xboard is installed. Use the following commands to launch GriScha. For the Worker-Node Software run:
```
	$ java -jar gnode.jar
```

To run the client software with xboard use this command:
```
	$ xboard -fcp "java -jar gclient.jar"
```

Right now not every parameter is configurable and the project is under development,
so maybe there are some modifications in the code necessary to get the system running.
The default Redis Server is grischa.f4.htw-berlin.de.

### Known errors!
There are several bugs:

* On some Intel hardware, with Linux OS (4.10 Kernel) the nodes run, but the Xboard wont
and generates one stupid move after which the communication dies totally.
* Some check mates wont be recognized, due some worker nodes could not find the right move
* The Redis messaging to Xboard has a threading problem, so that only one move is recognized


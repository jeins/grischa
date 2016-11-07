# GriScha short introduction
This is the GriScha project. GriScha is a realtime grid chess engine. In this version it uses XMPP for communication between the chess client and the worker nodes to evaluate moves. There are two other Protocols used SIMON and P2P. For those have a look at the git repository.

# How to compile and use
First you have to download all required libraries:
- http://www.igniterealtime.org/projects/smack/
- http://commons.apache.org/proper/commons-cli/
- http://logging.apache.org/log4j/1.2/download.html (maybe use the version in the lib folder form the master branch. The newest version from the website causes some exceptions)

Put them in a directory calls "libs". The next step is to initialize the git submodule for JSON support. Run the following command: 

	$ git submodule update --init
   
In this version you can compile GriScha using ant:

	$ ant gnode gclient # or
	$ ant grischa #  to build all at once

You get two jar-archives: gnode.jar which is the software running on the worker-nodes and gclient.jar. gclient.jar is the client software providing a interface to xboard. Assuming the project setup is correct, a XMPP-Server is running and xboard is installed. Use the following commands to launch GriScha. For the Worker-Node Software run:

	$ java -jar gnode.jar --user username@example.org \
					--password password

To run the client software with xboard use this command:
	
	$ xboard -fcp "java -jar gclient.jar"

Right now not every parameter is configurable and the project is under development, so maybe there are some modifications in the code necessary to get the system running. The default XMPP Server is grischa.f4.htw-berlin.de.

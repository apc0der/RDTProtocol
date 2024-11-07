# RDTProtocol
### Setup
1. Download `RDTClient.java` and `RDTServer.java` and store them under a folder in your netXX machine.
2. Download the `testserv` and `testclient` executables and put them in this same folder.
3. Navigate to the folder in the command line and then type `javac *.java` to compile both java files.
4. Verify that there are 3 new .class files in the folder: `RDTServer.class`, `Handler.class`, and `RDTClient.java`.

### Testing RDTServer.java
1. RDTServer supports multiple client connections.
2. Run the program by typing in the command line `java RDTServer <portNumber> <MAXSEQ#>`.
3. Open up new netXX machines and run multiple instances of the `testclient` executable by typing `testclient <server-name> <port#> <MAXSEQ#> <drop_prob>` in the command line.
4. In each client machine, type a word with no spaces and hit enter. You will see debug information on the serverside showing the received message and, if the character was recieved properly, a log message saying the character was ACK'd.

### Testing RDTClient.java
1. Multiple clients not possible as it is not supported by `testserv`.
2. Run `testserv` by typing `testserv <port> <MAXSEQ#> <loss_prob>` in the command line.
3. Open up new netXX machines and run multiple instances of RDTClient by typing `java RDTClient <hostName> <portNumber> <MAXSEQ#>` in the command line.
4. In each client machine, type a word with no spaces and hit enter. You will see debug information on the clientside showing how many times each character is taking to resend, and when a character is finally ACK'd properly.

import java.io.*;
import java.net.*;

public class RDTClient {
    public static int max = 0;
    public static void main(String[] args) throws IOException {
        if (args.length != 3) { // correct args
            System.err.println("Correct usage: java RDTClient <hostName> <portNumber> <MAXSEQ#>");
            System.exit(0);
        }
        String h = args[0]; // hostname
        int pNum = Integer.parseInt(args[1]); // port
        max = Integer.parseInt(args[2]); // maxseq
        try {
            DatagramSocket cliSock = new DatagramSocket(); // new socket
            String sentence = null; // variable for msgs
            BufferedReader io = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                sentence = io.readLine(); // get a msg
                int i = 0; // character iterator
                while (i < sentence.length()) { // while there are still chars to send
                    byte[] sendBuf = new byte[1024]; // send buffer
                    byte[] rcvBuf = new byte [1024]; // receive buffer
                    String toSend = "DATA " + Integer.toString(i%max) + " " + sentence.charAt(i); // make msg
                    sendBuf = toSend.getBytes(); // convert to bytes
                    DatagramPacket sendPkt = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(h), pNum);
                    cliSock.send(sendPkt); // build and send packet
                    DatagramPacket rcvPkt = new DatagramPacket(rcvBuf, rcvBuf.length); // prepare receive packet

                    try {
                        long startTime = System.currentTimeMillis();
                        cliSock.setSoTimeout(5000);
                        cliSock.receive((rcvPkt));
                        long curTime = System.currentTimeMillis();
                        int lapse = (int) (curTime - startTime);
                        String[] servMsg = new String(rcvPkt.getData()).trim().split(" ");
                        if (servMsg.length == 2) {
                            try {
                                if (servMsg[0].equals("ACK") && Integer.parseInt(servMsg[1]) == i%max) {} else {
                                    cliSock.setSoTimeout(Math.max(5000 - lapse, 0));
                                    cliSock.receive(rcvPkt);
                                }
                            } catch (Exception e){
                                cliSock.setSoTimeout(Math.max(5000 - lapse, 0));
                                cliSock.receive(rcvPkt);
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        i -= 1; // decrement, so increment later is the same as resend
                    } catch (SocketException e) {
                        System.err.println("Socket messed up.");
                        System.exit(0);
                    }
                    i += 1; // move on to the next
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read input.");
            System.exit(0);
        }
    }
}
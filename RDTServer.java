import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class RDTServer {
    // will keep track of the sequence numbers for multiple clients (multithreaded)
    public static ConcurrentHashMap<String, Integer> cliSeqNum = new ConcurrentHashMap<>();
    public static int max = 0;
    public static void main(String[] args) throws IOException {
        if (args.length != 2) { // correct args
            System.err.println("Correct usage: java RDTServer <portNumber> <MAXSEQ#>");
            System.exit(0);
        }
        int pNum = Integer.parseInt(args[0]); // port
        max = Integer.parseInt(args[1]); // max seq
        try {
            DatagramSocket servSock = new DatagramSocket(pNum); // create socket
            System.out.println("> Server hosted on " + InetAddress.getLocalHost().getHostName() + ", port " + pNum + ".");

            while (true) { // while we get new connections to server
                byte[] recvBuf = new byte[1024]; // new receive buffer
                DatagramPacket recvPkt = new DatagramPacket(recvBuf, recvBuf.length);
                servSock.receive((recvPkt)); // get the packet

                Handler h = new Handler(recvPkt, servSock); // new thread to handle this packet only
                h.start();
            }

        } catch (SocketException e) {
            System.err.println("> Server could not be opened.");
            System.exit(0);
        }
    }
}

class Handler extends Thread {
    private DatagramPacket pkt;
    private DatagramSocket skt;
    private String[] msg;
    private int seqNum;


    public Handler(DatagramPacket p, DatagramSocket s) {
        pkt = p;
        skt = s;
    }

    private void sendACK(int num) throws IOException {
        byte[] sendBuf = new byte[1024]; // to send to the client
        String ack = "ACK " + num; // build msg
        sendBuf = ack.getBytes(); // convert to bytes and load to array
        DatagramPacket rplPkt = new DatagramPacket(sendBuf, sendBuf.length, pkt.getAddress(), pkt.getPort());
        skt.send(rplPkt); // build packet and send
    }
    @Override
    public void run() {
        try {
            String cliName = pkt.getAddress().getHostName(); // for keeping track of sequence number
            if (!RDTServer.cliSeqNum.containsKey(cliName)) {
                RDTServer.cliSeqNum.put(cliName, 0);
            }
            seqNum = RDTServer.cliSeqNum.get(cliName);

            msg = new String(pkt.getData()).trim().split(" "); // parse the message
            System.out.println("> Server received message from " + cliName + ": " + String.join(" ", msg));

            if (msg.length == 3) { // gotta be length 3
                if (msg[0].equals("DATA")) { // needs to start with DATA
                    try {
                        if (Integer.parseInt(msg[1]) == seqNum) { // needs to be an int
                            System.out.println("> Server received character: " + msg[2]);
                            sendACK(seqNum); // good ack
                            seqNum += 1;
                            seqNum %= RDTServer.max;
                            RDTServer.cliSeqNum.put(cliName, seqNum);
                        } else {
                            // not good ack, so send previous
                            if (seqNum == 0) {
                                sendACK(RDTServer.max - 1);
                            } else {
                                sendACK(seqNum - 1);
                            }
                        }
                    } catch (Exception e) {
                        // not good ack, so send previous
                        System.err.println("> Did not receive a sequence number.");
                        if (seqNum == 0) {
                            sendACK(RDTServer.max - 1);
                        } else {
                            sendACK(seqNum - 1);
                        }
                    }
                } else {
                    // not good ack, so send previous
                    if (seqNum == 0) {
                        sendACK(RDTServer.max - 1);
                    } else {
                        sendACK(seqNum - 1);
                    }
                }
            } else {
                // not good ack, so send previous
                if (seqNum == 0) {
                    sendACK(RDTServer.max - 1);
                } else {
                    sendACK(seqNum - 1);
                }
            }
        } catch (Exception e) {
            System.err.println("> Could not send packet.");
        }
    }
}

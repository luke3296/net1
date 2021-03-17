package networks.cw1;    
import CMPC3M06.AudioRecorder;
//import uk.ac.uea.cmp.voip.DatagramSocket;

import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

public class AudioSendThread implements Runnable {

    final String dhBaseVal = "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1" +
      "29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD" +
      "EF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245" +
      "E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED" +
      "EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D" +
      "C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F" +
      "83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D" +
            "670C354E 4ABC9804 F1746C08 CA237327 FFFFFFFF FFFFFFFF";

    ByteBuffer voipPacket;
    InetAddress clientIP;
    String ip = "localhost";
    DatagramSocket sending_socket; // for sending key and the audio packets
    DatagramSocket receiving_socket; //for listening for a key from receieve
    int RECEIVE_PORT = 55556; //The port to listen to packets on
    int SEND_PORT = 55557; //The port to send packets too
    int dhParam = 4;
    int modulus = 2;
    int base = 2;
    int generator = 2;
    BigInteger dhBase;
    BigInteger dhSharedKey = BigInteger.ZERO; // calculate the key from the key recieved  form the Reciever
    short authKey = 38;
    AudioRecorder ar;
    ArrayList<VoipPacket> packets; //hold a list of packets for interleaving
    int interleaveCount = 0; // count the packets sent
    int interleaveCase = 0; // 0 = no interleaving,  1= 4x4 interleaving, 2 = 8x8

    public AudioSendThread(int interleave) {
        this.interleaveCase = interleave;
    }


    public void initializeResources() {
        System.out.println("send start");
        dhBase = new BigInteger(dhBaseVal.replaceAll("\\s+", ""), 16);
        Random rnd = new Random();
        dhParam = rnd.nextInt();
        BigInteger dhSendKey = BigInteger.valueOf(generator).modPow(BigInteger.valueOf(dhParam), dhBase);

        //dhSendKey = (int) (Math.pow(dhBase , dhParam) % modulus);
        packets = new ArrayList<>();
        //initalize the clinetIP;
        try {
            clientIP = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //initlaize the sending socket, will send data from random ports
        try {
            sending_socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //initlaize the recieveing socket, will listen on port provided
        try {
            receiving_socket = new DatagramSocket(RECEIVE_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //initlaize the audio recorder
        try {
            ar = new AudioRecorder();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        //Doing the dh key exchange over tcp
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(4999);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Socket s = null;
        try {
            s = ss.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStreamReader ins = null;
        try {
            ins = new InputStreamReader(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(ins);

        String str = null;
        try {
            str = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert pw != null;
        pw.println(dhSendKey);
        pw.flush();

        assert str != null;
        BigInteger receivedKey = new BigInteger(str);
        //compute the shared key
        dhSharedKey = receivedKey.modPow(BigInteger.valueOf(dhParam), dhBase);
        System.out.println("sender got to main loop with key: " + dhSharedKey.toString(16));

        //close the resources used for the tcp dh key swap
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw.close();
        try {
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw.close();
    }



    @Override
    public void run() {
        initializeResources();
        //main loop
        while (true) {
            try {
                byte[] data;
                //load 32ms of audio data
                data = ar.getBlock();
                VoipPacket packet = new VoipPacket(authKey, HelperClass.encryptData(data, dhSharedKey));

                switch (interleaveCase) {
                    case 0:
                            sendData(packet.encode());
                            break;
                    case 1:
                            if (interleaveCount == 16) {
                                sendPackets(HelperClass.interleavePackets4x4(packets));
                                //    System.out.println("sending " + packets.size() + " packets");
                                packets.clear();
                                interleaveCount = 0;
                            } else {
                                packets.add(packet);
                                interleaveCount++;
                            }
                        break;
                    case 2:
                            if (interleaveCount == 64) {
                                sendPackets(HelperClass.interleavePackets8x8(packets));
                                packets.clear();
                                interleaveCount = 0;
                            } else {
                                packets.add(packet);
                                interleaveCount++;
                            }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void sendPackets(ArrayList<VoipPacket> arrL) throws IOException {
        for (VoipPacket pkt : arrL) {
            byte[] data = pkt.encode();
            DatagramPacket packet = new DatagramPacket(data, data.length, clientIP, SEND_PORT);
            sending_socket.send(packet);
        }
    }
    public void sendData(byte[] blk) throws IOException {
        DatagramPacket packet = new DatagramPacket(blk,blk.length, clientIP, SEND_PORT);

        //Send it
        sending_socket.send(packet);
    }
}

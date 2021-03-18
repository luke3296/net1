package networks.cw1;/*
 * TextReceiver.java
 */

import CMPC3M06.AudioPlayer;
//import uk.ac.uea.cmp.voip.DatagramSocket2;

import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AudioRecieveThread implements Runnable{

    final String dhBaseVal = "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1" +
            "29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD" +
            "EF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245" +
            "E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED" +
            "EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D" +
            "C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F" +
            "83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D" +
            "670C354E 4ABC9804 F1746C08 CA237327 FFFFFFFF FFFFFFFF";

    InetAddress clientIP;
    String ip = "localhost";
    DatagramSocket sending_socket; // for sending key and the audio packets
    DatagramSocket receiving_socket; //for listening for a key from receieve   now redundent
    int RECIEVE_PORT = 55557; //The port to listen to packets on
    int SEND_PORT = 55556; //The port to send packets too
    int dhParam = 3;
    int generator = 2;
    BigInteger dhBase;
    BigInteger dhSharedKey = BigInteger.ZERO; // calculate the key from the key recieved  form the Reciever
    short authKey = 38;
    AudioPlayer ap;
    ArrayList<VoipPacket> packets; //hold a list of packets for interleaving
    int interleaveCount = 0; // count the packets sent
    int interleaveCase = 1; // 0 = no interleaving,  1= 4x4 interleaving, 2 = 8x8

    int counter = 0;

    //now redundent using tcp for key exhange
    static boolean waitForKey = true;
    static  boolean waitForConfirm = true;
    static int confirmed = 0;

    public AudioRecieveThread(int interleave) {
        this.interleaveCase = interleave;
    }


    @Override
    public void run() {
        System.out.println("reciever start");
     
        dhBase = new BigInteger(dhBaseVal.replaceAll("\\s+", ""), 16);
        Random rnd = new Random();
        dhParam = rnd.nextInt();
        BigInteger dhSendKey = BigInteger.valueOf(generator).modPow(BigInteger.valueOf(dhParam), dhBase);
        packets = new ArrayList<VoipPacket>();

        //initialize the audio player
        try {
            ap = new AudioPlayer();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

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
            receiving_socket = new DatagramSocket(RECIEVE_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Socket s = null;
        try {
            s = new Socket(ip, 4999);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //send the shared key
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //send the shared part of dh-key
        pw.println(dhSendKey);
        pw.flush();

        //recived the dh shared key from the send thread
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader bfr = new BufferedReader(in);
        String str = null;
        try {
            str = bfr.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigInteger receivedKey = new BigInteger(str);
        //compute the shared key
        dhSharedKey = receivedKey.modPow(BigInteger.valueOf(dhParam), dhBase);
        System.out.println("reciever got to main loop with key: " + dhSharedKey.toString(16));

        //close the resources used for the tcp dh key swap
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw.close();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bfr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw.close();
        VoipPacket lastPacket = VoipPacket.EMPTY;
        //main loop
        while(true) {
            ByteBuffer buff = ByteBuffer.wrap(recieveData(522));
            VoipPacket packet = VoipPacket.from(buff);
            counter++;
            if (packet.authKey != authKey) {
                System.out.println("Auth err");
                continue;
            }
            try {
                switch (interleaveCase) {
                    case 0:
                        //389), 5657), 1507
                        //  byte[] data = Arrays.copyOfRange(rp.res, 4, 516);
                        if(packet.checkData(counter))
                            ap.playBlock(HelperClass.decryptData(packet.data, dhSharedKey));
                        //ap.playBlock(packet.data);
                        break;
                    case 1:
                        packets.add(packet);
                        if (interleaveCount == 16) {
                            //deinterleavce and play
                            packets = HelperClass.interleavePackets4x4(packets);
                            Collections.sort(packets);
                            playPackets((packets));
                            //to play packets without deinterleaving cuncomment below
                            // playPackets(packets);
                            packets.clear();
                            interleaveCount = 0;
                        } else {
//                    try {
//                        playData(decryptData(decryptData(decryptData(rp.res, 389), 5657), 1507));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                            interleaveCount++;
                        }
                        break;
                    case 2:
                        packets.add(packet);
                        if (interleaveCount == 25) {
                            //deinterleavce and play
                            packets = HelperClass.interleavePackets5x5(packets);
                            Collections.sort(packets);
                            playPackets((packets));
                            //  playPackets(arr);
                            packets.clear();
                            interleaveCount = 0;
                        } else {
                            interleaveCount++;
                        }
                        break;
                    case 3:
                    packets.add(packet);
                    if (interleaveCount == 64) {
                        //deinterleavce and play
                        packets = HelperClass.interleavePackets8x8(packets);
                        Collections.sort(packets);
                        playPackets((packets));
                        //  playPackets(arr);
                        packets.clear();
                        interleaveCount = 0;
                    } else {
                        interleaveCount++;
                    }
                    break; 
                    case 4:
                         //389), 5657), 1507
                        //  byte[] data = Arrays.copyOfRange(rp.res, 4, 516);
                        if(packet.checkData(counter))
                            ap.playBlock(packet.data);
                        //ap.playBlock(packet.data);
                        break;

                    default:
                        System.out.println("interleaveCase must be 0-2");
                }
            }catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if(packet.checkData(counter))
                lastPacket = packet;
        }
//        while(true){
//            HelperClass.RecievedPacket rp = HelperClass.parsePacket(recieveData(20));
//            System.out.println(rp.res.length + " "+ rp.authKey + " " + rp.shareKey);
//            System.out.println(convertByteArrayToInt(rp.res));
//            byte[] res = new byte[4];
//            byte[] res2 = new byte[4];
//            byte[] res3 = new byte[4];
//
//            for (int i = 0; i < 4; i++) {
//                res[i] = rp.res[i];
//                res2[i] = rp.res[i+4];
//                res3[i] = rp.res[i+8];
//            }
////            System.out.println(convertByteArrayToInt(res));
////            System.out.println(convertByteArrayToInt(res2));
////            System.out.println(convertByteArrayToInt(res3));
//
//        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void playPackets(ArrayList<VoipPacket> arrL) throws IOException {
        int packet = arrL.get(0).pktNum;
        for (VoipPacket voipPacket: arrL) {
            if(voipPacket.checkData(0) ) //packet == voipPacket.pktNum)
                ap.playBlock(HelperClass.decryptData(voipPacket.data, dhSharedKey));
            packet++;
        }

    }


    public byte[] recieveData(int length){
        byte[] buffer = new byte[length];
        try {
            //Receive a DatagramPacket (note that the string cant be more than 80 chars)
            DatagramPacket packet = new DatagramPacket(buffer, 0, length);
            receiving_socket.receive(packet);
            // receiving_socket.close();

        } catch (IOException e) {
            System.out.println("ERROR: TextReceiver: Some random IO error occured!");
            e.printStackTrace();
        }
        return buffer;
    }
}

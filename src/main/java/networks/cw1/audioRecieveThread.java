package networks.cw1;/*
 * TextReceiver.java
 */

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;
//import uk.ac.uea.cmp.voip.DatagramSocket;

import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class audioRecieveThread  implements Runnable{

static ByteBuffer voipPacket;
    static InetAddress clientIP;
    static String ip = "localhost";
    static DatagramSocket sending_socket; // for sending key and the audio packets
    static DatagramSocket receiving_socket; //for listening for a key from receieve   now redundent
    static int RECIEVE_PORT = 55557; //The port to listen to packets on
    static int SEND_PORT = 55556; //The port to send packets too
    static int receivedKey =0; //the key to recieve from the Reciever thread
    static int dhParam = 3;
    static int modulus = 23;
    static int base = 5;
    static int dhSendKey = 0;// the key to send == (base ^ dhParam) mod modulus e.g 5^4mod23
    static int dhSharedKey = 0; // calculate the key from the key recieved  form the Reciever
    static short authKey = 38;
    static AudioPlayer ap;
    static ArrayList<byte[]> packets; //hold a list of packets for interleaving
    static int interleaveCount = 0; // count the packets sent
    static int interleaveCase = 0; // 0 = no interleaving,  1= 4x4 interleaving, 2 = 8x8

    //now redundent using tcp for key exhange
    static boolean waitForKey = true;
    static  boolean waitForConfirm = true;
    static int confirmed = 0;





    @Override
    public void run() {
        System.out.println("reciever start");
        dhSendKey = (int) (Math.pow(base , dhParam) % modulus);
        packets = new ArrayList<byte[]>();

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
        //Doing the dh key exchange over udp
//        //listen for key
//        while (waitForKey){
//            receivedKey=recieveIntKey();
//            if(receivedKey > 0){
//                waitForKey = false;
//            }
//        }
//
//        //i should use tcp
//            try {
//                sendIntKey(1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//        while (waitForConfirm){
//            try {
//                sendIntKey(dhSendKey);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            confirmed = recieveIntKey();
//            if(confirmed == 1){
//                waitForConfirm = false;
//            }
//        }

        //Doing the dh key exchange over tcp
        //create a new socket on port 4449
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
        receivedKey = Integer.valueOf(str);
        dhSharedKey = (int) (Math.pow(receivedKey, dhParam)%modulus);
        System.out.println("reciever got to main loop with key: " + dhSharedKey);

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

        //main loop
        switch (interleaveCase){
            case 0:
                while (true){
                    //389), 5657), 1507
                    HelperClass.RecievedPacket rp = HelperClass.parsePacket(recieveData(516));
                  //  byte[] data = Arrays.copyOfRange(rp.res, 4, 516);
                    if(rp.authKey != authKey || rp.shareKey != dhSharedKey){
                        System.out.println("Auth err");
                    }else {
                        try {
                            ap.playBlock(HelperClass.decryptData(HelperClass.decryptData(HelperClass.decryptData(rp.res, 389), 5657), 1507));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            case 1:
                while (true){
                    HelperClass.RecievedPacket rp = HelperClass.parsePacket(recieveData(516));
                    if(rp.authKey != authKey || rp.shareKey != dhSharedKey){
                        System.out.println("Auth err");
                    }else {
                        packets.add(rp.res);
                        if (interleaveCount == 16) {
                            //deinterleavce and play
                            try {
                                playPackets(HelperClass.interleavePackets4x4(packets));
                                //to play packets without deinterleaving cuncomment below
                                 // playPackets(packets);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            packets.clear();
                            interleaveCount = 0;
                        } else {
                            if (rp.authKey != 38) {
                                System.out.println("packet wasnt autheticated");
                            } else {
//                    try {
//                        playData(decryptData(decryptData(decryptData(rp.res, 389), 5657), 1507));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                            }
                            interleaveCount++;
                        }
                    }
                }
            case 2:
                while (true){
                    HelperClass.RecievedPacket rp = HelperClass.parsePacket(recieveData(516));
                    if(rp.authKey != authKey || rp.shareKey != dhSharedKey){
                        System.out.println("Auth err");
                    }else {
                        packets.add(rp.res);
                        if (interleaveCount == 64) {
                            //deinterleavce and play
                            try {
                                playPackets(HelperClass.interleavePackets8x8(packets));
                                //  playPackets(arr);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            packets.clear();
                            interleaveCount = 0;
                        } else {
                            if (rp.authKey != 38) {
                                System.out.println("packet wasnt autheticated");
                            } else {
//                    try {
//                        playData(decryptData(decryptData(decryptData(rp.res, 389), 5657), 1507));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                            }
                            interleaveCount++;
                        }
                    }
                }
            default:
                System.out.println("interleaveCase must be 0-2");
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
    public static int convertByteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
    public static void playPackets(ArrayList<byte[]> arrL) throws IOException {
        for (byte[] byteArr: arrL) {
            ap.playBlock(HelperClass.decryptData(HelperClass.decryptData(HelperClass.decryptData(byteArr, 389), 5657), 1507));
        }

    }

    //sends an int in the payload of the udp
    public static void sendIntKey(int keyToSend) throws IOException {
        voipPacket =  ByteBuffer.allocate(4);
        voipPacket.putInt(keyToSend);
        byte[] firstSendKey;
        firstSendKey =  voipPacket.array();
        DatagramPacket pack = new DatagramPacket(firstSendKey,firstSendKey.length,clientIP,SEND_PORT);
        sending_socket.send(pack);
        voipPacket.clear();
    }
    //recieves a packet and returns an int     now redundent
    public static int recieveIntKey(){
        byte[] res = new byte[4];
        DatagramPacket recievedPacket = new DatagramPacket(res,res.length);

        try {
            receiving_socket.receive(recievedPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream byteIn = new ByteArrayInputStream(recievedPacket.getData());
        DataInputStream dataIn = new DataInputStream(byteIn);
        int tmp = 0;
        try {
            tmp = dataIn.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }
    public static byte[] addHeader(byte[] data){
        voipPacket = ByteBuffer.allocate(data.length+2);//2 bytes n=more than paylaod
        voipPacket.putShort(authKey);
        voipPacket.put(data);
        byte[] res = voipPacket.array();
        voipPacket.clear();
        return res;
    }
    public static byte[] recieveData(int length){
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

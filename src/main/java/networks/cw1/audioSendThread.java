package networks.cw1;    
import CMPC3M06.AudioRecorder;
//import uk.ac.uea.cmp.voip.DatagramSocket;

import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class audioSendThread implements Runnable {
    static ByteBuffer voipPacket;
    static InetAddress clientIP;
    static String ip = "localhost";
    static DatagramSocket sending_socket; // for sending key and the audio packets
    static DatagramSocket receiving_socket; //for listening for a key from receieve
    static int RECIEVE_PORT = 55556; //The port to listen to packets on
    static int SEND_PORT = 55557; //The port to send packets too
    static int receivedKey =0; //the key to recieve from the Reciever thread
    static int dhParam = 4;
    static int modulus = 23;
    static int base = 5;
    static int dhSendKey = 0;// the key to send == (base ^ dhParam) mod modulus e.g 5^4mod23
    static int dhSharedKey = 0; // calculate the key from the key recieved  form the Reciever
    static short authKey = 38;
    static AudioRecorder ar;
    static ArrayList<byte[]> packets; //hold a list of packets for interleaving
    static int interleaveCount = 0; // count the packets sent
    static int interleaveCase = 0; // 0 = no interleaving,  1= 4x4 interleaving, 2 = 8x8
//now redundent using tcp for key exhange
    static boolean waitForKey = true;
    static  boolean waitForConfirm = true;
    static int confirmed = 0;




    @Override
    public void run() {
        System.out.println("send start");
        dhSendKey = (int) (Math.pow(base , dhParam) % modulus);
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
            receiving_socket = new DatagramSocket(RECIEVE_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //initlaize the audio recorder
        try {
            ar = new AudioRecorder();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
//Doing the dh key exchange over udp
        //send key and listen for confirm, if confirm break
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
//        //send the confirm key 2 times, bursts could mess this up
//            try {
//                sendIntKey(1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        //listen for key
//        while (waitForKey){
//            receivedKey=recieveIntKey();
//            if(receivedKey > 0){
//                waitForKey = false;
//            }
//        }

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
        pw.println(dhSendKey);
        pw.flush();

        receivedKey = Integer.valueOf(str);
        //compute the shared key
        dhSharedKey = (int) (Math.pow(receivedKey, dhParam)%modulus);
        System.out.println("sender got to main loop with key: " + dhSharedKey);

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


        //main loop
        switch (interleaveCase){
            case 0:
                while(true){
                    byte[] data = new byte[512];
                    //load 32ms of audio data
                    try {
                        data = ar.getBlock();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        sendData(addDHKeyToHeader(addAuthKeyToHeader(HelperClass.encryptData(HelperClass.encryptData(HelperClass.encryptData(data, 1507), 5657), 389))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            case 1:
                while (true){
                    byte[] data = new byte[512];
                    //load 32ms of audio data
                    try {
                        data = ar.getBlock();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (interleaveCount == 16) {
                            sendPackets(HelperClass.interleavePackets4x4(packets));
                            //    System.out.println("sending " + packets.size() + " packets");
                            packets.clear();
                            interleaveCount = 0;
                        } else {
                            packets.add(addDHKeyToHeader(addAuthKeyToHeader(HelperClass.encryptData(HelperClass.encryptData(HelperClass.encryptData(data, 1507), 5657), 389))));
                            interleaveCount++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case 2:
                    while (true){
                        byte[] data = new byte[512];
                        //load 32ms of audio data
                        try {
                            data = ar.getBlock();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (interleaveCount == 64) {
                                sendPackets(HelperClass.interleavePackets8x8(packets));
                                packets.clear();
                                interleaveCount = 0;
                            } else {
                                packets.add(addDHKeyToHeader(addAuthKeyToHeader(HelperClass.encryptData(HelperClass.encryptData(HelperClass.encryptData(data, 1507), 5657), 389))));
                                interleaveCount++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

        }

//        while(true){
//            int[] ints = {1,2,3,4};
//            byte[] byts = null;
//            try {
//               byts  = HelperClass.integersToBytes(ints);
//           //     System.out.println(byts.length);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            byts = addAuthKeyToHeader(byts);
//            byts = addDHKeyToHeader(byts);
//         //   System.out.println("byts len+ " + byts.length);
//            DatagramPacket pack = new DatagramPacket(byts,byts.length,clientIP, SEND_PORT);
//            try {
//                sending_socket.send(pack);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    //sends an int in the payload of the      now redundent using tcp
    public static void sendIntKey(int keyToSend) throws IOException {
        voipPacket =  ByteBuffer.allocate(4);
        voipPacket.putInt(keyToSend);
        byte[] firstSendKey;
        firstSendKey =  voipPacket.array();
        DatagramPacket pack = new DatagramPacket(firstSendKey,firstSendKey.length,clientIP,SEND_PORT);
        sending_socket.send(pack);
        voipPacket.clear();
    }
    //recieves a packet and returns            now redundent using tcp
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
    //add Header
    public static byte[] addAuthKeyToHeader(byte[] data){
        voipPacket = ByteBuffer.allocate(data.length+2);//2 bytes n=more than paylaod
        voipPacket.putShort(authKey);
        voipPacket.put(data);
        byte[] res = voipPacket.array();
        voipPacket.clear();
        return res;
    }
    public static byte[] addDHKeyToHeader(byte[] data){
        voipPacket = ByteBuffer.allocate(data.length+2);//2 bytes n=more than paylaod
        voipPacket.putShort((short)dhSharedKey);
        voipPacket.put(data);
        byte[] res = voipPacket.array();
        voipPacket.clear();
        return res;
    }
    public static void sendPackets(ArrayList<byte[]> arrL) throws IOException {
        for (byte[] byteArr: arrL) {
            DatagramPacket packet = new DatagramPacket(byteArr,byteArr.length, clientIP, SEND_PORT);
            sending_socket.send(packet);
        }
    }
    public static void sendData(byte[] blk) throws IOException {
        DatagramPacket packet = new DatagramPacket(blk,blk.length, clientIP, SEND_PORT);

        //Send it
        sending_socket.send(packet);
    }
}

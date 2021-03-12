package networks.cw1;/*
 * TextReceiver.java
 */

/**
 *
 * @author  abj
 */
import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class audioRecieveThread implements Runnable {

    static DatagramSocket receiving_socket;
    static AudioPlayer ap;
    static int PORT = 55555;

    static byte[] integersToBytes(int[] values) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < values.length; ++i) {
            dos.writeInt(values[i]);
        }

        return baos.toByteArray();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        boolean running = true;
        int interleaveCount = 0;
        ArrayList<byte[]> arr = new ArrayList<byte[]>();
        int AuthKey = 38;


        try {
            ap = new AudioPlayer();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }


        try {
            receiving_socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }


        while (running) {
            RecievedPacket rp = parsePacket(recieveData());
            if (rp.authKey != AuthKey) {
                //msg wernt authentic
            }else{
                if (interleaveCount == 16) {
                //deinterleavce and play
                    try {
                        //need deinterleaving method
                    playPackets(deinterleavePackets4x4(arr));
                    //  playPackets(arr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    interleaveCount = 0;
                    arr.clear();
            } else {
                    arr.add(rp.res);
                    //plays packet w/o interleaving
//                    try {
//                        playData(decryptData(decryptData(decryptData(rp.res, 389), 5657), 1507));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                interleaveCount++;
            }
        }
    }
        receiving_socket.close();
}


    public static void playPackets(ArrayList<byte[]> arrL) throws IOException {
        for (byte[] byteArr: arrL) {
           playData(decryptData(decryptData(decryptData(byteArr, 389), 5657), 1507));
           //play w/o decrypting
          //  playData(byteArr);
        }
    }

    public static ArrayList<byte[]> deinterleavePackets4x4(ArrayList<byte[]> arrL){
        HelperClass.Matrix m = new HelperClass.Matrix(arrL,4,4);
        HelperClass.Matrix m2 = new HelperClass.Matrix(m.getPacketList(),4,4);
       // return m.transpose().getList(); //working 4x4 deinterleaveing
        return m.getList(); //not working interleaving
    }
    public static ArrayList<byte[]> deinterleavePackets12x12(ArrayList<byte[]> arrL){
        HelperClass.Matrix m = new HelperClass.Matrix(arrL,12,12);
        return m.transpose().getPacketList();
    }



    public static byte[] recieveData(){
        byte[] buffer = new byte[514];
        try {
            //Receive a DatagramPacket (note that the string cant be more than 80 chars)
            DatagramPacket packet = new DatagramPacket(buffer, 0, 514);
            receiving_socket.receive(packet);
        } catch (IOException e) {
            System.out.println("ERROR: TextReceiver: Some random IO error occured!");
            e.printStackTrace();
        }
        return buffer;
    }

    public static void playData(byte[] arr) throws IOException {
        ap.playBlock(arr);
    }
    public static byte[] decryptData(byte[] encryptedBlock, int key) throws IOException {
        int fourByte = 0;
        ByteBuffer cipherText = ByteBuffer.wrap(encryptedBlock);
        int[] plainIextInt = new int[128];
        for (int i = 0; i < encryptedBlock.length/4; i++) {
            fourByte = cipherText.getInt();
            fourByte = fourByte ^ key;
            plainIextInt[i] = fourByte;
        }
        return integersToBytes(plainIextInt);
    }


    public static RecievedPacket parsePacket(byte[] data){
        byte[] res = new byte[512];
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(data[0]);
        bb.put(data[1]);
        short shortVal = bb.getShort(0);
        //  System.out.println(shortVal);
        System.arraycopy(data, 2, res, 0,512);
        RecievedPacket rp = new RecievedPacket(res,shortVal);
        return rp;
    }

    static class RecievedPacket{
        byte[] res;
        short authKey;
        public RecievedPacket(byte[] res, short authKey){
            this.res = res;
            this.authKey = authKey;
        }
        public byte[] getRes(){return this.res;}
        public short getAuthKey(){return this.authKey;}
    }

}

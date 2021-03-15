package networks.cw1;    /*
     * TextSender.java
     */

/**
 *
 * @author  abj
 */
    import CMPC3M06.AudioRecorder;
    import uk.ac.uea.cmp.voip.DatagramSocket2;
    import uk.ac.uea.cmp.voip.DatagramSocket3;
    import uk.ac.uea.cmp.voip.DatagramSocket4;

    import javax.sound.sampled.LineUnavailableException;
    import java.net.*;
    import java.io.*;
    import java.nio.ByteBuffer;
    import java.util.ArrayList;
    import java.util.Random;

public class audioSendThread implements Runnable {

        // from Prabhu R on SO here -> https://stackoverflow.com/questions/1086054/how-to-convert-int-to-byte/1086071#1086071
        static byte[] integersToBytes(int[] values) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for(int i=0; i < values.length; ++i)
            {
                dos.writeInt(values[i]);
            }
            return baos.toByteArray();
        }


        // static int key = 57; // if the key is 512 the audio is uneffected the audio is still understandble but there noticble noise
        private DatagramSocket sending_socket;
        private InetAddress clientIP = null;
        static int PORT = 55555;
        private AudioRecorder ar;
        static short authKey = 38;
        private  ByteBuffer voipPacket;
        private ArrayList<byte[]> packets = new ArrayList<byte[]>();
        public void start(){
            Thread thread = new Thread(this);
            thread.start();
        }

        public void run (){
            try {
                ar = new AudioRecorder();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            int interleaveCount=0;
            while (true) {
                try {
                    sending_socket = new DatagramSocket4();
                } catch (SocketException e) {
                    System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
                    e.printStackTrace();
                    System.exit(0);
                }
                try {
                    clientIP = InetAddress.getByName("localhost");
                } catch (UnknownHostException e) {
                    System.out.println("ERROR: TextSender: Could not find client IP");
                    e.printStackTrace();
                    System.exit(0);
                }


                byte[] data = new byte[512];
                try {
                    data = ar.getBlock();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(data == null){
                    System.out.println("data was null");
                }
                try {
                    // sendData(addHeader(encryptData(encryptData(encryptData(data, 1507), 5657), 389))); // this is with three rounds yet still understanble
                    Packet packet = new Packet(authKey, encryptData(data, 1680480041));
                    DatagramPacket packet1 = new DatagramPacket(new byte[1], 1, clientIP, PORT);
                    packet.SendPacket(packet1);
                    sending_socket.send(packet1);

//
//                    if (interleaveCount == 16) {
//                        sendPackets(interleavePackets4x4(packets));
//                        //    System.out.println("sending " + packets.size() + " packets");
//                        packets.clear();
//                        interleaveCount = 0;
//                    } else {
//                        packets.add(addHeader(encryptData(encryptData(encryptData(data, 1507), 5657), 389)));
//                        interleaveCount++;
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /*
         * interlaeve function
         *
         * (id +j) = jd+(d-1-i) 0<= i,j <= d-1  d=4
         *     0   1   2   3  j*width+i width=4
         * 0   1   2   3   4
         * 1   5   6   7   8
         * 2   9   10  11  12
         * 3   13  14  15  16
         *
         * packet 4 position 3 i=3 j= 0 index = 0*4 +3 = 3
         * packet 10 position 9 i =1 j=2  index = 2*4 +1 = 9
         * */
//        public static ArrayList<byte[]> interleavePackets4x4(ArrayList<byte[]> arrL){
//            HelperClass.Matrix m = new HelperClass.Matrix(arrL,4,4);
//           // m.printMatrix();
//            return m.getList();
//        }
//        public static ArrayList<byte[]> interleavePackets12x12(ArrayList<byte[]> arrL){
//            HelperClass.Matrix m = new HelperClass.Matrix(arrL,12,12);
//            return m.transpose().getPacketList();
//        }

        public void sendData(byte[] blk) throws IOException {
            DatagramPacket packet = new DatagramPacket(blk,blk.length, clientIP, PORT);

            //Send it
            sending_socket.send(packet);
        }
        public byte[] getData() throws IOException {
            return ar.getBlock();
        }
        public byte[] addHeader(byte[] data){
            voipPacket = ByteBuffer.allocate(514);//2 bytes n=more than paylaod
            voipPacket.putShort(authKey);
            voipPacket.put(data);
            return voipPacket.array();

        }

        public void closeResources(){
            ar.close();
            sending_socket.close();
        }
        public byte[] encryptData(byte[] arr, int key) throws IOException {
            byte[] res = new byte[512];
            int[] intRes = new int[128];
            int fourByte =0;
            ByteBuffer plainText = ByteBuffer.wrap(arr);
            Random random = new Random(key);
            byte[] xorBytes = new byte[512];
            random.nextBytes(xorBytes);
            ByteBuffer xorKey = ByteBuffer.wrap(xorBytes);
            for (int j = 0; j < arr.length / 4; j++) {
                fourByte = plainText.getInt();
                fourByte = fourByte ^ xorKey.getInt();// XOR operation with keyunwrapEncrypt.putInt(fourByte); }
                intRes[j] = fourByte;
            }
            return integersToBytes(intRes);
        }

    }


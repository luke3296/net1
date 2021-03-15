package networks.cw1;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class HelperClass {
    static class RecievedPacket{
        //contains the 512 byte of audio data
        byte[] res;
        short authKey;
        short shareKey;
        public RecievedPacket(byte[] res, short authKey, short shareKey){
            this.res = res;
            this.authKey = authKey;
            this.shareKey = shareKey;
        }
        public byte[] getRes(){return this.res;}
        public short getAuthKey(){return this.authKey;}
        public short getShareKey(){return this.shareKey;}
    }
    public static RecievedPacket parsePacket(byte[] data){
        byte[] res = new byte[data.length-4];
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(data[0]);
        bb.put(data[1]);
        bb.put(data[2]);
        bb.put(data[3]);

        short shortVal = bb.getShort(0);
        short shortval2 = bb.getShort(2);
        //src arr, src pos, dest arr, length to copy
        System.arraycopy(data, 4, res, 0,data.length-4);
        RecievedPacket rp = new RecievedPacket(res,shortval2,shortVal);
        return rp;
    }

    public static byte[] integersToBytes(int[] values) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < values.length; ++i) {
            dos.writeInt(values[i]);
        }
        return baos.toByteArray();
    }
    public static ArrayList<byte[]> interleavePackets4x4(ArrayList<byte[]> arrL) {
        ArrayList<byte[]> res = new ArrayList<byte[]>();
        res.add(arrL.get(0)); //0
        res.add(arrL.get(4)); //1
        res.add(arrL.get(8)); //2
        res.add(arrL.get(12)); //3

        res.add(arrL.get(1)); //4
        res.add(arrL.get(5));
        res.add(arrL.get(9));
        res.add(arrL.get(13));

        res.add(arrL.get(2));
        res.add(arrL.get(6));
        res.add(arrL.get(10));
        res.add(arrL.get(14));


        res.add(arrL.get(3));
        res.add(arrL.get(7));
        res.add(arrL.get(11));
        res.add(arrL.get(15));

        return res;
    }

    public static ArrayList<int[]> interleavePackets4x4test(ArrayList<int[]> arrL) {
        ArrayList<int[]> res = new ArrayList<int[]>();
        res.add(arrL.get(0)); //0
        res.add(arrL.get(4)); //1
        res.add(arrL.get(8)); //2
        res.add(arrL.get(12)); //3

        res.add(arrL.get(1)); //4
        res.add(arrL.get(5));
        res.add(arrL.get(9));
        res.add(arrL.get(13));

        res.add(arrL.get(2));
        res.add(arrL.get(6));
        res.add(arrL.get(10));
        res.add(arrL.get(14));


        res.add(arrL.get(3));
        res.add(arrL.get(7));
        res.add(arrL.get(11));
        res.add(arrL.get(15));

        return res;
    }

    public static ArrayList<byte[]> interleavePackets8x8(ArrayList<byte[]> arrL){
  /*    1	9	17	25	33	41	49	57

        2	10	18	26	34	42	50	58

        3	11	19	27	35	43	51	59

        4	12	20	28	36	44	52	60

        5	13	21	29	37	45	53	61

        6	14	22	30	38	46	54	62

        7	15	23	31	39	47	55	63

        8	16	24	32	40	48	56	64    copy numbers in order left to right, up to down*/
        ArrayList<byte[]> res = new ArrayList<byte[]>();

        res.add(arrL.get(0	));
        res.add(arrL.get(8  ));
        res.add(arrL.get(16 ));
        res.add(arrL.get(24 ));
        res.add(arrL.get(32 ));
        res.add(arrL.get(40 ));
        res.add(arrL.get(48 ));
        res.add(arrL.get(56 ));
        res.add(arrL.get(1  ));
        res.add(arrL.get(9 ));
        res.add(arrL.get(17 ));
        res.add(arrL.get(25 ));
        res.add(arrL.get(33 ));
        res.add(arrL.get(41 ));
        res.add(arrL.get(49 ));
        res.add(arrL.get(57 ));
        res.add(arrL.get(2  ));
        res.add(arrL.get(10 ));
        res.add(arrL.get(18 ));
        res.add(arrL.get(26 ));
        res.add(arrL.get(34 ));
        res.add(arrL.get(42 ));
        res.add(arrL.get(50 ));
        res.add(arrL.get(58 ));
        res.add(arrL.get(3  ));
        res.add(arrL.get(11 ));
        res.add(arrL.get(19 ));
        res.add(arrL.get(27 ));
        res.add(arrL.get(35 ));
        res.add(arrL.get(43 ));
        res.add(arrL.get(51 ));
        res.add(arrL.get(59 ));
        res.add(arrL.get(4  ));
        res.add(arrL.get(12 ));
        res.add(arrL.get(20 ));
        res.add(arrL.get(28 ));
        res.add(arrL.get(36 ));
        res.add(arrL.get(44 ));
        res.add(arrL.get(52 ));
        res.add(arrL.get(60 ));
        res.add(arrL.get(5 ));
        res.add(arrL.get(13 ));
        res.add(arrL.get(21 ));
        res.add(arrL.get(29 ));
        res.add(arrL.get(37 ));
        res.add(arrL.get(45 ));
        res.add(arrL.get(53 ));
        res.add(arrL.get(61 ));
        res.add(arrL.get(6  ));
        res.add(arrL.get(14 ));
        res.add(arrL.get(22 ));
        res.add(arrL.get(30 ));
        res.add(arrL.get(38 ));
        res.add(arrL.get(46 ));
        res.add(arrL.get(54 ));
        res.add(arrL.get(62 ));
        res.add(arrL.get(7  ));
        res.add(arrL.get(15 ));
        res.add(arrL.get(23 ));
        res.add(arrL.get(31 ));
        res.add(arrL.get(39 ));
        res.add(arrL.get(47 ));
        res.add(arrL.get(55 ));
        res.add(arrL.get(63 ));

        return res;
    }
    public static ArrayList<int[]> interleavePackets8x8test(ArrayList<int[]> arrL){
  /*    1	9	17	25	33	41	49	57

        2	10	18	26	34	42	50	58

        3	11	19	27	35	43	51	59

        4	12	20	28	36	44	52	60

        5	13	21	29	37	45	53	61

        6	14	22	30	38	46	54	62

        7	15	23	31	39	47	55	63

        8	16	24	32	40	48	56	64    copy numbers in order left to right, up to down*/
        ArrayList<int[]> res = new ArrayList<int[]>();

        res.add(arrL.get(0	));
        res.add(arrL.get(8  ));
        res.add(arrL.get(16 ));
        res.add(arrL.get(24 ));
        res.add(arrL.get(32 ));
        res.add(arrL.get(40 ));
        res.add(arrL.get(48 ));
        res.add(arrL.get(56 ));
        res.add(arrL.get(1  ));
        res.add(arrL.get(9 ));
        res.add(arrL.get(17 ));
        res.add(arrL.get(25 ));
        res.add(arrL.get(33 ));
        res.add(arrL.get(41 ));
        res.add(arrL.get(49 ));
        res.add(arrL.get(57 ));
        res.add(arrL.get(2  ));
        res.add(arrL.get(10 ));
        res.add(arrL.get(18 ));
        res.add(arrL.get(26 ));
        res.add(arrL.get(34 ));
        res.add(arrL.get(42 ));
        res.add(arrL.get(50 ));
        res.add(arrL.get(58 ));
        res.add(arrL.get(3  ));
        res.add(arrL.get(11 ));
        res.add(arrL.get(19 ));
        res.add(arrL.get(27 ));
        res.add(arrL.get(35 ));
        res.add(arrL.get(43 ));
        res.add(arrL.get(51 ));
        res.add(arrL.get(59 ));
        res.add(arrL.get(4  ));
        res.add(arrL.get(12 ));
        res.add(arrL.get(20 ));
        res.add(arrL.get(28 ));
        res.add(arrL.get(36 ));
        res.add(arrL.get(44 ));
        res.add(arrL.get(52 ));
        res.add(arrL.get(60 ));
        res.add(arrL.get(5 ));
        res.add(arrL.get(13 ));
        res.add(arrL.get(21 ));
        res.add(arrL.get(29 ));
        res.add(arrL.get(37 ));
        res.add(arrL.get(45 ));
        res.add(arrL.get(53 ));
        res.add(arrL.get(61 ));
        res.add(arrL.get(6  ));
        res.add(arrL.get(14 ));
        res.add(arrL.get(22 ));
        res.add(arrL.get(30 ));
        res.add(arrL.get(38 ));
        res.add(arrL.get(46 ));
        res.add(arrL.get(54 ));
        res.add(arrL.get(62 ));
        res.add(arrL.get(7  ));
        res.add(arrL.get(15 ));
        res.add(arrL.get(23 ));
        res.add(arrL.get(31 ));
        res.add(arrL.get(39 ));
        res.add(arrL.get(47 ));
        res.add(arrL.get(55 ));
        res.add(arrL.get(63 ));

        return res;
    }
    public static byte[] encryptData(byte[] arr, int key) throws IOException {
        byte[] res = new byte[512];
        int[] intRes = new int[128];
        int fourByte =0;
        ByteBuffer plainText = ByteBuffer.wrap(arr);
        for (int j = 0; j < arr.length / 4; j++) {
            fourByte = plainText.getInt();
            fourByte = fourByte ^ key;// XOR operation with keyunwrapEncrypt.putInt(fourByte); }
            intRes[j] = fourByte;
        }
        return integersToBytes(intRes);
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
//testing the interleavers
    public static void main(String[] args) {
        int[] a1 = {1};
        int[] a2 = {2};
        int[] a3 = {3};
        int[] a4 = {4};
        int[] a5 = {5};
        int[] a6 = {6};
        int[] a7 = {7};
        int[] a8 = {8};
        int[] a9 = {9};
        int[] a10 = {10};
        int[] a11 = {11};
        int[] a12 = {12};
        int[] a13 = {13};
        int[] a14 = {14};
        int[] a15 = {15};
        int[] a16 = {16};
        ArrayList<int[]> ints = new ArrayList<>();
        ints.add(a1);
        ints.add(a2);
        ints.add(a3);
        ints.add(a4);
        ints.add(a5);
        ints.add(a6);
        ints.add(a7);
        ints.add(a8);
        ints.add(a9);
        ints.add(a10);
        ints.add(a11);
        ints.add(a12);
        ints.add(a13);
        ints.add(a14);
        ints.add(a15);
        ints.add(a16);
        System.out.println("before interleave");
        for (int [] a:ints) {
            System.out.print(Arrays.toString(a) + ", ");
        }
        ints = interleavePackets4x4test(ints);
        System.out.println("after interleave");
        for (int [] a:ints) {
            System.out.print(Arrays.toString(a) + ", ");
        }
        ints = interleavePackets4x4test(ints);
        System.out.println("after interleave again");
        for (int [] a:ints) {
            System.out.print(Arrays.toString(a) + ", ");
        }
        int[] a17 = {17};
        int[] a18 = {18};
        int[] a19 = {19};
        int[] a20 = {20};
        int[] a21 = {21};
        int[] a22 = {22};
        int[] a23 = {23};
        int[] a24 = {24};
        int[] a25 = {25};
        int[] a26 = {26};
        int[] a27 = {27};
        int[] a28 = {28};
        int[] a29 = {29};
        int[] a30 = {30};
        int[] a31 = {31};
        int[] a32 = {32};
        int[] a33 = {33};
        int[] a34 = {34};
        int[] a35 = {35};
        int[] a36 = {36};
        int[] a37 = {37};
        int[] a38 = {38};
        int[] a39 = {39};
        int[] a40 = {40};
        int[] a41 = {41};
        int[] a42 = {42};
        int[] a43 = {43};
        int[] a44 = {44};
        int[] a45 = {45};
        int[] a46 = {46};
        int[] a47 = {47};
        int[] a48 = {48};
        int[] a49 = {49};
        int[] a50 = {50};
        int[] a51 = {51};
        int[] a52 = {52};
        int[] a53 = {53};
        int[] a54 = {54};
        int[] a55 = {55};
        int[] a56 = {56};
        int[] a57 = {57};
        int[] a58 = {58};
        int[] a59 = {59};
        int[] a60 = {60};
        int[] a61 = {61};
        int[] a62 = {62};
        int[] a63 = {63};
        int[] a64 = {64};

        ints.add(a17);
        ints.add(a18);
        ints.add(a19);
        ints.add(a20);
        ints.add(a21);
        ints.add(a22);
        ints.add(a23);
        ints.add(a24);
        ints.add(a25);
        ints.add(a26);
        ints.add(a27);
        ints.add(a28);
        ints.add(a29);
        ints.add(a30);
        ints.add(a31);
        ints.add(a32);
        ints.add(a33);
        ints.add(a34);
        ints.add(a35);
        ints.add(a36);
        ints.add(a37);
        ints.add(a38);
        ints.add(a39);
        ints.add(a40);
        ints.add(a41);
        ints.add(a42);
        ints.add(a43);
        ints.add(a44);
        ints.add(a45);
        ints.add(a46);
        ints.add(a47);
        ints.add(a48);
        ints.add(a49);
        ints.add(a50);
        ints.add(a51);
        ints.add(a52);
        ints.add(a53);
        ints.add(a54);
        ints.add(a55);
        ints.add(a56);
        ints.add(a57);
        ints.add(a58);
        ints.add(a59);
        ints.add(a60);
        ints.add(a61);
        ints.add(a62);
        ints.add(a63);
        ints.add(a64);
        System.out.println("before interleave");
        for (int [] a:ints) {
            System.out.print(Arrays.toString(a) + ", ");
        }
        ints = interleavePackets8x8test(ints);
        System.out.println("after interleave");
        for (int [] a:ints) {
            System.out.print(Arrays.toString(a) + ", ");
        }
        ints = interleavePackets8x8test(ints);
        System.out.println("after interleave again");
        for (int [] a:ints) {
            System.out.print(Arrays.toString(a) + ", ");
        }
    }
}

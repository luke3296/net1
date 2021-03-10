import java.util.ArrayList;

public class HelperClass {

    static class Matrix {
        byteArrContainer[][] dataBlock = null;

        public Matrix(byteArrContainer[][] b) {
            dataBlock = b;
        }

        public Matrix(ArrayList<byte[]> bytesArray, int w, int h) {
            dataBlock = new byteArrContainer[w][h];
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    assert (i < 4 && j < 4);
                    assert ((j * w) + 1 < 15);
                    dataBlock[i][j] = new byteArrContainer(bytesArray.get(j * w + i), j * w + i);
                }
            }
        }

        public Matrix transpose() {
            int height = this.dataBlock.length; //rows
            int width = this.dataBlock[0].length; //cols
            byteArrContainer[][] res = new byteArrContainer[width][height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    res[i][j] = this.dataBlock[j][i];
                  //  System.out.print("#" + this.dataBlock[i][j].packetNum + "#");

                }
            }
            this.dataBlock = res;
            Matrix m = new Matrix(res);
            m.printMatrix();
            return new Matrix(res);
        }

        public void printMatrix() {
            for (byteArrContainer[] b : dataBlock) {
                for (byteArrContainer bb : b) {
                    System.out.print(bb.packetNum + " , ");
                }
            }
        }
        public ArrayList<byte[]> getList(){
            ArrayList<byte[]> res = new ArrayList<>();
            int height = this.dataBlock.length; //rows
            int width = this.dataBlock[0].length;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j <height ; j++) {
                    res.add(this.dataBlock[i][j].arr);
                }
            }
            return res;
        }

        public ArrayList<byte[]> getPacketList() {
            int height = this.dataBlock.length; //rows
            int width = this.dataBlock[0].length; //cols
            ArrayList<byte[]> arr = new ArrayList<byte[]>();
            byteArrContainer[][] res = new byteArrContainer[width][height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    res[i][j] = this.dataBlock[j][i];
                    // System.out.print("#" + this.dataBlock[i][j].packetNum + "#");
                    arr.add(this.dataBlock[j][i].arr);
                }
            }
            return arr;
        }

        public ArrayList<byte[]> getPacketList2() {
            int height = this.dataBlock.length; //rows
            int width = this.dataBlock[0].length; //cols
            ArrayList<byte[]> arr = new ArrayList<byte[]>();
            byteArrContainer[][] res = new byteArrContainer[width][height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    res[i][j] = this.dataBlock[j][i];
                   // System.out.print("#" + this.dataBlock[i][j].packetNum + "#");
                    arr.add(this.dataBlock[i][j].arr);
                }
            }
            return arr;
        }

        public ArrayList<byte[]> getPacketList3(){
            int height = this.dataBlock.length; //rows
            int width = this.dataBlock[0].length; //cols
            ArrayList<byte[]> arr = new ArrayList<byte[]>();
            byteArrContainer[][] res = new byteArrContainer[width][height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    res[i][j] = this.dataBlock[j][i];
                  //  System.out.print("#" + this.dataBlock[i][j].packetNum + "#");
                    arr.add(this.dataBlock[j][i].arr);
                }
            }
            return arr;
        }

        public ArrayList<byte[]> getPacketList4(){
            int height = this.dataBlock.length; //rows
            int width = this.dataBlock[0].length; //cols
            ArrayList<byte[]> arr = new ArrayList<byte[]>();
            byteArrContainer[][] res = new byteArrContainer[width][height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    res[i][j] = this.dataBlock[j][i];
                    //  System.out.print("#" + this.dataBlock[i][j].packetNum + "#");
                    arr.add(this.dataBlock[i][j].arr);
                }
            }
            return arr;
        }
        }

        static class byteArrContainer {
            int packetNum;
            byte[] arr;

            public byteArrContainer(byte[] arr, int packetNum) {
                this.arr = arr;
                this.packetNum = packetNum;
            }

            public byte[] getByteArray() {
                return this.arr;
            }

            public int getPacketNum() {
                return this.packetNum;
            }
        }
        public static void test(){
        ArrayList<byte[]> arr = new ArrayList<>();
        byte[] arr1 = new String("A").getBytes();
        byte[] arr2 = new String("B").getBytes();
        byte[] arr3 = new String("C").getBytes();
        byte[] arr4 = new String("D").getBytes();
        byte[] arr5 = new String("E").getBytes();
        byte[] arr6 = new String("F").getBytes();
        byte[] arr7 = new String("G").getBytes();
        byte[] arr8 = new String("H").getBytes();
        byte[] arr9 = new String("I").getBytes();
        byte[] arr10 = new String("J").getBytes();
        byte[] arr11= new String("K").getBytes();
        byte[] arr12 = new String("L").getBytes();
        byte[] arr13 = new String("M").getBytes();
        byte[] arr14 = new String("N").getBytes();
        byte[] arr15 = new String("O").getBytes();
        byte[] arr16 = new String("P").getBytes();
            arr.add(arr1);
            arr.add(arr2);
            arr.add(arr3);
            arr.add(arr4);
            arr.add(arr5);
            arr.add(arr6);
            arr.add(arr7);
            arr.add(arr8);
            arr.add(arr9);
            arr.add(arr10);
            arr.add(arr11);
            arr.add(arr12);
            arr.add(arr13);
            arr.add(arr14);
            arr.add(arr15);
            arr.add(arr16);
            Matrix m = new Matrix(arr, 4,4);
            m.printMatrix();


        }

    public static void main(String[] args) {
        test();
    }
    }
/*
 0 , 4 , 8 , 12
 1 , 5 , 9 , 13
 2 , 6 , 10 , 14
  3 , 7 , 11 , 15
 * */
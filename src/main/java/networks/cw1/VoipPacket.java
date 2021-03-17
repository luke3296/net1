package networks.cw1;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class VoipPacket {

    public static final VoipPacket EMPTY = new VoipPacket((short) 0, new byte[512]);
    public final short authKey;
    //public final int dhSharedkey;
    public final byte[] data;
    CRC32 chksum;
    int computedChksum;
    int pktNum;
    static int pktCount = 0;

    public VoipPacket(short authkey, byte[] data) {
        this(authkey, data, 0, 0);
        pktCount++;
        this.pktNum = pktCount;
    }
    private VoipPacket(short authKey, byte[] data, int chksum, int pktNum) {
        this.authKey = authKey;
        //this.dhSharedkey = dhSharedkey;
        this.data = data;
        this.chksum = new CRC32();
        this.chksum.update(data);
        this.computedChksum = chksum;
        this.pktNum = pktNum;
    }

    public byte[] encode() {
        ByteBuffer buff = ByteBuffer.allocate(512 + Short.BYTES + (Integer.BYTES * 2));
        buff.putShort(authKey);
        buff.putInt(pktNum);
        buff.putInt((int) chksum.getValue());
        //buff.putInt(dhSharedkey);
        buff.put(data);
        return buff.array();
    }


    public static VoipPacket from(ByteBuffer buff) {
        short authKey = buff.getShort();
        //int dhSharedKey = buff.getInt();
        int pktNum = buff.getInt();
        int computedChksum = buff.getInt();
        byte[] data = new byte[512];
        buff.get(data);
        return new VoipPacket(authKey, data, computedChksum, pktNum);
    }

    public boolean checkData(int counter) {
        return this.computedChksum == (int)chksum.getValue();
    }


}

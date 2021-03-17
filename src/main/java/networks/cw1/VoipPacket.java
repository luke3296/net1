package networks.cw1;

import java.nio.ByteBuffer;

public class VoipPacket {

    public final short authKey;
    //public final int dhSharedkey;
    public final byte[] data;

    public VoipPacket(short authkey, byte[] data) {
        this.authKey = authkey;
        //this.dhSharedkey = dhSharedkey;
        this.data = data;
    }

    public byte[] encode() {
        ByteBuffer buff = ByteBuffer.allocate(512 + Short.BYTES + Integer.BYTES);
        buff.putShort(authKey);
        //buff.putInt(dhSharedkey);
        buff.put(data);
        return buff.array();
    }


    public static VoipPacket from(ByteBuffer buff) {
        short authKey = buff.getShort();
        //int dhSharedKey = buff.getInt();
        byte[] data = new byte[512];
        buff.get(data);
        return new VoipPacket(authKey, data);
    }


}

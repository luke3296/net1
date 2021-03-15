package networks.cw1;




import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Packet {

    public final short authkey;
    public final byte[] data;

    public Packet(short authkey, byte[] data) {
        this.authkey = authkey;
        this.data = data;
    }

    public void SendPacket(DatagramPacket packet) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + Short.BYTES);
        buffer.putShort(authkey);
        buffer.put(data, 0, data.length);
        packet.setData(buffer.array());
    }

    public static Packet ReceivePacket(DatagramPacket packet) {
        ByteBuffer buffer  = ByteBuffer.wrap(packet.getData());
        short authkey = buffer.getShort();
        byte[] data = new byte[512];
        buffer.get(data);
        return new Packet(authkey, data);
    }

}
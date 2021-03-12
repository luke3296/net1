package networks.cw1;

public class Main {
    /*
Project : A VoIP stack in Java
Will follow a layered architecture we use a datagram socket as our network primitive meaning we implememnt transport layer up,
this is because UDP by itself wont provide reliability in packet transmission

Read Audio In -> packetize -> produce UDP packets -> sendUDP packet
Read packet In -> unpacketize  -> read payload -> play payload

Peer processes :
    encryptor / decryptor should be applied to the payload before attaching to the packet / reading out from packet. This should happen above the session level
    but work in such a way that its transparent to the application

    packetizer : turns data into chuncks of uniform size 512B  / unpacketiser undoes this

    session management functions: appdend a seq number to the header and reorder on the reciever side. Clinet should have a timeout where it requests retransmission
    Interleaving process e.g send packets non consecutively  ! not doing this we dont need ordering or retrasmission

    s1: send and recieve text on local host       /
    s2: send and recieve audio on local host      /

    s3: encrypt send audio                        /
    s4: decrypt recieved audio                    /

    s5: add header                                /
    s6: strip header                              /

    s7: collect subset of packets say 16 and interleave        /
    s8: recieve subset of packets and un-interleave            /

    s9: More robust msg Auth with the header

    AudioRecieve:
    run->
        genKey
        if Key
            getdata
            interlv

 */
    public static void main(String[] args) {
        audioSendThread sender = new audioSendThread();
        audioRecieveThread recieve = new audioRecieveThread();
        recieve.start();
        sender.start();
    }

}

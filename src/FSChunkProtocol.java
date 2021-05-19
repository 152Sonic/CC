import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public class FSChunkProtocol{




    public static void sendToGw(DatagramSocket ds, String msg, int porta, InetAddress ip){
        DatagramPacket p = null;
        try{
            byte[] data = msg.getBytes();
            p = new DatagramPacket(data, data.length,ip,porta);
            ds.send(p);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(p.getPort());
    }

    public static DatagramPacket receiveFromServers(DatagramSocket ds){
        byte [] buf = new byte [256];
        DatagramPacket p = new DatagramPacket(buf, 256);;
        try {
            ds.receive(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int porta = p.getPort();
        InetAddress ip = p.getAddress();
        DatagramPacket packet = new DatagramPacket(buf, 256, ip, porta);
        //System.out.println("Ds-porta: " + ds.getPort());
        //p.setPort(ds.getPort());
        //p.setAddress(ds.getInetAddress());

        System.out.println("Pacote-porta: " + packet.getPort());
        return packet;
    }

    public static void sendToServers(DatagramSocket ds, String msg, int porta, InetAddress ip){
        try{
            byte[] data = msg.getBytes();
            //for (InetAddress ip: s.keySet()) {
                DatagramPacket p = new DatagramPacket(data, data.length,ip,porta);
                ds.send(p);
            //}
        }  catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static DatagramPacket receiveFromGw(DatagramSocket ds){
        byte [] buf = new byte [256];
        int length=256;
        int porta = 0;
        DatagramPacket p = null;
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            p = new DatagramPacket(buf, length,ip,porta);
            ds.receive(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;

    }

    
}
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public class FSChunkProtocol{




    public static void sendToGw(DatagramSocket ds, DatagramPacket dp, InetAddress ip, int port){
        try{
            //ds.connect(ip, port);;
            System.out.println(dp.getPort());
            ds.send(dp);
            //System.out.println("Fds");
        }  catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(p.getPort());
    }

    public static Packet receiveFromServers(DatagramSocket ds){
        byte [] buf = new byte [1024];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        try {
            ds.receive(p);
            System.out.println("Recebi");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Estou aqui: " + p.getPort());
        System.out.println("IP: " + p.getAddress());
        Packet pckt = null;
        try {
            pckt = new Packet(buf);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //System.out.println("Ds-porta: " + ds.getPort());
        //p.setPort(ds.getPort());
        //p.setAddress(ds.getInetAddress());
        return pckt;
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
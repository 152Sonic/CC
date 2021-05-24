import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;


public class FSChunkProtocol{




    public static void sendToGw(DatagramSocket ds, DatagramPacket dp){
        try{
            ds.send(dp);
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Packet receiveFromServer(DatagramSocket ds){
        byte [] buf = new byte [1024];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        try {
            ds.receive(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static Packet receiveFromGw(DatagramSocket ds){
        byte [] buf = new byte [1024];
        DatagramPacket p = new DatagramPacket(buf,buf.length);
        try {
            ds.receive(p);
            System.out.println("recebido");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Packet pckt = null;
        try {
            pckt = new Packet(buf);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return pckt;

    }

    
}

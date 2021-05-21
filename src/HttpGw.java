import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpGw {
    private DatagramSocket ds_envio;
    private DatagramSocket ds_rececao;
    private Map<InetAddress,Integer> servers;
    private int porta;
    private InetAddress ip;

    public HttpGw (int porta){
        this.porta = porta;
        try {
            this.ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        try {
            this.ds_envio = new DatagramSocket();
            this.ds_rececao = new DatagramSocket(this.porta);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.servers = new HashMap<>();
    }

    public void runGW (){
        while (true){
            new Thread(() -> {
                Packet p = FSChunkProtocol.receiveFromServers(ds_rececao);
                System.out.println("Cheguei, vim da porta: " + p.getPorta());
                System.out.println(p.toString());
            }).start();
            new Thread(() -> {
                //FSChunkProtocol.sendToServers(ds_envio,"Xau",4300,ip);
            }).start();
        }
    }

    public static void main (String [] args){
        HttpGw gw = new HttpGw(4200);
        gw.runGW();
    }
}

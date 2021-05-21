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
    private Map<Integer,Packet> packets;
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
        this.packets = new HashMap<>();
    }

    public void addPacket(Packet p){
        packets.put(p.getPorta(), p);
    }

    /*
    public void gerirPedido(Pedido ped){
        for (Packet p : packets.values()) {
            if (p.getFileName() == ped.getFileName()){
                if(p.getTipo() == 1){
                    Packet p2 = new Packet(6, 2, porta, 0, "aceito", 0, 0, "pdf");
                    DatagramPacket dp2 = new DatagramPacket(p2.toBytes(), p2.toBytes().length, p.getIP(), p.getPorta());
                    FSChunkProtocol.sendToGw(ds_envio, dp2);
                    break; -> este break acontece se nao tivermos em conta a possibilidade haver frgamentações
                }
                else if(p.getTipo() == 4){
                    Ja temos o ficheiro para mandar para o cliente
                }
            }
        }
    }
    */

    public void runGW () throws UnknownHostException{
        new Thread(() -> {
            while (true){
                Packet p = FSChunkProtocol.receiveFromServer(ds_rececao);
                System.out.println("Cheguei, vim da porta: " + p.getPorta());
                addPacket(p);
            }
        }).start();

        while(true){
            //for pedido in pedidos:
                //new Thread(() -> {
                    //gerirPedido(pedido);
                //}).start();
        }

    }

    public static void main (String [] args) throws UnknownHostException{
        HttpGw gw = new HttpGw(4200);
        gw.runGW();
    }
}

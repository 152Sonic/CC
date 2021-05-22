import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Server{
    private int porta;
    private InetAddress ip;
    private int estado; //0 -> livre; 1 -> ocupado

    public Server (int porta, InetAddress ip, int estado){
        this.porta = porta;
        this.ip = ip;
        this.estado = estado;
    }

    public int getEstado(){
        return this.estado;
    }

    public int getPorta() {
        return porta;
    }

    public InetAddress getIp() {
        return ip;
    }
}

public class HttpGw {
    private DatagramSocket ds_envio;
    private DatagramSocket ds_rececao;
    private ServerSocket ss;
    private DataInputStream dis;
    private DataOutputStream dos;
    private List<Server> servers;
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
            this.ss = new ServerSocket(this.porta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.servers = new ArrayList<Server>();
    }

    public void addServer(int porta, InetAddress ip){
        Server s = new Server(porta,ip,0);
        servers.add(s);
    }


    public void gerirPedido(String ped) throws IOException {
        System.out.println("ola");
        String pedido = "pedido";
        byte[] pedido_buffer = pedido.getBytes();
        Packet pacote = new Packet(2,4200,InetAddress.getLocalHost().getHostAddress(), 0,pedido_buffer,0,0,ped);
        for(Server s : servers){
            if(s.getEstado()==0) {
                DatagramPacket dp = new DatagramPacket(pacote.toBytes(), pacote.toBytes().length, s.getIp(),s.getPorta());
                ds_envio.send(dp);
                break;
            }
        }

    }


    public void gerirPacket(Packet p) throws UnknownHostException{
        if (p.getTipo() == 1) addServer(p.getPorta(), p.getIP());
        else if (p.getTipo() == 3);
    }

    public void runGW () throws UnknownHostException{
        new Thread(() -> {
            while (true){
                Packet p = FSChunkProtocol.receiveFromServer(ds_rececao);
                System.out.println("Cheguei, vim da porta: " + p.toString());
                try {
                    gerirPacket(p);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Socket s = ss.accept();
                    dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                    String p = dis.readLine();
                    String[] tokens = p.split(" ");
                    p = tokens[1].substring(1);
                    System.out.println(p);
                    gerirPedido(p);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static void main (String [] args) throws UnknownHostException{
        HttpGw gw = new HttpGw(4200);
        gw.runGW();
    }
}

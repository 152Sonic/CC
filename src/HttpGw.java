import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Server{
    private int porta;
    private String ip;
    private int estado; //0 -> livre; 1 -> ocupado

    public Server (int porta, String ip, int estado){
        this.porta = porta;
        this.ip = ip;
        this.estado = estado;
    }

    public int getEstado(){
        return this.estado;
    }

    public int getPorta() {
        return this.porta;
    }

    public String getIp() {
        return this.ip;
    }

    public InetAddress getInetAddress() throws UnknownHostException{
        return InetAddress.getByName(this.ip);
    }
}

public class HttpGw {
    private DatagramSocket ds_envio;
    private DatagramSocket ds_rececao;
    private ServerSocket ss;
    private List<Server> servers;
    private int porta;
    private InetAddress ip;
    private Map<Integer,Socket> clientes;

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
	    this.clientes = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.servers = new ArrayList<Server>();
    }

    public void addServer(int porta, String ip){
        Server s = new Server(porta,ip,0);
        servers.add(s);
    }


    public void gerirPedido(String ped, Socket sckt) throws IOException {
        System.out.println("ola");
        byte[] pedido_buffer = ped.getBytes();
        Packet pacote = new Packet(2,clientes.size(),4200,InetAddress.getLocalHost().getHostAddress(), 0,pedido_buffer,0,0);
	clientes.put(clientes.size(),sckt);
        System.out.println(servers);
        for(Server s : servers) {
            if (s.getEstado() == 0) {
                System.out.print("vou mandar para: " + s.getInetAddress() + "   " + s.getPorta());
                DatagramPacket dp = new DatagramPacket(pacote.toBytes(), pacote.toBytes().length, s.getInetAddress(), s.getPorta());
                ds_envio.send(dp);
                break;
            }
        }

    }


    public void gerirPacket(Packet p) throws IOException {
        System.out.println("server->porta: " + p.getPorta() + ", ip: " + p.getIP());
        if (p.getTipo() == 1) addServer(p.getPorta(), p.getIP());
        else if (p.getTipo() == 3){
            byte[] file = p.getData();
            Socket s = clientes.get(p.getId());
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            String fileout = new String(file, StandardCharsets.UTF_8);
            dos.writeUTF(fileout);
            dos.flush();
            s.close();
		
	    }
    }

    public void runGW () throws UnknownHostException{
        new Thread(() -> {
            while (true){
                Packet p = FSChunkProtocol.receiveFromServer(ds_rececao);
                System.out.println("pacote recebido: " + p.toString());
                try {
                    gerirPacket(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Socket s = ss.accept();
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    String p = dis.readLine();
                    String[] tokens = p.split(" ");
                    p = tokens[1].substring(1);
                    System.out.println(p);
                    gerirPedido(p,s);

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

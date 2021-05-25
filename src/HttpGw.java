import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HttpGw {
    private DatagramSocket ds_envio;
    private DatagramSocket ds_rececao;
    private ServerSocket ss;
    private Map<String,Server> servers;
    private Map<Integer,List<Packet>> frags;
    private int porta;
    private InetAddress ip;
    private Map<Integer,Socket> clientes;
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock rl = lock.readLock();
    private Lock wl = lock.writeLock();


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
	        this.frags = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.servers = new HashMap<>();
    }

    public void addServer(int porta, String ip){
        Server s = new Server(porta,ip,0);
        servers.put(ip, s);
    }


    public void gerirPedido(String ped, Socket sckt) throws IOException {
        System.out.println("ola");
        byte[] pedido_buffer = ped.getBytes();
        Packet pacote = new Packet(2,clientes.size(),4200,InetAddress.getLocalHost().getHostAddress(), 0,pedido_buffer,0,0);
	clientes.put(clientes.size(),sckt);
        System.out.println(servers);
        for(Server s : servers.values()) {
                if (s.getEstado() == 0) {
                    System.out.print("vou mandar para: " + s.getInetAddress() + "   " + s.getPorta());
                    DatagramPacket dp = new DatagramPacket(pacote.toBytes(), pacote.toBytes().length, s.getInetAddress(), s.getPorta());
                    ds_envio.send(dp);
                    s.setEstado(1);
                    break;
                }
            }
        }



    public void gerirPacket(Packet p) throws IOException {
        System.out.println("server->porta: " + p.getPorta() + ", ip: " + p.getIP());
        String fim = "";
        if (p.getTipo() == 1) addServer(p.getPorta(), p.getIP());
        else if (p.getTipo() == 3 && p.getFrag()==0){
            if(!frags.containsKey(p.getId())) {
                byte[] file = p.getData();
                Socket s = clientes.get(p.getId());
                servers.get(p.getIP()).setEstado(0);
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                String fileout = new String(file, StandardCharsets.UTF_8);
                dos.writeUTF(fileout);
                dos.flush();
                s.close();
            }
            else{
                List<Packet> f = frags.get(p.getId());
                Socket s = clientes.get(p.getId());
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                byte[] file;
                for(Packet pcs: f){
                    //System.out.println("\n" + pcs.toString() + "\b");
                    file = pcs.getData();
                    String fileout = new String(file, StandardCharsets.UTF_8);
                    fim+=fileout;
                    //dos.writeUTF(fileout);
                }
                System.out.println("Puta que pariu" + p.toString());
                file = p.getData();
                System.out.println(file.length);
                String fileout = new String(file, StandardCharsets.UTF_8);
                fim+=fileout;
                dos.writeUTF(fim);
                dos.flush();
                s.close();
            }
            servers.get(p.getIP()).setEstado(0);
	    }
        else if (p.getTipo() == 3 && p.getFrag()==1) {
            if(!frags.containsKey(p.getId())){
                List<Packet> lista = new ArrayList<>();
                lista.add(p);
                frags.put(p.getId(),lista);
            }
            else{
                frags.get(p.getId()).add(p);
            }
        }

        else if (p.getTipo() == 5){
            String ip = p.getIP();
            Server s = servers.get(ip);
            long x = System.nanoTime()/1000000000;
            //wl.lock();
            s.setTempo(x);
            //wl.unlock();
        }
    }

    public void runGW () throws IOException {
        new Thread(() -> {
            while(true) {
                try {
                    List<String> ips = new ArrayList<>();
                    for (Server s : servers.values()) {
                        long x = System.nanoTime()/1000000000;
                        long m = s.getTempo();
                        long minus = x - m;
                        System.out.println(minus);
                        if(minus> 10){
                            ips.add(s.getIp());
                        }
                    }
                    for(String ip:ips){
                        servers.remove(ip);
                    }
                    System.out.println(servers.keySet().toString());
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        while (true){
            Packet p = FSChunkProtocol.receiveFromServer(ds_rececao);
        new Thread(() -> {
                System.out.println("pacote recebido: ");
                try {
                    gerirPacket(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        new Thread(() -> {
                try {
                    Socket s = ss.accept();
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    String pct = dis.readLine();
                    String[] tokens = pct.split(" ");
                    pct = tokens[1].substring(1);
                    System.out.println(pct);
                    gerirPedido(pct,s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    public static void main (String [] args) throws IOException {
        HttpGw gw = new HttpGw(4200);
        gw.runGW();
    }
}

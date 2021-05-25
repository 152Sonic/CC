import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HttpGw {
    private DatagramSocket ds_envio;
    private DatagramSocket ds_rececao;
    private ServerSocket ss;
    private Map<Integer,Server> servers;
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
        wl.lock();
        servers.put(porta, s);
        wl.unlock();
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
                    wl.lock();
                    s.setEstado(1);
                    wl.unlock();
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
                servers.get(p.getPorta()).setEstado(0);
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                //String fileout = new String(file, StandardCharsets.UTF_8);
                dos.write(file,0,file.length);
                dos.flush();
                s.close();
                System.out.println("fechei");
            }
            else{
                List<Packet> f = frags.get(p.getId());
                Collections.sort(f,new SortByOff());
                Socket s = clientes.get(p.getId());
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                byte[] file;
                for(Packet pcs: f){
                    //System.out.println("\n" + pcs.toString() + "\b");
                    file = pcs.getData();
                    //String fileout = new String(file, StandardCharsets.UTF_8);
                   // fim+=fileout;
                    //dos.writeUTF(fileout);
                    dos.write(file,0,file.length);
                }
                //System.out.println("Puta que pariu" + p.toString());
                file = p.getData();
                //System.out.println(file.length);
                //String fileout = new String(file, StandardCharsets.UTF_8);
                //fim+=fileout;
                //dos.writeUTF(fim);
                dos.write(file,0,file.length);
                dos.flush();
                servers.get(p.getPorta()).setEstado(0);
                s.close();
            }
            servers.get(p.getPorta()).setEstado(0);
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
            Server s = servers.get(p.getPorta());
            long x = System.nanoTime()/1000000000;
            wl.lock();
            s.setTempo(x);
            wl.unlock();
        }

        else if (p.getTipo() == 6){
            Socket s = clientes.get(p.getId());
            PrintWriter out = new PrintWriter(s.getOutputStream());
            out.println("Ficheiro não existe!");
            out.flush();
            servers.get(p.getPorta()).setEstado(0);
            s.close();
        }
    }

    public void runGW () throws IOException {
        new Thread(() -> {
            while(true) {
                try {
                    List<Integer> ips = new ArrayList<>();
                    for (Server s : servers.values()) {
                        long x = System.nanoTime()/1000000000;
                        long m = s.getTempo();
                        long minus = x - m;
                        System.out.println(minus);
                        if(minus> 10){
                            ips.add(s.getPorta());
                        }
                    }
                    for(Integer ip:ips){
                        wl.lock();
                        servers.remove(ip);
                        wl.unlock();
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

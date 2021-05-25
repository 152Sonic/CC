import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FastFileSrv{
    private String ip;
    private String ip_destino;
    private Lock l;
    private DatagramSocket ds_envio;
    private DatagramSocket ds_rececao;
    private int porta;
    private byte[] file;


    public FastFileSrv (int porta, String ipd) {
        this.porta = porta; //porta é indicada no terminal mas agora esta assim
        try {
            this.ds_envio = new DatagramSocket();
            this.ds_rececao = new DatagramSocket(this.porta);
            this.ip = InetAddress.getLocalHost().getHostAddress();
            this.ip_destino = ipd;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.l = new ReentrantLock();
    }

    public void sendBeacons() throws IOException{
        String s = "";
        byte [] msg = s.getBytes();
        Packet beacon = new Packet(5,-1,porta,ip,0,msg,0,0);
        DatagramPacket b1 = new DatagramPacket(beacon.toBytes(),beacon.toBytes().length, InetAddress.getByName(ip_destino),4200);
        FSChunkProtocol.sendToGw(ds_envio, b1);
    }


    public void runServer () throws IOException{
        String s = "olá";
        byte [] msg = s.getBytes();
        System.out.println(s);
        //estabelecer ligaçao
        System.out.println(ip);
        Packet p1 = new Packet(1, -1,porta, ip, 0, msg, 0, 0);
        DatagramPacket dp1 = new DatagramPacket(p1.toBytes(), p1.toBytes().length, InetAddress.getByName(ip_destino), 4200);
        FSChunkProtocol.sendToGw(ds_envio, dp1);

        new Thread(() -> {
            while(true)
                try {
                    sendBeacons();
                    Thread.sleep(5000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
        }).start();
        
        while(true){
            Packet p = FSChunkProtocol.receiveFromGw(ds_rececao);
            System.out.println(p.toString());
            new Thread(() -> {
                System.out.println("recebido");
                System.out.println(p.toString());
                // gerir o pacote
                if (p.getTipo() == 2){
                    Charset charset = StandardCharsets.US_ASCII;
                    String filename = charset.decode(ByteBuffer.wrap(p.getData()))
				.toString();
                    filename = filename.replace("\0","");
                    File file = new File("../" + filename);
                    try {
                        double tam = Files.size(file.toPath());
                        System.out.println(tam);
                        if (tam > 996){
                            int off = 0;
                            byte[] filecontent = Files.readAllBytes(file.toPath());
                            byte[] buff = new byte[996];
                            for(;off<tam;off+=996){
                                if(off+996>tam){
                                    System.out.println("ficheiro lido" + off);
                                    byte[]fim = new byte[(int)tam-off];
                                    System.arraycopy(filecontent,off,fim,0,(int) tam-off);
                                    Packet pckt = new Packet(3, p.getId(), porta, ip, 0, fim, 0, 0);
                                    System.out.println(pckt.toString());
                                    DatagramPacket pac = new DatagramPacket(pckt.toBytes(), pckt.toBytes().length, InetAddress.getByName(ip_destino), 4200);
                                    FSChunkProtocol.sendToGw(ds_envio, pac);
                                }
                                else{
                                    System.arraycopy(filecontent,off,buff,0,996);
                                    System.out.println("ficheiro lido" + off);
                                    Packet pckt = new Packet(3, p.getId(), porta, ip, 1, buff, 0, 0);
                                    DatagramPacket pac = new DatagramPacket(pckt.toBytes(), pckt.toBytes().length, InetAddress.getByName(ip_destino), 4200);
                                    FSChunkProtocol.sendToGw(ds_envio, pac);
                                }
                            }

                        }
                        else {
                            byte[] filecontent = Files.readAllBytes(file.toPath());
                            Packet pckt = new Packet(3, p.getId(), porta, ip, 0, filecontent, 0, 0);
                            DatagramPacket pac = new DatagramPacket(pckt.toBytes(), pckt.toBytes().length, InetAddress.getByName(ip_destino), 4200);
                            FSChunkProtocol.sendToGw(ds_envio, pac);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                    else if (p.getTipo() == 4){
                        //ds_envio.close()
                    }
            }).start();
        }
        
            
        
    }
	

    public static void main(String [] args) throws IOException{
        System.out.println(args[1]);
        FastFileSrv s = new FastFileSrv(Integer.parseInt(args[0]),args[1]);
        s.runServer();
    }
}

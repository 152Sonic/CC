import java.io.File;
import java.io.IOException;
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


    public void runServer () throws IOException{
        String s = "Ola ta tudo top";
        byte [] msg = s.getBytes();
        System.out.println(s);
        //estabelecer ligaçao
        System.out.println(ip);
        Packet p1 = new Packet(1, -1,porta, ip, 0, msg, 0, 0);
        DatagramPacket dp1 = new DatagramPacket(p1.toBytes(), p1.toBytes().length, InetAddress.getByName(ip_destino), 4200);
        FSChunkProtocol.sendToGw(ds_envio, dp1);

        new Thread(() -> {
            while(true){
                Packet p = FSChunkProtocol.receiveFromGw(ds_rececao);
                System.out.println("recebido");
                System.out.println(p.toString());
                // gerir o pacote
                if (p.getTipo() == 2){
                    Charset charset = StandardCharsets.US_ASCII;
                    String filename = charset.decode(ByteBuffer.wrap(p.getData()))
				.toString();
		    File file = new File("../../" + filename);
                    byte[] filecontent = new byte[0];
                    try {
                        filecontent = Files.readAllBytes(file.toPath());
                        p = new Packet(3,p.getId(),porta,ip,0,filecontent,0,0);
                        DatagramPacket pac = new DatagramPacket(p.toBytes(), p.toBytes().length, InetAddress.getByName(ip_destino),4200);
                        FSChunkProtocol.sendToGw(ds_envio,pac);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (p.getTipo() == 4){
                    //ds_envio.close()
                }
            }
        }).start();
            
        
    }
	

    public static void main(String [] args) throws IOException{
        System.out.println(args[1]);
        FastFileSrv s = new FastFileSrv(Integer.parseInt(args[0]),args[1]);
        s.runServer();
    }
}

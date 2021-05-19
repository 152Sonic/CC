import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FastFileSrv{
    private InetAddress ip;
    private Lock l;
    private DatagramSocket ds_envio;
    private DatagramSocket ds_rececao;
    private int porta;


    public FastFileSrv (int porta){
        this.porta = porta; //porta é indicada no terminal mas agora esta assim
        try {
            this.ds_envio = new DatagramSocket();
            this.ds_rececao = new DatagramSocket(this.porta);
            this.ip = InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.l = new ReentrantLock();
    }

    public void runServer (){
        while(true){
            new Thread(() -> {
                FSChunkProtocol.sendToGw(ds_envio,"Entao",4200,ip);
            }).start();
            new Thread(() -> {
                DatagramPacket p = FSChunkProtocol.receiveFromGw(ds_rececao);
                System.out.println(p.getData());
            }).start();
            
        }
    }

    public static void main(String [] args){
        FastFileSrv s = new FastFileSrv(4300);
        s.runServer();
    }
}
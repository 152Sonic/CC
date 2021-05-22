import java.io.IOException;
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
    private byte[] file;


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


    public void runServer () throws IOException{
        String s = "Ola ta tudo top";
        byte [] msg = s.getBytes();

        //estabelecer ligaçao
        Packet p1 = new Packet(1, porta, ip.getHostAddress(), 0, msg, 0, 0, "38955");
        DatagramPacket dp1 = new DatagramPacket(p1.toBytes(), p1.toBytes().length, ip, 4200);
        FSChunkProtocol.sendToGw(ds_envio, dp1);

        new Thread(() -> {
            while(true){
                Packet p = FSChunkProtocol.receiveFromGw(ds_rececao);
                System.out.println(p.toString());
                // gerir o pacote
                if (p.getTipo() == 2){
                    //pede o fivheiro
                }
                else if (p.getTipo() == 4){
                    //ds_envio.close()
                }
            }
        }).start();
            
        
    }




    public static void main(String [] args) throws IOException{
        FastFileSrv s = new FastFileSrv(Integer.parseInt(args[0]));
        s.runServer();
    }
}
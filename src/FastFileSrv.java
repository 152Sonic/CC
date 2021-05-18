import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FastFileSrv{
    private InetAddress ip;
    private Lock l;
    private DatagramSocket ds;
    private int porta;


    FastFileSrv (){
        try {
            this.ds = new DatagramSocket();
            this.ip = InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.porta = 80; //porta é indicada no terminal mas agora esta assim
        this.l = new ReentrantLock();
    }

    public void runServer (){

        //vai mandar merdas para o GW com sendToGw e vai receber merdas com receiveFromGw

    }

    public static void main(String [] args){
        FastFileSrv s = new FastFileSrv();
        s.runServer();
    }
}
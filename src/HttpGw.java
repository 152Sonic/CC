import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class HttpGw {
    private DatagramSocket ds;
    private InetAddress ip;

    HttpGw (){
        try {
            this.ds = new DatagramSocket();
            this.ip = InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void runGW (){

    }

    public static void main (String [] args){
        HttpGw gw = new HttpGw();
        gw.runGW();
    }
}
